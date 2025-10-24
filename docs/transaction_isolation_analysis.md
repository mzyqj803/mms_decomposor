# 事务隔离级别导致的并发问题分析 - 2025-10-24

## 问题发现

用户提出了一个关键问题：**"分析是否因为事务隔离级别导致在不同事务中的更新互相看不见？"**

答案是：**是的！** 这正是导致重复创建非标组件的根本原因。

## 环境信息

### 数据库隔离级别
```sql
SELECT @@transaction_isolation;
-- 结果：REPEATABLE-READ
```

**REPEATABLE-READ（可重复读）**是MySQL/MariaDB的默认隔离级别。

### 原始代码
```java
@Transactional  // ❌ 问题所在！
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    // 第一次检查
    Optional<Components> existingComponent = 
        componentsRepository.findByComponentCode(nonStandardCode);
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

## 问题分析

### REPEATABLE-READ 隔离级别特性

1. **一致性快照（Consistent Snapshot）**：
   - 事务开始时创建数据库快照
   - 事务内的所有读操作都基于这个快照

2. **可见性规则**：
   - 看不到其他未提交事务的修改（防止脏读）
   - 看不到在当前事务开始**之后**其他事务提交的修改（防止不可重复读）

3. **幻读（Phantom Read）**：
   - REPEATABLE-READ 无法完全防止幻读
   - 但在我们的场景中，问题不是幻读，而是快照隔离

### 并发场景详细分析

```
时间 | 线程A (分解箱包312)                      | 线程B (分解箱包311)
-----|----------------------------------------|----------------------------------------
T1   | BEGIN;                                 |
     | 创建快照@T1                             |
     | SELECT * FROM components               |
     | WHERE component_code='TTA0E104002~...' |
     | 结果：空（基于T1快照）                   |
-----|----------------------------------------|----------------------------------------
T2   | 获取synchronized(lock)                  | BEGIN;
     |                                        | 创建快照@T2
     |                                        | SELECT * FROM components
     |                                        | WHERE component_code='TTA0E104002~...'
     |                                        | 结果：空（基于T2快照）
     |                                        | 等待synchronized(lock)...
-----|----------------------------------------|----------------------------------------
T3   | 锁内第二次SELECT                        | (阻塞中)
     | 结果：仍然是空                          |
     | ⚠️ 因为仍然基于T1快照！                 |
     | INSERT INTO components...              |
     | 释放synchronized(lock)                  |
-----|----------------------------------------|----------------------------------------
T4   | COMMIT; ✅                             | 获取synchronized(lock)
     | 数据已持久化到数据库                    | 锁内第二次SELECT
     |                                        | ⚠️ 结果：仍然是空！
     |                                        | 因为仍然基于T2快照！
     |                                        | T2 < T4，看不到T4的提交
-----|----------------------------------------|----------------------------------------
T5   |                                        | INSERT INTO components...
     |                                        | 💥 创建重复记录
     |                                        | COMMIT;
```

### 为什么 synchronized 无效？

```java
@Transactional  // ← 事务在这里就开启了！
private Optional<Components> getOrCreateNonStandardComponent(...) {
    // ↓ 第一次查询：事务已开启，快照已创建
    componentsRepository.findByComponentCode(...)  // 基于事务开始时的快照
    
    synchronized (lock) {  // ← JVM内存级别的锁
        // ↓ 第二次查询：仍在同一个事务中
        componentsRepository.findByComponentCode(...)  // ⚠️ 仍基于同一个快照！
        
        // 即使其他线程已经提交了新数据，
        // 由于REPEATABLE-READ的快照隔离，
        // 这里仍然看不到！
    }
}
```

**关键点**：
- `synchronized`锁的作用域：JVM内存
- 事务快照的作用域：数据库
- `synchronized`锁释放 ≠ 事务提交
- 锁释放后事务未提交 → 其他线程进入锁时仍看不到数据

## 解决方案

### 方案1：移除方法级 @Transactional ✅ 推荐

```java
// ✅ 移除 @Transactional
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    // 第一次查询：不在事务中，每次都是实时查询
    Optional<Components> existingComponent = 
        componentsRepository.findByComponentCode(nonStandardCode);
    if (existingComponent.isPresent()) {
        return existingComponent;
    }
    
    synchronized (lock) {
        // 第二次查询：仍然是实时查询
        // ✅ 能看到其他线程刚刚提交的数据
        existingComponent = componentsRepository.findByComponentCode(nonStandardCode);
        if (existingComponent.isPresent()) {
            return existingComponent;
        }
        
        // 创建和保存：Repository的save方法会自动开启事务
        Components newComponent = new Components();
        // ...
        return Optional.of(componentsRepository.save(newComponent));  // 自动事务
    }
}
```

**优点**：
- ✅ 查询总是读取最新数据（READ-COMMITTED语义）
- ✅ 只在save时开启事务，作用域最小
- ✅ synchronized锁能正确工作

### 方案2：修改隔离级别为 READ-COMMITTED

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    // READ-COMMITTED 下，每次查询都能看到已提交的最新数据
    // ...
}
```

**优点**：
- ✅ 能看到其他事务已提交的数据
- ✅ 避免了快照隔离问题

**缺点**：
- ⚠️ 可能出现不可重复读（同一事务内多次读取结果不同）
- ⚠️ 但在我们的场景中，这不是问题

