# 紧固件ERP代码查找工具类

## 概述

`FastenerErpCodeFinder` 是一个用于根据 `component_code` 和 `name` 查找对应 `fastenerWarehouse` 中 `ERP_Code` 的工具类。

## 工作流程

1. **检查是否为紧固件**：判断 `component_id` 是否存在于视图 `components_fastener_assembled` 或 `components_fastener_unassembled` 中
2. **解析紧固件信息**：调用 `FastenerParser.parse()` 解析 `component_code` 和 `name`
3. **按优先级匹配**：按照以下顺序匹配 `fastenerWarehouse` 中的记录：
   - `productCode`（产品代码）
   - `specs`（规格）
   - `level`（等级）
   - `surfaceTreatment`（表面处理）
4. **返回结果**：返回匹配的 `ERP_Code`

## 使用方法

### 基本用法

```java
@Autowired
private FastenerErpCodeFinder fastenerErpCodeFinder;

// 查找单个紧固件的ERP代码
ErpCodeResult result = fastenerErpCodeFinder.findErpCode(
    componentId,     // 工件ID
    componentCode,   // 工件代码
    name            // 工件名称
);

if (result.isSuccess()) {
    String erpCode = result.getErpCode();
    System.out.println("找到ERP代码: " + erpCode);
} else {
    System.out.println("查找失败: " + result.getErrorMessage());
}
```

### 批量查找

```java
@Autowired
private FastenerErpCodeService fastenerErpCodeService;

// 准备测试数据
Object[][] testData = {
    {1L, "GB5783-M6*20-8.8Z", "螺栓"},
    {2L, "GB6170-M6-8Z", "螺母"},
    {3L, "FT001", "非紧固件"},
};

// 批量查找
ErpCodeResult[] results = fastenerErpCodeService.batchFindErpCodes(testData);

// 打印结果
fastenerErpCodeService.printResults(results);
```

## 匹配规则

### 1. 产品代码匹配
- 首先查找 `productCode` 是否存在
- 如果不存在，返回"未知物料"错误

### 2. 规格匹配
- 在 `productCode` 匹配的基础上，进一步匹配 `specs`
- 如果匹配不到，继续使用 `productCode` 匹配的结果

### 3. 等级匹配
- 在上一级匹配的基础上，进一步匹配 `level`
- 如果匹配不到，使用上一级匹配的结果

### 4. 表面处理匹配
- 在上一级匹配的基础上，进一步匹配 `surfaceTreatment`
- 如果匹配不到，使用上一级匹配的结果

### 5. 最终结果
- 如果所有匹配都成功，返回最精确匹配的 `ERP_Code`
- 如果部分匹配失败，返回上一级匹配规则到的所有工件中任意一个

## 返回结果

### ErpCodeResult 类

```java
public class ErpCodeResult {
    private Long componentId;                    // 工件ID
    private String componentCode;                 // 工件代码
    private String name;                          // 工件名称
    private String erpCode;                       // ERP代码
    private boolean success;                      // 是否成功
    private String errorMessage;                  // 错误信息
    private String matchedProductCode;            // 匹配的产品代码
    private String matchedSpecs;                  // 匹配的规格
    private String matchedLevel;                  // 匹配的等级
    private String matchedSurfaceTreatment;       // 匹配的表面处理
}
```

### 成功情况

```java
ErpCodeResult result = fastenerErpCodeFinder.findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓");
// result.isSuccess() == true
// result.getErpCode() == "07.0100.00001"
// result.getMatchedProductCode() == "GB5783"
// result.getMatchedSpecs() == "M6*20"
// result.getMatchedLevel() == "8.8"
// result.getMatchedSurfaceTreatment() == "镀锌等"
```

### 失败情况

1. **不是紧固件**
   ```java
   // result.isSuccess() == false
   // result.getErrorMessage() == "不是紧固件，跳过"
   ```

2. **解析错误**
   ```java
   // result.isSuccess() == false
   // result.getErrorMessage() == "解析错误: 具体错误信息"
   ```

3. **未知物料**
   ```java
   // result.isSuccess() == false
   // result.getErrorMessage() == "未知物料: GB9999"
   ```

## 测试用例

### 标准紧固件
- 输入：`componentId=1L, componentCode="GB5783-M6*20-8.8Z", name="螺栓"`
- 期望：成功匹配到对应的ERP代码

### 非紧固件
- 输入：`componentId=2L, componentCode="FT001", name="非紧固件"`
- 期望：返回"不是紧固件，跳过"

### 无效代码
- 输入：`componentId=3L, componentCode="INVALID_CODE", name="无效代码"`
- 期望：返回"解析错误"

### 未知物料
- 输入：`componentId=4L, componentCode="GB9999-M6*20-8.8Z", name="未知物料"`
- 期望：返回"未知物料: GB9999"

## 依赖关系

- `ComponentFastenerRepository`：用于检查是否为紧固件
- `FastenerWarehouseRepository`：用于查询紧固件仓库数据
- `FastenerParser`：用于解析紧固件信息

## 注意事项

1. 确保数据库中存在相应的视图和数据
2. 工具类会自动处理各种异常情况
3. 匹配算法采用渐进式匹配，确保即使部分匹配失败也能返回结果
4. 建议在生产环境中添加适当的日志记录和监控
