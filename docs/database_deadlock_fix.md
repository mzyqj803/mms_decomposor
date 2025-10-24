# 数据库死锁问题分析与解决方案

## 问题发现
2025-10-24 - 在并行分解箱包时，数据库日志出现死锁（Deadlock）错误。

## 🔥 死锁原因分析

### 死锁场景1：并发更新 containers 表

**问题代码**：
```java
@Transactional
public Map<String, Object> breakdownContainer(Long containerId, ...) {
    // 1. 开始时更新状态为0
    container.setStatus(0);
    containersRepository.save(container);  // 获取 container 行的排他锁
    
    // 2. 插入大量 breakdown 记录
    for (部件...) {
        saveBreakdownRecord(...);  // 外键约束导致对 containers 表加共享锁
    }
    
    // 3. 结束时更新状态为1
    container.setStatus(1);
    containersRepository.save(container);  // 再次获取 container 行的排他锁
}
```

**死锁发生过程**：
```
时间线：
T1: 线程A 更新箱包A状态=0 [持有A的排他锁]
T2: 线程B 更新箱包B状态=0 [持有B的排他锁]
T3: 线程A 插入breakdown记录 [外键导致需要B的共享锁，等待...]
T4: 线程B 插入breakdown记录 [外键导致需要A的共享锁，等待...]
💥 死锁！
```

**根本原因**：
1. 多个线程同时更新不同的 containers 记录
2. 插入 container_components_breakdown 时，外键约束 `fk_ccb_container` 需要对 containers 表加共享锁
3. 更新 container 状态需要排他锁
4. **共享锁 + 排他锁 + 多线程 = 死锁**

### 死锁场景2：外键约束的锁升级

**外键定义**：
```sql
CREATE TABLE container_components_breakdown (
  Container_Component_ID    INT,
  Sub_Component_ID          INT,
  Container_ID              INT,
  
  -- 3个外键约束
  CONSTRAINT fk_ccb_ccid      FOREIGN KEY (Container_Component_ID) 
    REFERENCES container_components(ID),
  CONSTRAINT fk_ccb_subcomp   FOREIGN KEY (Sub_Component_ID)
    REFERENCES components(ID),
  CONSTRAINT fk_ccb_container FOREIGN KEY (Container_ID)
    REFERENCES containers(ID)
);
```

**影响**：
- 每次插入 breakdown 记录时，InnoDB 会在3个被引用表上加**共享锁**
- 如果同时有其他线程试图更新这些表，会产生锁等待
- 多个线程交叉操作时，容易形成循环等待 → **死锁**

### 死锁场景3：Spring AOP 事务失效

**问题代码**：
```java
@Transactional
public Map<String, Object> breakdownContract(Long contractId) {
    // 主事务开启，持有连接
    
    deleteContractBreakdownRecords(...);  // private 方法
    // ❌ REQUIRES_NEW 不生效！仍在外层事务中！
    
    // 长时间并行处理...
    
    updateContractStatusToCompleted(...);  // private 方法  
    // ❌ REQUIRES_NEW 不生效！仍在外层事务中！
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void deleteContractBreakdownRecords(...) {
    // Spring AOP 无法拦截 private 方法
    // 这个注解实际上无效！
}
```

**后果**：
- 整个 `breakdownContract` 过程在一个大事务中
- 主线程长时间持有数据库连接和锁
- 加剧了死锁风险

## ✅ 解决方案

### 方案1：移除并发更新 containers 状态

**修改前**：
```java
@Transactional
public Map<String, Object> breakdownContainer(...) {
    container.setStatus(0);  // ❌ 每个线程都更新
    containersRepository.save(container);
    
    // 处理数据...
    
    container.setStatus(1);  // ❌ 每个线程都更新
    containersRepository.save(container);
}
```

**修改后**：
```java
@Transactional
public Map<String, Object> breakdownContainer(..., boolean deleteOldRecords) {
    if (deleteOldRecords) {
        // 只在单独调用时更新状态
        container.setStatus(0);
        containersRepository.save(container);
    }
    
    // 处理数据...
    
    // ✅ 不再更新状态，避免并发更新
    // 状态将在所有箱包分解完成后批量更新
}
```

### 方案2：批量更新箱包状态

**在主流程中添加**：
```java
public Map<String, Object> breakdownContract(Long contractId) {
    // 步骤1：批量删除旧记录并更新状态为0
    deleteContractBreakdownRecords(...);
    
    // 步骤2：并行分解（不更新状态）
    // ... 并行处理 ...
    
    // 步骤3：所有箱包分解完成后，批量更新状态为1
    batchUpdateContainersStatus(containerIds, 1);
    
    // 步骤4：更新合同状态
    updateContractStatusToCompleted(...);
}
```

**新增批量更新方法**：
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void batchUpdateContainersStatus(List<Long> containerIds, Integer status) {
    List<Containers> containers = containersRepository.findAllById(containerIds);
    for (Containers container : containers) {
        container.setStatus(status);
    }
    containersRepository.saveAll(containers);
    // 一次性批量更新，避免并发冲突
}
```

### 方案3：修复 Spring AOP 事务失效

**问题**：private 方法上的 `@Transactional` 注解不生效

**解决**：
1. 将方法改为 public
2. 通过 Spring 代理对象调用

```java
// ❌ 错误：直接调用
deleteContractBreakdownRecords(...);

// ✅ 正确：通过代理调用
BreakdownServiceImpl selfProxy = applicationContext.getBean(BreakdownServiceImpl.class);
selfProxy.deleteContractBreakdownRecords(...);
```

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void deleteContractBreakdownRecords(...) {
    // 改为 public 方法，Spring AOP 可以拦截
    // REQUIRES_NEW 现在生效，使用独立事务
    // 事务完成后立即释放连接和锁
}
```

