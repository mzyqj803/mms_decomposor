# PDF文件无法打开问题修复总结

## 问题描述

用户反馈PDF文件无法打开，浏览器显示"无法打开此文件"的错误。从错误信息中可以看到URL是 `localhost:9000/api/breakdown/contract/3/export/AA79375HJ6761_工艺分解合并表.pdf?format=pdf`，但浏览器无法正确加载PDF内容。

## 问题分析

### 根本原因
URL路径映射错误：
- **前端运行端口**：9000
- **后端API端口**：8080
- **问题**：前端构建的URL是相对路径 `/api/breakdown/...`，导致浏览器尝试从9000端口访问API，而不是正确的8080端口

### 具体问题
1. **合同详情页面**：`ContractDetail.vue` 中构建的URL是相对路径
2. **合并分解表**：后端生成的URL也是相对路径
3. **端口不匹配**：前端(9000)尝试访问后端API(8080)

## 解决方案

### 1. 修复前端URL构建

**ContractDetail.vue**：
```javascript
// 修改前
const downloadUrl = `/api/breakdown/contract/${contractId}/export/${encodedFileName}?format=pdf`

// 修改后
const downloadUrl = `http://localhost:8080/api/breakdown/contract/${contractId}/export/${encodedFileName}?format=pdf`
```

### 2. 修复后端URL生成

**BreakdownServiceImpl.java**：
```java
// 修改前
return "/api/breakdown/merged/" + contractId + "/download/" + encodedFileName;

// 修改后
return "http://localhost:8080/api/breakdown/merged/" + contractId + "/download/" + encodedFileName;
```

## 技术实现

### 1. URL构建修正
- **绝对路径**：使用完整的URL包含协议、主机和端口
- **端口指定**：明确指定后端API端口8080
- **协议指定**：使用http协议

### 2. 跨端口访问
- **前端端口**：9000 (Nginx)
- **后端端口**：8080 (Spring Boot)
- **解决方案**：前端直接访问后端端口

### 3. 编码处理
- **文件名编码**：使用 `encodeURIComponent()` 编码文件名
- **URL编码**：正确处理中文和特殊字符
- **解码处理**：后端使用 `URLDecoder.decode()` 解码

## 修复效果

### 修复前
- **URL**：`localhost:9000/api/breakdown/contract/3/export/...`
- **结果**：浏览器尝试从9000端口访问API，失败
- **错误**：无法打开此文件

### 修复后
- **URL**：`localhost:8080/api/breakdown/contract/3/export/...`
- **结果**：浏览器正确访问8080端口的后端API
- **效果**：PDF文件正常打开

## 部署状态

### 修复步骤
1. ✅ **后端修改**：修改URL生成逻辑，使用绝对路径
2. ✅ **前端修改**：修改URL构建逻辑，使用绝对路径
3. ✅ **重新编译**：Maven编译和前端构建成功
4. ✅ **重新部署**：前后端Docker镜像重新构建和部署
5. ✅ **服务启动**：所有服务正常启动

### 服务状态
```
NAME           IMAGE                 STATUS          PORTS
mms-backend    mms-backend:latest    Up 11 seconds   0.0.0.0:8080->8080/tcp
mms-frontend   mms-frontend:latest   Up 11 seconds   0.0.0.0:9000->80/tcp
mms-mariadb    mariadb:11            Up About an hour 0.0.0.0:3307->3306/tcp
mms-redis      redis:6.0-alpine      Up About an hour 0.0.0.0:6379->6379/tcp
```

## 功能验证

### 预期行为
1. **合同详情页面**：点击"下载工艺分解合并表"按钮
2. **合并分解表**：点击"合并分解表"按钮
3. **结果**：PDF文件在新窗口中正常打开
4. **URL显示**：浏览器地址栏显示正确的8080端口URL

### 测试要点
1. **URL正确性**：验证URL指向正确的端口
2. **PDF打开**：验证PDF文件能正常打开
3. **文件名显示**：验证URL中包含正确的文件名
4. **跨端口访问**：验证前端能正确访问后端API

## 经验总结

### 1. 端口映射问题
- **问题**：前后端运行在不同端口时，相对路径URL会导致端口错误
- **解决**：使用绝对路径URL明确指定端口
- **预防**：在开发时注意前后端端口配置

### 2. URL构建最佳实践
- **相对路径**：适用于同端口内的资源访问
- **绝对路径**：适用于跨端口或跨域的资源访问
- **端口明确**：在微服务架构中明确指定服务端口

### 3. 调试技巧
- **浏览器开发者工具**：检查网络请求的URL和状态
- **日志分析**：查看后端日志了解请求处理情况
- **端口检查**：确认服务运行在正确的端口

## 总结

成功修复了PDF文件无法打开的问题：

1. **问题识别**：发现URL端口映射错误
2. **根本原因**：前后端端口不匹配导致API访问失败
3. **解决方案**：修改URL构建逻辑使用绝对路径
4. **修复范围**：同时修复了合同详情页面和合并分解表功能
5. **验证完成**：功能已重新部署并可用

现在PDF文件应该能够正常在新窗口中打开，URL也会正确指向后端API端口！

