# 合同分解超时问题修复指南

## 问题描述

在处理大型合同（包含多个箱包和大量组件）的工艺分解时，前端可能会因为超时而显示失败，但实际上后端已成功完成分解并保存数据。

## 问题原因

1. **处理时间长**: 大规模合同分解需要较长处理时间
2. **前端超时**: 浏览器或HTTP客户端的默认超时设置（通常30-60秒）
3. **连接断开**: 超时后前端主动断开连接，无法接收后端的成功响应

## 解决方案

### 方案 1：增加前端超时时间（推荐快速修复）

修改前端的HTTP请求超时配置：

#### 文件：`frontend/src/api/request.js` 或 `frontend/src/utils/request.js`

找到 Axios 配置，增加超时时间：

```javascript
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 300000 // 从默认值改为 5分钟（300秒）
})
```

### 方案 2：改为异步处理模式（推荐长期方案）

将合同分解改为异步处理，立即返回任务ID，前端轮询查询进度。

#### 2.1 后端实现

**新增异步分解接口**：

```java
@PostMapping("/breakdown/contract/{contractId}/async")
public ResponseEntity<Map<String, Object>> breakdownContractAsync(@PathVariable Long contractId) {
    String taskId = UUID.randomUUID().toString();
    
    // 异步执行分解
    CompletableFuture.runAsync(() -> {
        try {
            breakdownService.breakdownContract(contractId);
            // 保存任务状态为成功
            taskStatusService.updateStatus(taskId, "COMPLETED");
        } catch (Exception e) {
            // 保存任务状态为失败
            taskStatusService.updateStatus(taskId, "FAILED", e.getMessage());
        }
    });
    
    Map<String, Object> response = new HashMap<>();
    response.put("taskId", taskId);
    response.put("status", "PROCESSING");
    response.put("message", "分解任务已提交，正在处理中");
    
    return ResponseEntity.ok(response);
}

@GetMapping("/breakdown/task/{taskId}/status")
public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
    // 查询任务状态
    TaskStatus status = taskStatusService.getStatus(taskId);
    return ResponseEntity.ok(Map.of(
        "taskId", taskId,
        "status", status.getStatus(),
        "progress", status.getProgress(),
        "message", status.getMessage()
    ));
}
```

#### 2.2 前端实现

```javascript
// 提交分解任务
async function startBreakdown(contractId) {
  try {
    const response = await axios.post(`/api/breakdown/contract/${contractId}/async`)
    const taskId = response.data.taskId
    
    // 开始轮询任务状态
    pollTaskStatus(taskId)
    
    // 显示进度提示
    ElMessage.info('分解任务已提交，正在处理中...')
  } catch (error) {
    ElMessage.error('提交分解任务失败')
  }
}

// 轮询任务状态
function pollTaskStatus(taskId) {
  const interval = setInterval(async () => {
    try {
      const response = await axios.get(`/api/breakdown/task/${taskId}/status`)
      const status = response.data.status
      
      if (status === 'COMPLETED') {
        clearInterval(interval)
        ElMessage.success('合同分解完成！')
        // 刷新数据
        refreshData()
      } else if (status === 'FAILED') {
        clearInterval(interval)
        ElMessage.error(`分解失败: ${response.data.message}`)
      } else {
        // 更新进度显示
        updateProgress(response.data.progress)
      }
    } catch (error) {
      clearInterval(interval)
      ElMessage.error('查询任务状态失败')
    }
  }, 2000) // 每2秒查询一次
}
```

### 方案 3：添加进度反馈（最佳用户体验）

在分解过程中实时反馈进度，使用 WebSocket 或 Server-Sent Events (SSE)。

#### 3.1 使用 SSE 实现实时进度

**后端**：

```java
@GetMapping(value = "/breakdown/contract/{contractId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter breakdownContractWithProgress(@PathVariable Long contractId) {
    SseEmitter emitter = new SseEmitter(5L * 60 * 1000); // 5分钟超时
    
    CompletableFuture.runAsync(() -> {
        try {
            // 发送开始事件
            emitter.send(SseEmitter.event()
                .name("progress")
                .data(Map.of("stage", "开始", "percent", 0)));
            
            // 执行分解，并在关键步骤发送进度
            breakdownService.breakdownContractWithProgress(contractId, (stage, percent) -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(Map.of("stage", stage, "percent", percent)));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            });
            
            // 发送完成事件
            emitter.send(SseEmitter.event()
                .name("complete")
                .data(Map.of("message", "分解完成")));
            emitter.complete();
            
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });
    
    return emitter;
}
```