### 方案3：使用悲观锁 SELECT ... FOR UPDATE

```java
@Repository
public interface ComponentsRepository extends JpaRepository<Components, Long> {
    
    @Query("SELECT c FROM Components c WHERE c.componentCode = :code FOR UPDATE")
    Optional<Components> findByComponentCodeForUpdate(@Param("code") String code);
}

// 使用
@Transactional
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    synchronized (lock) {
        // 使用悲观锁查询，会锁定行
        Optional<Components> existingComponent = 
            componentsRepository.findByComponentCodeForUpdate(nonStandardCode);
        
        if (existingComponent.isPresent()) {
            return existingComponent;
        }
        
        // 其他线程的INSERT会被阻塞，直到当前事务提交
        // ...
    }
}
```

**优点**：
- ✅ 数据库级别的行锁
- ✅ 完全避免并发问题

**缺点**：
- ⚠️ 性能开销较大
- ⚠️ 可能导致死锁

### 方案4：数据库唯一约束（已实施）✅

```sql
ALTER TABLE components ADD UNIQUE KEY uk_component_code (Component_Code);
```

**作为最后一道防线**：
- ✅ 即使应用层有bug，数据库也会拒绝重复数据
- ✅ 简单可靠
- ✅ 性能影响小

## 已实施的解决方案

### 1. 移除 @Transactional ✅

**修改前**：
```java
@Transactional
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
```

**修改后**：
```java
/**
 * 注意：不使用@Transactional，避免REPEATABLE-READ隔离级别的快照读问题
 * 在REPEATABLE-READ下，事务开始时会创建快照，导致锁内的第二次查询
 * 仍然看不到其他已提交事务的数据，从而可能创建重复记录
 */
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
```

### 2. 数据库唯一约束 ✅

```sql
ALTER TABLE components ADD UNIQUE KEY uk_component_code (Component_Code);
```

### 为什么这个组合方案最好？

1. **移除@Transactional**：
   - 查询时没有事务快照限制
   - 第二次查询能看到其他线程刚提交的数据
   - synchronized锁能正确防止并发

2. **唯一约束**：
   - 作为最后防线
   - 即使应用逻辑有bug，数据库也保证数据完整性
   - 万一有极端情况（如分布式部署），数据库仍能保证唯一性

## 测试验证

### 场景1：正常并发创建

```
线程A: 查询(空) → 获取锁 → 查询(空) → 创建 → 保存 → 释放锁 → ✅
线程B: 查询(空) → 等待锁 → 获取锁 → 查询(✅找到A创建的) → 返回已有 → ✅
```

### 场景2：极端并发（万一）

```
线程A和B几乎同时INSERT
↓
数据库唯一约束生效
↓
线程B的INSERT失败：Duplicate entry
↓
应用层可以捕获异常并重新查询（可选）
```

## 关键经验教训

### 1. @Transactional 的时机很重要

```java
// ❌ 错误：整个方法都在事务中
@Transactional
public void businessLogic() {
    query();  // 基于快照
    synchronized(lock) {
        query();  // 仍基于同一个快照
        save();
    }
}

// ✅ 正确：只在需要时开启事务
public void businessLogic() {
    query();  // 实时查询
    synchronized(lock) {
        query();  // 实时查询
        save();   // Repository自动开启事务
    }
}
```

### 2. synchronized 锁 ≠ 事务隔离

- **synchronized**：JVM内存级别的互斥
- **Transaction Isolation**：数据库级别的可见性控制
- **两者独立工作**，需要正确配合

### 3. REPEATABLE-READ 的陷阱

在并发创建场景中：
- ✅ 适合：读多写少，需要一致性快照
- ❌ 不适合：并发写入，需要看到最新数据

### 4. 多层防护的重要性

1. **应用层**：synchronized锁
2. **事务层**：正确的隔离级别或无事务查询
3. **数据库层**：唯一约束

三层配合，确保数据完整性。

## 相关配置

### 查看当前隔离级别
```sql
SELECT @@transaction_isolation;
```

### 修改会话隔离级别（如果需要）
```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

### 修改全局隔离级别（需谨慎）
```sql
SET GLOBAL TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

## 监控建议

部署后监控以下指标：

1. **唯一约束冲突频率**：
   ```sql
   -- 如果频繁出现Duplicate entry错误，说明仍有并发问题
   ```

2. **非标组件创建日志**：
   ```
   ✅ "Non-standard component already exists (second check)"
   ❌ "Created new non-standard component"（应该很少）
   ```

3. **分解成功率**：
   - 应该接近100%
   - 如果有失败，检查是否是唯一约束冲突

## 总结

**问题本质**：`@Transactional` + `REPEATABLE-READ` + `synchronized` 的组合导致：
- synchronized锁只能保证JVM内部的互斥
- 但无法改变事务的快照隔离
- 导致锁内的查询仍然看不到其他已提交的数据

**解决方案**：
- ✅ 移除查询阶段的事务
- ✅ 添加数据库唯一约束
- ✅ 双重防护，确保数据完整性

**适用场景**：
任何需要"先查询后创建"的并发场景，都需要注意事务隔离级别对数据可见性的影响。

## 相关文件

- `src/main/java/com/mms/service/impl/BreakdownServiceImpl.java` - 修复后的代码
- `cleanup_duplicate_components_proper.sql` - 数据清理脚本
- `docs/component_code_unique_constraint_fix.md` - 问题修复文档

