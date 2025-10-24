# 数据库连接超时问题解决方案

## 问题描述
在并行分解箱包时，日志出现数据库连接超时（Connection Timeout）错误。

## 问题原因分析

### 1. **缺少连接池配置**
- Spring Boot 使用 HikariCP 作为默认连接池
- 但原配置文件中**没有配置连接池参数**
- 默认连接数（10个）不足以支持并行分解

### 2. **事务策略不合理**
- `breakdownContract()` 方法使用 `@Transactional`
- 整个合同分解过程（包括删除、并行分解、汇总、更新状态）都在一个事务中
- 主事务持有连接时间过长（可能数十秒到数分钟）

### 3. **并发连接需求**
- 并行分解使用线程池，线程数 = min(箱包数, CPU核心数)
- 每个线程需要独立的数据库连接
- 主线程也需要连接
- **总连接需求 = 1（主线程）+ N（工作线程）+ 1（其他请求）**

### 4. **连接分配示例**
假设 8 核 CPU，10 个箱包：
```
线程数 = min(10, 8) = 8
需要连接数 = 1（主事务）+ 8（工作线程）= 9
默认连接池大小 = 10
```
虽然理论上够用，但：
- 主事务长时间持有连接
- 其他请求可能需要连接
- 连接获取/释放有时间差
- **容易发生连接耗尽**

## 解决方案

### 1. **配置 HikariCP 连接池**

```yaml
spring:
  datasource:
    hikari:
      # 连接池最大连接数（支持更多并发）
      maximum-pool-size: 20
      # 最小空闲连接数
      minimum-idle: 5
      # 连接超时时间（毫秒）- 从连接池获取连接的最大等待时间
      connection-timeout: 30000
      # 空闲连接超时时间（毫秒）- 600秒 = 10分钟
      idle-timeout: 600000
      # 连接最大生命周期（毫秒）- 1800秒 = 30分钟
      max-lifetime: 1800000
      # 连接测试查询
      connection-test-query: SELECT 1
      # 自动提交
      auto-commit: true
      # 连接池名称
      pool-name: MMS-HikariCP
      # 泄漏检测阈值（毫秒）- 60秒未归还的连接会被记录
      leak-detection-threshold: 60000
```

**关键参数说明**：
- `maximum-pool-size: 20` - 足够支持并行分解 + 其他请求
- `connection-timeout: 30000` - 30秒超时，避免无限等待
- `leak-detection-threshold: 60000` - 检测连接泄漏，便于排查问题

### 2. **优化事务策略**

**修改前**：
```java
@Transactional
public Map<String, Object> breakdownContract(Long contractId) {
    // 删除旧记录（持有连接）
    // 并行分解（持有连接）
    // 生成汇总（持有连接）
    // 更新状态（持有连接）
}
```
**主事务持有连接时间 = 整个流程时间（可能数分钟）**

**修改后**：
```java
// 不使用事务，让子操作使用独立事务
public Map<String, Object> breakdownContract(Long contractId) {
    // 独立事务：删除旧记录
    deleteContractBreakdownRecords(contractId, containers);
    
    // 并行分解（每个箱包独立事务）
    // 不持有主连接
    
    // 只读操作：生成汇总
    generateBreakdownSummary(contractId);
    
    // 独立事务：更新状态
    updateContractStatusToCompleted(contractId);
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void deleteContractBreakdownRecords(...) {
    // 快速完成，立即释放连接
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
private void updateContractStatusToCompleted(...) {
    // 快速完成，立即释放连接
}
```

**优势**：
- ✅ 主线程不持有连接
- ✅ 删除操作完成后立即释放连接
- ✅ 每个箱包分解使用独立连接和事务
- ✅ 更新状态操作独立，快速完成

### 3. **添加连接池监控日志**

```yaml
logging:
  level:
    # HikariCP 连接池日志
    com.zaxxer.hikari.HikariConfig: DEBUG
    com.zaxxer.hikari: INFO
```