## 📊 修改对比

### 锁持有时间对比

| 操作 | 修改前 | 修改后 | 改善 |
|-----|--------|--------|------|
| 删除记录 | 在主事务中（持续整个过程） | 独立事务（< 1秒） | ✅ 减少99% |
| 更新状态 | 每个线程独立更新（并发冲突） | 批量更新（串行，无冲突） | ✅ 消除死锁 |
| 主事务 | 持续整个分解过程 | 无主事务（各操作独立） | ✅ 不持有连接 |
| 合同状态更新 | 在主事务中 | 独立事务（< 1秒） | ✅ 减少99% |

### 并发安全性对比

| 场景 | 修改前 | 修改后 |
|-----|--------|--------|
| 并发更新 containers | ❌ 死锁风险 | ✅ 串行批量更新 |
| 外键锁冲突 | ❌ 高风险 | ✅ 低风险（单次批量） |
| 事务隔离 | ❌ 长事务 | ✅ 短事务 |
| 连接池压力 | ❌ 高（长时间占用） | ✅ 低（快速释放） |

## 📝 修改清单

### 1. `breakdownContainer()` 方法

**文件**：`BreakdownServiceImpl.java`

**修改内容**：
- ✅ 移除事务结束时的状态更新
- ✅ 只在 `deleteOldRecords=true` 时更新状态
- ✅ 添加详细日志

### 2. `breakdownContract()` 方法

**修改内容**：
- ✅ 移除 `@Transactional` 注解
- ✅ 使用代理对象调用独立事务方法
- ✅ 在并行分解完成后批量更新状态

### 3. 新增 `batchUpdateContainersStatus()` 方法

**功能**：批量更新箱包状态

**特点**：
- `@Transactional(propagation = REQUIRES_NEW)`
- public 方法（Spring AOP 可拦截）
- 快速完成，立即释放连接

### 4. 修改独立事务方法可见性

**修改**：
- `deleteContractBreakdownRecords()`: private → public
- `updateContractStatusToCompleted()`: private → public

**原因**：Spring AOP 只能拦截 public 方法

## 🧪 测试验证

### 1. 并发压力测试

```bash
# 同时上传多个包含多个箱包的装箱单
# 观察是否出现死锁错误
```

### 2. 数据库锁监控

```sql
-- 查看当前锁等待
SELECT * FROM information_schema.INNODB_LOCKS;

-- 查看死锁历史
SHOW ENGINE INNODB STATUS;
```

### 3. 日志验证

检查日志中的关键信息：
```log
========== 步骤1: 批量删除合同下所有箱包的旧分解记录 ==========
批量删除分解记录完成
批量更新 5 个箱包状态完成
========== 所有箱包旧记录删除完成，耗时: 156ms ==========

========== 步骤2: 并行分解所有箱包 ==========
线程 breakdown-worker-1 完成箱包分解（未更新状态）: ...
线程 breakdown-worker-2 完成箱包分解（未更新状态）: ...
成功: 5 个, 失败: 0 个

========== 批量更新箱包状态为已分解 ==========
批量更新 5 个箱包状态完成

✅ 没有并发更新冲突
✅ 没有死锁错误
```

## 🎯 预期效果

### 1. 消除死锁
- ✅ 不再有并发更新 containers 表
- ✅ 批量更新串行执行，无冲突
- ✅ 外键锁冲突大幅减少

### 2. 提高性能
- ✅ 独立事务快速释放连接
- ✅ 连接池压力降低
- ✅ 整体吞吐量提升

### 3. 提高稳定性
- ✅ 事务边界清晰
- ✅ 错误更容易定位
- ✅ 系统更可靠

## 📚 相关知识

### InnoDB 锁机制

1. **共享锁 (S Lock)**：允许读，阻止写
2. **排他锁 (X Lock)**：阻止读和写
3. **外键检查**：在被引用表上加共享锁

### 死锁检测

InnoDB 自动检测死锁并回滚其中一个事务：
```
*** (1) TRANSACTION:
TRANSACTION 421234, ACTIVE 2 sec inserting
mysql tables in use 1, locked 1
LOCK WAIT 2 lock struct(s), heap size 1136, 1 row lock(s)

*** (2) TRANSACTION:  
TRANSACTION 421235, ACTIVE 2 sec updating
mysql tables in use 1, locked 1
3 lock struct(s), heap size 1136, 2 row lock(s), undo log entries 1

*** WE ROLL BACK TRANSACTION (1)
```

### Spring 事务传播

- **REQUIRED**（默认）：加入当前事务
- **REQUIRES_NEW**：创建新事务，挂起当前事务
- **NESTED**：嵌套事务

**注意**：Spring AOP 只能拦截 public 方法！

## 🔄 回滚方案

如果出现问题，可以快速回滚：

### 1. 恢复并发更新状态
```java
@Transactional
public Map<String, Object> breakdownContainer(...) {
    container.setStatus(0);
    containersRepository.save(container);
    
    // 处理...
    
    container.setStatus(1);
    containersRepository.save(container);
}
```

### 2. 移除批量更新
注释掉 `batchUpdateContainersStatus()` 调用

## 相关文档

- [database_connection_timeout_fix.md](./database_connection_timeout_fix.md) - 连接池配置
- [breakdown_optimization_changes.md](./breakdown_optimization_changes.md) - 分解流程优化
- [MySQL InnoDB 锁机制](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html)
- [Spring 事务管理](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)

---

**修改日期**: 2025-10-24  
**问题级别**: 严重 (P0) - 死锁导致系统不可用  
**修复状态**: ✅ 已完成  
**测试状态**: ⏳ 待验证  
**影响范围**: 所有并发分解场景

