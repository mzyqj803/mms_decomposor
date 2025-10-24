# 箱包分解流程优化修改说明

## 修改日期
2025-10-24

## 问题描述
上传装箱单后自动分解合同时，每次分解完成只有一个箱包的分解数据。问题原因是多个线程并发删除箱包记录时产生锁冲突。

## 解决方案

### 1. 修改删除旧记录的策略
**位置**: `BreakdownServiceImpl.breakdownContract()`

**修改前**:
- 每个箱包在自己的线程中独立删除旧记录（`deleteOldRecords=true`）
- 多个线程并发删除时可能产生表级锁冲突

**修改后**:
- 在主线程中批量删除所有箱包的旧记录（使用 `contractId` 一次性删除）
- 并行分解时不再删除记录（`deleteOldRecords=false`）
- 避免了并发锁冲突，提高了性能

### 2. 批量操作优化
使用以下批量操作替代循环操作：
```java
// 批量删除分解记录
breakdownRepository.deleteByContractId(contractId);

// 批量删除问题部件记录
problemsRepository.deleteByContractId(contractId);

// 批量更新箱包状态
containersRepository.saveAll(containers);
```

### 3. 增强日志记录
添加了详细的执行日志，分为4个步骤：

**步骤1: 批量删除合同下所有箱包的旧分解记录**
- 记录删除操作的开始和结束
- 记录耗时统计

**步骤2: 并行分解所有箱包**
- 记录每个线程的分解开始和完成
- 记录每个箱包的处理部件数和耗时
- 记录任务提交和完成进度
- 统计成功/失败数量

**步骤3: 生成分解汇总表**
- 记录汇总表生成耗时

**步骤4: 更新合同状态**
- 记录状态更新耗时
- 输出最终统计信息

### 4. 关闭 Hibernate SQL 日志
**位置**: `application.yml`

**修改内容**:
```yaml
spring:
  jpa:
    show-sql: false      # 从 true 改为 false
    properties:
      hibernate:
        format_sql: false  # 从 true 改为 false
```

**目的**: 减少日志输出，降低日志分析难度

## 日志输出示例

```
=========== 开始对合同进行工艺分解 ===========
合同ID: 1
合同包含 5 个箱包
========== 步骤1: 批量删除合同下所有箱包的旧分解记录 ==========
开始批量删除分解记录: contractId=1
批量删除分解记录完成
开始批量删除问题部件记录: contractId=1
批量删除问题部件记录完成
开始批量更新箱包状态为未分解
批量更新 5 个箱包状态完成
========== 所有箱包旧记录删除完成，耗时: 156ms ==========
========== 步骤2: 并行分解所有箱包 ==========
使用 4 个线程并行处理 5 个箱包
已提交 5 个箱包分解任务到线程池，等待执行完成...
线程 breakdown-worker-1 开始分解箱包: containerId=1, containerNo=C001
线程 breakdown-worker-2 开始分解箱包: containerId=2, containerNo=C002
线程 breakdown-worker-1 完成箱包分解: containerId=1, containerNo=C001, 处理部件数=25, 耗时=1234ms
收到第 1/5 个箱包的分解结果: containerId=1, containerNo=C001
线程 breakdown-worker-2 完成箱包分解: containerId=2, containerNo=C002, 处理部件数=30, 耗时=1456ms
收到第 2/5 个箱包的分解结果: containerId=2, containerNo=C002
...
========== 所有箱包并行分解完成 ==========
总耗时: 3456ms, 平均每个箱包: 691ms
成功: 5 个, 失败: 0 个
开始关闭线程池...
线程池已关闭
========== 步骤3: 生成分解汇总表 ==========
生成汇总表耗时: 234ms
========== 步骤4: 更新合同状态 ==========
更新合同状态为已完成，耗时: 45ms
=========== 合同工艺分解全部完成 ===========
合同ID: 1, 箱包总数: 5, 成功: 5, 失败: 0
处理部件总数: 150, 问题部件数: 3
总耗时: 3891ms, 平均速度: 38.56部件/秒
合同状态已更新为: COMPLETED
===============================================
```

## 预期效果

1. **解决主要问题**: 所有箱包都能正确分解并保存数据
2. **提高性能**: 批量删除比循环删除更快
3. **避免锁冲突**: 主线程统一删除，避免并发锁问题
4. **便于调试**: 详细的日志便于追踪每个箱包的分解状态
5. **减少日志干扰**: 关闭 SQL 日志后，业务日志更清晰

## 修改文件列表

1. `src/main/java/com/mms/service/impl/BreakdownServiceImpl.java`
   - `breakdownContract()` 方法：修改删除策略和日志
   
2. `src/main/resources/application.yml`
   - 关闭 Hibernate SQL 日志输出

## 测试建议

1. 上传包含多个箱包的装箱单
2. 检查日志确认每个箱包都开始和完成分解
3. 验证数据库中所有箱包的分解记录都正确保存
4. 检查合同状态是否正确更新为 COMPLETED
5. 验证问题部件是否正确记录到问题件表

## 注意事项

- `breakdownContainer(containerId, deleteOldRecords)` 方法仍然保留了 `deleteOldRecords` 参数
- 单独调用箱包分解时仍会删除旧记录（`deleteOldRecords=true`）
- 合同级别分解时不再删除（`deleteOldRecords=false`），由主线程统一处理