**监控信息**：
- 连接池初始化信息
- 连接获取/释放统计
- 连接泄漏警告
- 连接池状态

## 配置详解

### HikariCP 连接数配置建议

```
最大连接数 = (并发线程数 × 1.5) + 5
```

示例计算：
- CPU 核心数：8
- 最大并发箱包分解：8
- 预留其他请求：5
- **推荐最大连接数 = (8 × 1.5) + 5 = 17 ≈ 20**

### 超时时间配置建议

| 参数 | 推荐值 | 说明 |
|-----|-------|------|
| connection-timeout | 30000ms (30秒) | 获取连接超时时间，避免无限等待 |
| idle-timeout | 600000ms (10分钟) | 空闲连接保持时间 |
| max-lifetime | 1800000ms (30分钟) | 连接最大存活时间，定期刷新 |
| leak-detection-threshold | 60000ms (60秒) | 连接泄漏检测阈值 |

## 修改文件清单

### 1. `src/main/resources/application.yml`
- ✅ 添加完整的 HikariCP 连接池配置
- ✅ 添加连接池监控日志配置

### 2. `src/main/java/com/mms/service/impl/BreakdownServiceImpl.java`
- ✅ 移除 `breakdownContract()` 的 `@Transactional` 注解
- ✅ 新增 `deleteContractBreakdownRecords()` 方法（独立事务）
- ✅ 新增 `updateContractStatusToCompleted()` 方法（独立事务）
- ✅ 使用 `REQUIRES_NEW` 传播属性确保独立事务

## 预期效果

### 1. **解决连接超时问题**
- ✅ 连接池增大到 20，支持更多并发
- ✅ 主线程不再长时间持有连接
- ✅ 各操作使用独立事务，快速释放连接

### 2. **提高系统稳定性**
- ✅ 避免连接耗尽
- ✅ 避免死锁
- ✅ 更好的事务隔离

### 3. **便于问题排查**
- ✅ 连接池状态日志
- ✅ 连接泄漏检测
- ✅ 详细的性能监控

## 测试验证

### 1. 检查连接池启动日志
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
HikariPool-1 - Pool stats (total=5, active=0, idle=5, waiting=0)
```

### 2. 测试并行分解
- 上传包含多个箱包的装箱单
- 观察日志中的连接池状态
- 确认无连接超时错误

### 3. 监控关键指标
```log
# 正常日志示例
HikariPool-1 - Pool stats (total=12, active=8, idle=4, waiting=0)

# 连接泄漏警告（如果出现需要排查）
Connection leak detection triggered for ...
```

## 进一步优化建议

### 1. 根据实际负载调整连接数
```
# 监控实际使用的最大连接数
SELECT MAX(THREADS_CONNECTED) FROM information_schema.PROCESSLIST;

# 调整 maximum-pool-size 为实际峰值 + 20% 余量
```

### 2. 配置数据库端最大连接数
```sql
-- MariaDB/MySQL
SET GLOBAL max_connections = 200;
```

### 3. 考虑使用连接池监控工具
- Spring Boot Actuator
- Prometheus + Grafana
- HikariCP Metrics

## 回滚方案

如果出现问题，可以快速回滚：

### 1. 恢复默认连接池配置
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # 恢复默认值
```

### 2. 恢复事务注解
```java
@Transactional
public Map<String, Object> breakdownContract(Long contractId) {
    // ...
}
```

## 相关文档

- [breakdown_optimization_changes.md](./breakdown_optimization_changes.md) - 分解流程优化
- [HikariCP 官方文档](https://github.com/brettwooldridge/HikariCP)
- [Spring Boot 数据源配置](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data)

---

**修改日期**: 2025-10-24  
**问题级别**: 严重 (P0)  
**修复状态**: ✅ 已完成  
**测试状态**: ⏳ 待验证

