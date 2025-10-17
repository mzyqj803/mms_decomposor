# 非标组件自动生成功能实现总结

## 功能概述

在工艺分解过程中，当遇到包含 `~` 符号的组件代码时，系统会自动创建非标组件。非标组件以基础组件为模板，复制所有属性、规格和关系，并在 `components_spec` 表中添加特殊标记。

## 实现原理

### 1. 触发条件

当在工艺分解时遇到包含 `~` 符号的 `componentCode` 时，例如：
- `TTA0E104002~AA79375`
- 基础组件：`TTA0E104002`
- 非标后缀：`AA79375`

### 2. 自动创建逻辑

系统会自动执行以下步骤：

1. **检测非标组件代码**
   - 在 `getComponentByCode()` 方法中检测 `~` 符号
   - 如果检测到，调用 `getOrCreateNonStandardComponent()` 方法

2. **检查是否已存在**
   - 首先检查非标组件是否已经在数据库中存在
   - 如果存在，直接返回

3. **提取基础组件代码**
   - 从非标代码中提取 `~` 符号前的部分
   - 例如：从 `TTA0E104002~AA79375` 提取 `TTA0E104002`

4. **查找基础组件**
   - 在数据库中查找基础组件
   - 如果基础组件不存在，返回空值（将被记录为问题组件）

5. **创建非标组件**
   - 创建新的 `Components` 实体
   - 复制基础组件的所有属性：
     - `componentCode`：使用完整的非标代码（如 `TTA0E104002~AA79375`）
     - `name`：从基础组件复制
     - `categoryCode`：从基础组件复制
     - `comment`：从基础组件复制
     - `procurementFlag`：从基础组件复制
     - `commonPartsFlag`：从基础组件复制

6. **复制规格信息**
   - 从 `components_spec` 表复制基础组件的所有规格
   - 保持 `spec_code` 和 `spec_value` 不变

7. **添加非标标记**
   - 在 `components_spec` 表中添加新记录：
     - `spec_code` = `nonStandardPartFlag`
     - `spec_value` = `1`
     - `comments` = `自动生成的非标组件标记`

8. **复制组件关系**
   - 从 `components_relationship` 表复制基础组件的所有子组件关系
   - 保持数量关系不变

9. **缓存管理**
   - 将新创建的非标组件存储到 Redis 缓存中
   - 后续查询可以直接从缓存获取

## 代码修改

### 1. BreakdownServiceImpl.java

#### 新增依赖
```java
private final ComponentsSpecRepository componentsSpecRepository;
```

#### 修改 getComponentByCode() 方法
```java
private Optional<Components> getComponentByCode(String componentCode) {
    // 检查是否为非标组件（包含~符号）
    if (componentCode != null && componentCode.contains("~")) {
        return getOrCreateNonStandardComponent(componentCode);
    }
    // ... 原有逻辑
}
```

#### 新增 getOrCreateNonStandardComponent() 方法
```java
@Transactional
private Optional<Components> getOrCreateNonStandardComponent(String nonStandardCode) {
    // 1. 检查非标组件是否已存在
    // 2. 提取基础组件代码
    // 3. 查找基础组件
    // 4. 创建非标组件并复制所有属性
    // 5. 复制规格信息
    // 6. 添加非标标记
    // 7. 复制组件关系
    // 8. 更新缓存
}
```

## 数据库影响

### Components 表
- 新增记录：非标组件（如 `TTA0E104002~AA79375`）
- 字段值从基础组件复制

### Components_Spec 表
- 复制基础组件的所有规格记录
- 新增非标标记记录：
  ```
  component_id: <新组件ID>
  spec_code: nonStandardPartFlag
  spec_value: 1
  comments: 自动生成的非标组件标记
  ```

### Components_Relationship 表
- 复制基础组件的所有子组件关系
- parent_id: 新的非标组件ID
- child_id: 与基础组件相同
- quantity: 与基础组件相同

## 使用场景

1. **工艺分解自动创建**
   - 在箱包分解时，如果 `container_components` 表中的 `component_no` 包含 `~` 符号
   - 系统会自动创建对应的非标组件

2. **一次性创建**
   - 每个非标组件只会被创建一次
   - 后续分解会直接使用已创建的非标组件

3. **基于模板**
   - 非标组件完全继承基础组件的属性
   - 可以被识别为非标组件（通过 `nonStandardPartFlag` 标记）

## 日志记录

系统会记录以下关键步骤：
- 检测到非标组件代码
- 提取基础组件代码
- 找到基础组件
- 创建非标组件成功
- 复制规格完成
- 添加非标标记成功
- 复制关系完成
- 非标组件创建完成（包含统计信息）

## 错误处理

1. **基础组件不存在**
   - 记录错误日志
   - 返回空值
   - 该非标组件会被记录为问题组件

2. **创建过程异常**
   - 捕获异常并记录详细日志
   - 返回空值
   - 事务回滚，不会产生脏数据

## 优势

1. **自动化**：无需手动创建非标组件
2. **一致性**：保证非标组件与基础组件的属性一致
3. **可追溯**：通过 `nonStandardPartFlag` 标记可以识别非标组件
4. **高效**：使用缓存机制，提高查询效率
5. **安全**：使用事务管理，保证数据一致性

## 注意事项

1. 非标组件代码必须包含 `~` 符号
2. `~` 符号前的部分必须是系统中已存在的基础组件代码
3. 如果基础组件不存在，非标组件无法创建
4. 非标组件创建后，其关系和规格是独立的，修改基础组件不会影响已创建的非标组件

## 测试建议

1. **正常场景**
   - 基础组件存在，创建非标组件成功
   - 验证所有属性、规格、关系都被正确复制

2. **边界场景**
   - 基础组件不存在
   - 非标组件已存在
   - `~` 符号在开头或结尾

3. **性能测试**
   - 大量非标组件创建
   - 缓存命中率验证

## 版本信息

- 实现日期：2025-10-17
- 修改文件：`src/main/java/com/mms/service/impl/BreakdownServiceImpl.java`
- 涉及表：`components`, `components_spec`, `components_relationship`

