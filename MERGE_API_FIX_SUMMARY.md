# 合并分解表API修复总结

## 问题分析

用户反馈PDF文件无法打开，经过分析发现合同详情页面的"下载工艺分解合并表"功能应该使用合并分解表API (`/api/breakdown/merge`)，而不是导出API (`/api/breakdown/contract/{contractId}/export`)。

## 修复方案

### 1. 修改前端逻辑

**ContractDetail.vue**：
- 添加 `breakdownApi` 导入
- 修改 `downloadBreakdownTable` 函数，使用合并分解表API
- 获取合同的所有箱包，筛选已分解的箱包
- 调用 `breakdownApi.mergeBreakdownTables(containerIds)`
- 使用返回的 `downloadUrl` 直接打开PDF

### 2. API调用流程

```javascript
const downloadBreakdownTable = async () => {
  try {
    const contractId = route.params.id
    
    // 获取该合同的所有箱包
    const containers = await contractsApi.getContractContainers(contractId)
    
    // 筛选出已分解的箱包
    const decomposedContainers = containers.filter(container => container.status === 1)
    
    // 调用合并分解表功能
    const containerIds = decomposedContainers.map(container => container.id)
    const response = await breakdownApi.mergeBreakdownTables(containerIds)
    
    if (response.success) {
      // 直接在新窗口中打开PDF文件
      window.open(response.downloadUrl, '_blank')
    }
  } catch (error) {
    ElMessage.error('合并分解表失败')
  }
}
```

## API测试结果

### 1. 合并分解表API测试
```bash
POST http://localhost:8080/api/breakdown/merge
Body: {"containerIds": [1,2]}
```

**响应结果**：
```json
{
  "success": true,
  "downloadUrl": "http://localhost:8080/api/breakdown/merged/1/download/Test001_%E5%90%88%E5%B9%B6%E5%88%86%E8%A7%A3%E8%A1%A8.pdf",
  "totalContainers": 2,
  "totalComponents": 48,
  "totalProblems": 2,
  "message": "合并分解表成功"
}
```

### 2. PDF下载URL测试
```bash
GET http://localhost:8080/api/breakdown/merged/1/download/Test001_%E5%90%88%E5%B9%B6%E5%88%86%E8%A7%A3%E8%A1%A8.pdf
```

**响应结果**：
- 状态码：200
- 内容类型：application/pdf
- 内容长度：54245字节
- PDF内容正常返回

## URL格式确认

### 正确的URL格式
```
http://localhost:8080/api/breakdown/merged/{contractId}/download/{encodedFileName}
```

### 实际示例
```
http://localhost:8080/api/breakdown/merged/1/download/Test001_%E5%90%88%E5%B9%B6%E5%88%86%E8%A7%A3%E8%A1%A8.pdf
```

### URL组成部分
- **协议**：http
- **主机**：localhost:8080
- **路径**：/api/breakdown/merged/1/download/
- **文件名**：Test001_合并分解表.pdf（URL编码后）

## 功能特点

### 1. 正确的API使用
- **合同详情页面**：使用合并分解表API
- **分解页面**：使用合并分解表API
- **一致性**：两个页面使用相同的API和逻辑

### 2. URL生成逻辑
- **后端生成**：使用合同号生成文件名
- **URL编码**：正确处理中文文件名
- **绝对路径**：包含完整的URL信息

### 3. 用户体验
- **即时打开**：直接在新窗口中打开PDF
- **文件名可见**：URL中包含文件名信息
- **错误处理**：完善的错误提示机制

## 部署状态

### 修复完成
- ✅ **前端修改**：ContractDetail.vue 已修改为使用合并分解表API
- ✅ **API导入**：添加了 breakdownApi 导入
- ✅ **前端构建**：npm build 成功
- ✅ **前端部署**：Docker镜像重新构建和部署
- ✅ **API测试**：合并分解表API和PDF下载URL都正常工作

### 服务状态
```
NAME           IMAGE                 STATUS          PORTS
mms-backend    mms-backend:latest    Up             0.0.0.0:8080->8080/tcp
mms-frontend   mms-frontend:latest   Up             0.0.0.0:9000->80/tcp
mms-mariadb    mariadb:11            Up             0.0.0.0:3307->3306/tcp
mms-redis      redis:6.0-alpine      Up             0.0.0.0:6379->6379/tcp
```

## 测试建议

### 功能测试
1. **合同详情页面**：点击"下载工艺分解合并表"按钮
2. **分解页面**：点击"合并分解表"按钮
3. **PDF打开**：验证PDF文件在新窗口中正常打开
4. **URL格式**：验证URL格式正确

### 预期结果
- PDF文件在新窗口中正常打开
- URL格式为：`http://localhost:8080/api/breakdown/merged/{contractId}/download/{fileName}`
- 文件名包含合同号，如：`Test001_合并分解表.pdf`

## 总结

成功修复了合同详情页面的PDF下载功能：

1. **API选择正确**：使用合并分解表API而不是导出API
2. **逻辑统一**：与分解页面使用相同的逻辑
3. **URL格式正确**：生成正确的URL格式
4. **功能验证**：API和PDF下载都正常工作
5. **部署完成**：前端已重新部署

现在合同详情页面的"下载工艺分解合并表"功能应该能够正常工作，PDF文件会在新窗口中打开，URL格式也符合预期！
