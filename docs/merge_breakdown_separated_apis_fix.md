# 合并分解表功能拆分 - 2025-10-24

## 问题描述

在工艺分解页面，用户勾选部分箱包后点击"合并分解表"按钮，系统应该只生成选中箱包的分解表，但实际却生成了所有箱包的分解表。

### 问题截图分析
用户在工艺分解页面勾选了部分箱包（例如前两个箱包），但生成的PDF包含了该合同下所有箱包的数据。

## 问题原因

### 代码分析

1. **`mergeBreakdownTables` 方法**（正确）：
   ```java
   // 在这个方法中，正确过滤了选中的箱包
   List<Long> longContainerIds = containerIds.stream().map(Integer::longValue).collect(...);
   List<ContainerComponentsBreakdown> filteredBreakdowns = allBreakdowns.stream()
       .filter(breakdown -> longContainerIds.contains(breakdown.getContainer().getId()))
       .collect(...);
   ```

2. **PDF下载URL生成**（问题所在）：
   ```java
   // 原代码生成的URL只包含contractId
   return "/api/breakdown/merged/" + contractId + "/download/" + encodedFileName;
   ```

3. **`generateMergedBreakdownPdf` 方法**（根本原因）：
   ```java
   // 这个方法只接收contractId，重新查询了所有箱包的数据
   public byte[] generateMergedBreakdownPdf(Long contractId) {
       List<ContainerComponentsBreakdown> allBreakdowns = breakdownRepository.findByContractId(contractId);
       // ← 这里查询了所有箱包，忽略了用户的选择
   }
   ```

### 为什么会出现这个问题？

1. `mergeBreakdownTables` 正确地只处理了选中的箱包
2. 但生成的下载URL只包含 `contractId`，丢失了 `containerIds` 信息
3. 当用户点击下载时，后端 `generateMergedBreakdownPdf` 方法只能根据 `contractId` 查询，因此获取了所有箱包

### 同时存在的另一个需求

合同详情页面也有"下载全部分解表"功能，需要下载该合同的所有箱包数据。这个功能也使用了相同的API，因此不能简单修改。

## 解决方案

### 设计思路

将功能拆分为两个独立的API：

1. **全部箱包分解表**（合同详情页使用）：
   - API: `GET /api/breakdown/merged/{contractId}/download`
   - 功能: 下载该合同的所有箱包分解表
   - 不传递 `containerIds` 参数

2. **选中箱包分解表**（工艺分解页使用）：
   - API: `GET /api/breakdown/merged/selected/download?contractId={id}&containerIds={ids}`
   - 功能: 只下载选中箱包的分解表
   - 通过查询参数传递 `containerIds`

### 代码修改

#### 1. Service接口 (`BreakdownService.java`)

添加重载方法：

```java
/**
 * 生成合并分解表PDF（全部箱包）
 * @param contractId 合同ID
 * @return PDF文件字节数组
 */
byte[] generateMergedBreakdownPdf(Long contractId);

/**
 * 生成合并分解表PDF（选中箱包）
 * @param contractId 合同ID
 * @param containerIds 箱包ID列表
 * @return PDF文件字节数组
 */
byte[] generateMergedBreakdownPdf(Long contractId, List<Long> containerIds);
```

#### 2. Service实现 (`BreakdownServiceImpl.java`)

**a) 修改URL生成方法**：
```java
private String generateMergedBreakdownPdfUrl(Long contractId, List<Long> containerIds, ...) {
    // 将containerIds编码为查询参数
    String containerIdsParam = containerIds.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));
    
    // 使用新的API端点
    return "/api/breakdown/merged/selected/download?contractId=" + contractId + 
           "&containerIds=" + containerIdsParam + "&fileName=" + encodedFileName;
}
```

**b) 添加全部箱包版本的方法**：
```java
@Override
public byte[] generateMergedBreakdownPdf(Long contractId) {
    log.info("生成合并分解表PDF(全部箱包): contractId={}", contractId);
    // 调用带containerIds参数的版本，传null表示全部箱包
    return generateMergedBreakdownPdf(contractId, null);
}
```

**c) 修改选中箱包版本的方法**：
```java
@Override
public byte[] generateMergedBreakdownPdf(Long contractId, List<Long> containerIds) {
    // 查询所有分解数据
    List<ContainerComponentsBreakdown> allBreakdowns = breakdownRepository.findByContractId(contractId);
    List<ContainerComponentsBreakdownProblems> allProblems = problemsRepository.findByContractId(contractId);
    
    // 如果指定了containerIds，则只保留这些箱包的数据
    if (containerIds != null && !containerIds.isEmpty()) {
        allBreakdowns = allBreakdowns.stream()
            .filter(breakdown -> containerIds.contains(breakdown.getContainer().getId()))
            .collect(Collectors.toList());
            
        allProblems = allProblems.stream()
            .filter(problem -> containerIds.contains(problem.getContainer().getId()))
            .collect(Collectors.toList());
    }
    
    // ... 后续处理
}
```

#### 3. Controller (`BreakdownController.java`)