**前端**：

```javascript
function startBreakdownWithProgress(contractId) {
  const eventSource = new EventSource(`/api/breakdown/contract/${contractId}/stream`)
  
  eventSource.addEventListener('progress', (event) => {
    const data = JSON.parse(event.data)
    console.log(`进度: ${data.stage} - ${data.percent}%`)
    // 更新进度条
    updateProgressBar(data.percent, data.stage)
  })
  
  eventSource.addEventListener('complete', (event) => {
    eventSource.close()
    ElMessage.success('合同分解完成！')
    refreshData()
  })
  
  eventSource.addEventListener('error', (event) => {
    eventSource.close()
    ElMessage.error('分解过程中出现错误')
  })
}
```

## 临时解决方法（当前可用）

### 用户操作指南

当遇到"分解失败"的提示时：

1. **不要重复点击分解按钮**
2. **等待1-2分钟后刷新页面**
3. **检查合同状态是否已变为"已完成"**
4. **查看分解结果是否已生成**

### 验证方法

通过以下SQL查询验证分解是否真的成功：

```sql
-- 查询合同状态
SELECT id, contract_no, status FROM contracts WHERE id = <合同ID>;
-- status = 2 表示已完成

-- 查询分解记录数量
SELECT COUNT(*) as breakdown_count 
FROM container_components_breakdown 
WHERE container_id IN (
    SELECT id FROM containers WHERE contract_id = <合同ID>
);
```

## 实施建议

### 短期（立即实施）

✅ **方案 1**: 增加前端超时时间到5分钟
- 实施难度: ⭐ 简单
- 修改文件: 1个
- 风险: 低

### 中期（1-2周内）

✅ **方案 2**: 实现异步处理模式
- 实施难度: ⭐⭐⭐ 中等
- 需要: 新增任务表、新增API接口、修改前端逻辑
- 优势: 更好的用户体验，不会阻塞浏览器

### 长期（1个月内）

✅ **方案 3**: 添加实时进度反馈
- 实施难度: ⭐⭐⭐⭐ 较高
- 需要: SSE/WebSocket实现、进度计算逻辑、前端进度UI
- 优势: 最佳用户体验，用户可实时看到处理进度

## 配置建议

### 建议的超时配置

| 合同规模 | 箱包数 | 建议超时时间 |
|---------|--------|-------------|
| 小型 | 1-10 | 60秒 |
| 中型 | 11-30 | 180秒（3分钟）|
| 大型 | 31-50 | 300秒（5分钟）|
| 超大型 | 50+ | 600秒（10分钟）或异步处理 |

## 性能优化建议

1. **批量插入**: 使用批量SQL插入减少数据库往返
2. **缓存优化**: 预加载常用组件数据到缓存
3. **并行处理**: 对独立的箱包进行并行分解
4. **索引优化**: 确保关键查询字段有合适的索引
5. **连接池**: 调整数据库连接池大小

## 监控和日志

添加关键指标监控：

```java
@Slf4j
public class BreakdownServiceImpl {
    
    @Override
    @Transactional
    public Map<String, Object> breakdownContract(Long contractId) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始合同分解: contractId={}", contractId);
        
        try {
            // 分解逻辑
            Map<String, Object> result = doBreakdown(contractId);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("合同分解完成: contractId={}, 耗时={}ms", contractId, duration);
            
            // 记录性能指标
            if (duration > 60000) {
                log.warn("合同分解耗时超过1分钟: contractId={}, 耗时={}ms", contractId, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("合同分解失败: contractId={}, 耗时={}ms, error={}", 
                contractId, duration, e.getMessage(), e);
            throw e;
        }
    }
}
```

## 常见问题

### Q1: 为什么后端显示成功但前端显示失败？
A: 这是因为处理时间超过了前端的超时设置，前端在收到响应前就断开了连接。

### Q2: 如何判断分解是否真的成功？
A: 查看后端日志中的"合同工艺分解完成"日志，或直接查询数据库中的合同状态。

### Q3: 是否需要重新分解？
A: 不需要！如果日志显示"分解完成"且数据库中有分解记录，说明分解已成功，只是响应未送达。

### Q4: 如何避免重复分解？
A: 实施方案2（异步处理），可以在提交任务前检查是否已有进行中的任务。

## 总结

当前问题不是真正的分解失败，而是前端超时导致的响应接收失败。建议：

1. **立即**: 修改前端超时配置为5分钟
2. **本周**: 实施异步处理模式
3. **下月**: 添加实时进度反馈

这样可以提供更好的用户体验，避免用户误以为分解失败而重复操作。

