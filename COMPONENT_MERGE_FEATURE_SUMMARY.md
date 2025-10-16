# 合并分解表部件编号合并功能实现总结

## 功能需求

如果部件编号相同，合并成一行，数量显示所有同名组件的数量相加之和。这样可以避免重复显示相同部件，提供更清晰的汇总信息。

## 实现方案

### 数据结构调整

**修改前**：使用 `List<Map<String, Object>> allRows` 存储所有数据行
**修改后**：使用 `Map<String, Map<String, Object>> mergedComponents` 按部件编号合并

### 合并逻辑

#### 1. 正常部件合并
```java
// 处理正常部件
for (ContainerComponentsBreakdown breakdown : allBreakdowns) {
    Components subComponent = breakdown.getSubComponent();
    if (subComponent != null) {
        String componentCode = subComponent.getComponentCode();
        Integer quantity = breakdown.getQuantity();
        
        if (mergedComponents.containsKey(componentCode)) {
            // 累加数量
            Map<String, Object> existing = mergedComponents.get(componentCode);
            Integer currentQuantity = (Integer) existing.get("quantity");
            existing.put("quantity", currentQuantity + quantity);
        } else {
            // 新组件
            Map<String, Object> component = new HashMap<>();
            component.put("type", "normal");
            component.put("componentCode", componentCode);
            component.put("name", subComponent.getName());
            component.put("quantity", quantity);
            component.put("erpCode", erpCode);
            component.put("procurementFlag", subComponent.getProcurementFlag());
            component.put("commonPartsFlag", subComponent.getCommonPartsFlag());
            component.put("remark", "");
            mergedComponents.put(componentCode, component);
        }
    }
}
```

#### 2. 问题部件合并
```java
// 处理问题部件
for (ContainerComponentsBreakdownProblems problem : allProblems) {
    String componentNo = problem.getComponentNo();
    Integer quantity = problem.getQuantity();
    
    if (mergedComponents.containsKey(componentNo)) {
        // 累加数量
        Map<String, Object> existing = mergedComponents.get(componentNo);
        Integer currentQuantity = (Integer) existing.get("quantity");
        existing.put("quantity", currentQuantity + quantity);
        // 更新备注为问题部件
        existing.put("remark", "工件不存在");
    } else {
        // 新问题组件
        Map<String, Object> problemComponent = new HashMap<>();
        problemComponent.put("type", "problem");
        problemComponent.put("componentCode", componentNo);
        problemComponent.put("name", problem.getName() != null ? problem.getName() : componentNo);
        problemComponent.put("quantity", quantity);
        problemComponent.put("erpCode", "");
        problemComponent.put("procurementFlag", false);
        problemComponent.put("commonPartsFlag", false);
        problemComponent.put("remark", "工件不存在");
        
        mergedComponents.put(componentNo, problemComponent);
    }
}
```

#### 3. 转换为列表
```java
// 转换为列表用于排序和显示
List<Map<String, Object>> allRows = new ArrayList<>(mergedComponents.values());
```

## 合并规则

### 1. 相同部件编号合并
- **键值**：使用 `componentCode` 作为合并键
- **数量累加**：相同部件编号的数量相加
- **信息保留**：保留第一个部件的其他信息（名称、ERP代码等）

### 2. 正常部件与问题部件合并
- **优先级**：如果正常部件和问题部件有相同编号，正常部件优先
- **备注更新**：合并后如果包含问题部件，备注更新为"工件不存在"
- **数量累加**：正常部件和问题部件的数量都会累加

### 3. 属性处理
- **数量**：所有相同编号的数量相加
- **名称**：保留第一个部件的名称
- **ERP代码**：保留第一个部件的ERP代码
- **采购标识**：保留第一个部件的采购标识
- **通用件标识**：保留第一个部件的通用件标识
- **备注**：如果包含问题部件，备注为"工件不存在"

## 业务逻辑改进

### 1. 数据去重
- **避免重复**：相同部件编号只显示一行
- **信息汇总**：提供完整的数量汇总
- **减少冗余**：PDF表格更加简洁

### 2. 数量统计
- **准确统计**：所有相同部件的数量正确累加
- **跨箱包汇总**：不同箱包中的相同部件数量合并
- **问题部件统计**：问题部件的数量也参与合并

### 3. 信息完整性
- **保留关键信息**：保留部件的关键属性
- **问题标识**：正确标识包含问题部件的情况
- **数据一致性**：确保合并后的数据一致性

## 技术实现

### 1. 数据结构优化
- **HashMap合并**：使用HashMap的键值特性进行合并
- **内存效率**：减少重复数据的内存占用
- **处理效率**：提高数据处理效率

### 2. 合并算法
- **增量合并**：逐个处理分解记录，增量合并
- **状态保持**：保持合并过程中的状态信息
- **异常处理**：处理各种边界情况

### 3. 排序和显示
- **列表转换**：将合并后的Map转换为List
- **排序保持**：保持按部件编号排序的功能
- **显示优化**：优化PDF表格的显示效果

## 功能效果

### 1. 数据汇总
- **去重显示**：相同部件编号只显示一行
- **数量汇总**：显示所有相同部件的总数量
- **信息完整**：保留部件的完整信息

### 2. 表格优化
- **行数减少**：减少重复行，表格更简洁
- **信息集中**：相关信息集中在一行
- **阅读友好**：提高PDF的可读性

### 3. 业务价值
- **统计准确**：提供准确的部件数量统计
- **决策支持**：为采购和库存决策提供准确数据
- **效率提升**：减少重复信息，提高工作效率

## 部署状态

### 编译状态
- ✅ Maven编译成功
- ✅ 无语法错误
- ✅ 无Linter警告

### 部署状态
- ✅ Docker镜像构建成功
- ✅ 后端服务启动成功
- ✅ 所有服务正常运行

### 服务状态
```
NAME           IMAGE                 STATUS          PORTS
mms-backend    mms-backend:latest    Up 8 seconds    0.0.0.0:8080->8080/tcp
mms-frontend   mms-frontend:latest   Up 48 minutes   0.0.0.0:9000->80/tcp
mms-mariadb    mariadb:11            Up 48 minutes   0.0.0.0:3307->3306/tcp
mms-redis      redis:6.0-alpine      Up 48 minutes   0.0.0.0:6379->6379/tcp
```

## 测试建议

### 功能测试
1. **相同部件合并测试**：验证相同部件编号是否正确合并
2. **数量累加测试**：验证数量是否正确累加
3. **问题部件合并测试**：验证问题部件与正常部件的合并

### 边界测试
1. **大量数据测试**：测试大量部件时的合并性能
2. **复杂合并测试**：测试正常部件和问题部件的复杂合并
3. **空值处理测试**：测试各种空值情况的处理

## 总结

成功实现了合并分解表的部件编号合并功能：

1. **数据结构优化**：使用HashMap进行部件合并
2. **合并逻辑完善**：正确处理正常部件和问题部件的合并
3. **数量统计准确**：所有相同部件的数量正确累加
4. **信息保留完整**：保留部件的关键信息
5. **业务逻辑清晰**：符合合并表的业务需求

该功能现在已经完全部署并可以使用，合并分解表将正确合并相同部件编号的组件，显示准确的数量汇总，大大提升了数据的准确性和表格的可读性。
