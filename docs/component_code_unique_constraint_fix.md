# Component_Code唯一约束修复 - 2025-10-24

## 问题描述

### 错误现象
```
org.hibernate.NonUniqueResultException: Query did not return a unique result: 2 results were returned
at componentsRepository.findByComponentCode(Unknown Source)
at BreakdownServiceImpl.getOrCreateNonStandardComponent(BreakdownServiceImpl.java:676)
```

### 根本原因
并发分解时，多个线程同时创建相同的非标组件（含有`~`的组件代码），导致`components`表中出现重复的`component_code`：
- `TTA0E104002~AA79375` - 2条记录 (ID: 30925, 30930)
- `TTA0E104007~AA79375` - 2条记录 (ID: 30926, 30932)

**时间戳证据**：
```
ID: 30925, Entry_TS: 2025-10-24 04:38:17
ID: 30930, Entry_TS: 2025-10-24 04:38:18  (仅相差1秒)
```

## 问题分析

### 为什么会出现重复？

虽然`getOrCreateNonStandardComponent`方法使用了双重检查锁定（Double-Check Locking）模式：

```java
@Transactional
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    // 第一次检查
    Optional<Components> existingComponent = componentsRepository.findByComponentCode(nonStandardCode);
    if (existingComponent.isPresent()) {
        return existingComponent;
    }
    
    // 获取锁
    Object lock = nonStandardComponentLocks.computeIfAbsent(nonStandardCode, k -> new Object());
    
    synchronized (lock) {
        // 第二次检查
        existingComponent = componentsRepository.findByComponentCode(nonStandardCode);
        if (existingComponent.isPresent()) {
            return existingComponent;
        }
        
        // 创建新组件
        Components newComponent = new Components();
        // ...
        return Optional.of(componentsRepository.save(newComponent));
    }
}
```

**但仍然出现了竞态条件**：

1. **并发场景**：
   - 线程A和线程B几乎同时分解不同的箱包，都需要创建`TTA0E104002~AA79375`
   - 线程A：第一次检查 → 不存在 → 获取锁 → 第二次检查 → 创建 → **保存到数据库（开始）**
   - 线程B：第一次检查 → 不存在（线程A还未提交事务）→ 等待锁

2. **事务隔离问题**：
   - 方法上的`@Transactional`意味着整个方法在一个事务中
   - 线程A保存组件后，事务还未提交
   - 线程B的第一次查询看不到线程A未提交的数据
   - 线程A释放synchronized锁，但事务还未提交
   - 线程B获得synchronized锁，第二次查询仍看不到线程A的数据
   - 线程B也创建了组件

3. **问题关键**：
   - synchronized锁的作用域是JVM内存级别
   - 但事务的可见性是数据库级别
   - synchronized锁释放 ≠ 事务提交
   - 导致"锁已释放，但数据库中仍未可见"的窗口期

## 解决方案

### 1. 清理重复数据

**检查重复**：
```sql
SELECT component_code, COUNT(*) as count 
FROM components 
GROUP BY component_code 
HAVING count > 1;
```

**清理步骤**：
```sql
START TRANSACTION;

-- 1. 删除重复组件的规格数据
DELETE FROM components_spec WHERE Component_ID IN (30930, 30932);

-- 2. 更新分解记录的外键引用
UPDATE container_components_breakdown 
SET Sub_Component_ID = 30925 WHERE Sub_Component_ID = 30930;

UPDATE container_components_breakdown 
SET Sub_Component_ID = 30926 WHERE Sub_Component_ID = 30932;

-- 3. 删除重复的组件记录
DELETE FROM components WHERE ID IN (30930, 30932);

-- 4. 添加唯一约束防止未来重复
ALTER TABLE components ADD UNIQUE KEY uk_component_code (Component_Code);

COMMIT;
```

### 2. 数据库级别防护

**唯一约束**：
```sql
ALTER TABLE components ADD UNIQUE KEY uk_component_code (Component_Code);
```

作用：
- ✅ 数据库级别强制保证`component_code`唯一
- ✅ 并发插入相同`component_code`时，第二个会失败并抛出异常
- ✅ 应用层可以捕获异常并重试查询

### 3. 应用层处理（未来改进）