**a) 保留原有API（全部箱包）**：
```java
/**
 * 下载合并分解表PDF（全部箱包）- 用于合同详情页
 */
@GetMapping("/merged/{contractId}/download")
public ResponseEntity<byte[]> downloadMergedBreakdownPdf(@PathVariable Long contractId) {
    byte[] pdfBytes = breakdownService.generateMergedBreakdownPdf(contractId);
    // ... 返回PDF
}

@GetMapping("/merged/{contractId}/download/{fileName}")
public ResponseEntity<byte[]> downloadMergedBreakdownPdfWithFileName(
        @PathVariable Long contractId, 
        @PathVariable String fileName) {
    byte[] pdfBytes = breakdownService.generateMergedBreakdownPdf(contractId);
    // ... 返回PDF
}
```

**b) 添加新API（选中箱包）**：
```java
/**
 * 下载选中箱包的合并分解表PDF - 用于工艺分解页
 */
@GetMapping("/merged/selected/download")
public ResponseEntity<byte[]> downloadSelectedContainersBreakdownPdf(
        @RequestParam Long contractId,
        @RequestParam String containerIds,
        @RequestParam(required = false) String fileName) {
    
    // 解析containerIds（逗号分隔）
    List<Long> containerIdList = Arrays.stream(containerIds.split(","))
        .map(String::trim)
        .map(Long::parseLong)
        .collect(Collectors.toList());
    
    // 调用带containerIds参数的方法
    byte[] pdfBytes = breakdownService.generateMergedBreakdownPdf(contractId, containerIdList);
    
    // ... 返回PDF
}
```

## API对比

### 原有API（保持不变）
```
GET /api/breakdown/merged/{contractId}/download
GET /api/breakdown/merged/{contractId}/download/{fileName}
```
- **用途**: 合同详情页下载全部分解表
- **行为**: 生成该合同的所有箱包的分解表
- **参数**: 只需要 contractId

### 新增API
```
GET /api/breakdown/merged/selected/download?contractId={id}&containerIds={ids}&fileName={name}
```
- **用途**: 工艺分解页下载选中箱包的分解表
- **行为**: 只生成选中箱包的分解表
- **参数**: 
  - `contractId`: 合同ID
  - `containerIds`: 逗号分隔的箱包ID列表（例如: "1,2,3"）
  - `fileName`: 可选的文件名

## 测试场景

### 场景1：合同详情页 - 下载全部分解表
1. 打开合同详情页
2. 点击"下载全部分解表"按钮
3. **预期**: 生成包含该合同所有箱包的PDF
4. **使用API**: `GET /api/breakdown/merged/{contractId}/download`

### 场景2：工艺分解页 - 下载选中箱包
1. 打开工艺分解页
2. 勾选部分箱包（例如：箱包1和箱包2）
3. 点击"合并分解表"按钮
4. **预期**: 只生成包含箱包1和箱包2的PDF
5. **使用API**: `GET /api/breakdown/merged/selected/download?contractId=8&containerIds=1,2`

### 场景3：工艺分解页 - 全选后下载
1. 打开工艺分解页
2. 勾选所有箱包
3. 点击"合并分解表"按钮
4. **预期**: 生成包含所有箱包的PDF（与场景1相同，但走不同的API）
5. **使用API**: `GET /api/breakdown/merged/selected/download?contractId=8&containerIds=1,2,3,...,31`

## 关键改进

1. **URL包含完整信息**: 
   - 旧: `/merged/{contractId}/download` - 丢失了选中信息
   - 新: `/merged/selected/download?contractId=8&containerIds=1,2,3` - 保留了选中信息

2. **方法重载**: 使用Java方法重载，保持向后兼容性

3. **条件过滤**: 
   ```java
   if (containerIds != null && !containerIds.isEmpty()) {
       // 只保留选中的箱包
   }
   // 否则保留所有箱包
   ```

4. **独立API端点**: 两个功能使用不同的URL路径，清晰明确

## 部署说明

### 1. 编译
```bash
mvn clean package -DskipTests
```

### 2. 重建镜像
```bash
docker-compose down
docker-compose build --no-cache backend
```

### 3. 启动服务
```bash
docker-compose up -d
```

### 4. 验证
```bash
# 检查日志
docker-compose logs backend --tail=50

# 测试全部箱包API
curl "http://localhost:8080/api/breakdown/merged/8/download"

# 测试选中箱包API
curl "http://localhost:8080/api/breakdown/merged/selected/download?contractId=8&containerIds=1,2,3"
```

## 注意事项

1. **前端无需修改**: 前端的 `mergeBreakdownTables` 调用返回的 `downloadUrl` 已经是正确的新API地址

2. **向后兼容**: 原有的合同详情页功能完全不受影响

3. **日志区分**: 
   - "生成合并分解表PDF(全部箱包)" - 全部箱包
   - "生成合并分解表PDF(选中箱包)" - 选中箱包

4. **参数格式**: `containerIds` 使用逗号分隔，例如: "1,2,3,4,5"

## 相关文件

- `src/main/java/com/mms/service/BreakdownService.java` - Service接口
- `src/main/java/com/mms/service/impl/BreakdownServiceImpl.java` - Service实现
- `src/main/java/com/mms/controller/BreakdownController.java` - Controller

## 状态: ✅ 已完成

修改已完成并部署，两个功能现在独立工作，互不干扰。