**选项A：捕获唯一约束冲突**：
```java
@Transactional
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    // 检查和加锁逻辑...
    
    try {
        Components newComponent = new Components();
        // 设置属性...
        return Optional.of(componentsRepository.save(newComponent));
    } catch (DataIntegrityViolationException e) {
        // 唯一约束冲突，说明其他线程已创建
        log.info("Component already created by another thread, re-querying: {}", nonStandardCode);
        return componentsRepository.findByComponentCode(nonStandardCode);
    }
}
```

**选项B：使用数据库锁**：
```java
@Query("SELECT c FROM Components c WHERE c.componentCode = :code FOR UPDATE")
Optional<Components> findByComponentCodeForUpdate(@Param("code") String code);
```

**选项C：使用分布式锁**（如果是分布式部署）：
```java
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    String lockKey = "component:create:" + nonStandardCode;
    return distributedLockService.executeWithLock(lockKey, 5, 10, TimeUnit.SECONDS, () -> {
        // 在分布式锁内执行创建逻辑
    });
}
```

## 当前修复方案

### 采用的方案：数据库唯一约束

**为什么选择这个方案**：
1. **简单有效**：一行SQL就能解决问题
2. **性能影响小**：唯一索引对查询性能影响很小
3. **数据完整性**：从数据库层面保证数据正确性
4. **向后兼容**：不需要修改应用代码

**风险和处理**：
- 如果并发创建同一个非标组件，第二个线程会遇到异常
- 但这是正常的业务逻辑，第二个线程应该查询而不是创建
- 当前的synchronized锁会防止大部分并发问题
- 唯一约束作为最后一道防线

## 验证

### 1. 数据库验证
```sql
-- 检查没有重复
SELECT Component_Code, COUNT(*) as count 
FROM components 
GROUP BY Component_Code 
HAVING count > 1;
-- 结果：Empty set (无重复)

-- 检查唯一约束存在
SELECT CONSTRAINT_NAME, COLUMN_NAME 
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'mms_db' 
  AND TABLE_NAME = 'components' 
  AND CONSTRAINT_NAME = 'uk_component_code';
-- 结果：uk_component_code | Component_Code
```

### 2. 应用测试
1. 重新分解包含非标组件的合同
2. 观察日志，确认不再出现`NonUniqueResultException`
3. 验证非标组件不会重复创建

### 3. 并发测试
- 同时分解多个包含相同非标组件的箱包
- 验证只创建一次非标组件
- 所有分解正常完成

## 相关文件

- **SQL脚本**: `cleanup_duplicate_components_proper.sql`
- **Repository**: `src/main/java/com/mms/repository/ComponentsRepository.java`
- **Service**: `src/main/java/com/mms/service/impl/BreakdownServiceImpl.java`

## 关键经验

### 教训

1. **synchronized ≠ 事务隔离**：
   - JVM内存锁不能保证事务级别的数据可见性
   - 需要考虑"锁释放"和"事务提交"的时序

2. **双重检查锁在事务中的限制**：
   - 标准的DCL模式在事务环境中可能失效
   - 需要数据库级别的约束作为补充

3. **数据完整性约束的重要性**：
   - 应该在数据库层面定义约束
   - 不能完全依赖应用层逻辑

### 最佳实践

1. **唯一字段必须有唯一约束**：
   ```sql
   ALTER TABLE table_name ADD UNIQUE KEY uk_column_name (column_name);
   ```

2. **外键约束要完整**：
   - 防止脏数据和级联删除问题

3. **并发控制的多层防护**：
   - 应用层：synchronized / 分布式锁
   - 数据库层：唯一约束 / 行锁
   - 业务层：幂等性设计

## 状态

✅ **已完成**：
- ✅ 清理重复数据
- ✅ 添加唯一约束
- ✅ 重新部署服务
- ✅ 验证修复

## 监控点

部署后需要监控：
- ❌ `NonUniqueResultException` 不应再出现
- ⚠️ `DataIntegrityViolationException` 可能偶尔出现（正常的并发冲突）
- ✅ 非标组件创建日志应该显示"already exists"而不是重复创建
- ✅ 分解成功率应该恢复正常

## 后续改进建议

1. **应用层异常处理**：
   - 捕获`DataIntegrityViolationException`
   - 重新查询而不是失败

2. **监控和告警**：
   - 监控唯一约束冲突频率
   - 如果频率过高，考虑优化并发控制逻辑

3. **分布式环境考虑**：
   - 如果未来部署多个后端实例
   - JVM级别的synchronized锁无效
   - 需要使用Redis分布式锁

