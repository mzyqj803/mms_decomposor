# 非标组件自动生成功能测试指南

## 测试前准备

### 1. 确保基础组件存在

在测试之前，需要确保系统中存在基础组件。例如：
- 基础组件代码：`TTA0E104002`
- 该组件在 `components` 表中存在
- 该组件有相关的规格和关系数据

### 2. 检查数据库连接

确保以下服务正常运行：
- MySQL 数据库
- Redis 缓存
- 后端服务

## 测试步骤

### 测试场景 1：基础功能测试

#### 步骤 1：准备测试数据

在 `container_components` 表中插入一条包含 `~` 符号的组件记录：

```sql
INSERT INTO container_components 
(Container_ID, Component_No, Component_Name, Quantity, Entry_User, Last_Update_User)
VALUES 
(1, 'TTA0E104002~AA79375', 'Test Non-Standard Component', 5, 'TEST_USER', 'TEST_USER');
```

#### 步骤 2：执行工艺分解

通过 API 执行工艺分解：

```bash
# 分解箱包
curl -X POST http://localhost:8080/api/breakdown/container/1

# 或分解合同
curl -X POST http://localhost:8080/api/breakdown/contract/1
```

#### 步骤 3：验证结果

1. **检查 components 表**

```sql
SELECT * FROM components 
WHERE component_code = 'TTA0E104002~AA79375';
```

预期结果：
- 找到一条新记录
- `component_code` = `TTA0E104002~AA79375`
- 其他属性与基础组件 `TTA0E104002` 相同

2. **检查 components_spec 表**

```sql
SELECT * FROM components_spec 
WHERE component_id = (
    SELECT id FROM components 
    WHERE component_code = 'TTA0E104002~AA79375'
);
```

预期结果：
- 包含基础组件的所有规格记录
- 额外包含一条记录：
  - `spec_code` = `nonStandardPartFlag`
  - `spec_value` = `1`
  - `comments` = `自动生成的非标组件标记`

3. **检查 components_relationship 表**

```sql
SELECT r.*, 
       p.component_code as parent_code,
       c.component_code as child_code
FROM components_relationship r
JOIN components p ON r.Parent_ID = p.id
JOIN components c ON r.Child_ID = c.id
WHERE p.component_code = 'TTA0E104002~AA79375';
```

预期结果：
- 子组件关系与基础组件 `TTA0E104002` 完全相同

4. **检查分解结果**

```bash
# 获取箱包分解结果
curl -X GET http://localhost:8080/api/breakdown/container/1

# 获取合同分解汇总
curl -X GET http://localhost:8080/api/breakdown/contract/1/summary
```

预期结果：
- 非标组件 `TTA0E104002~AA79375` 出现在分解结果中
- 数量正确
- 子组件正确展开

### 测试场景 2：重复创建测试

#### 目的
验证非标组件不会被重复创建

#### 步骤

1. 再次执行工艺分解（使用相同的非标组件代码）
2. 检查日志，应该看到 "非标组件已存在" 的消息
3. 验证数据库中只有一条 `TTA0E104002~AA79375` 的记录

### 测试场景 3：基础组件不存在

#### 目的
验证当基础组件不存在时的错误处理

#### 步骤

1. 准备不存在的基础组件代码：

```sql
INSERT INTO container_components 
(Container_ID, Component_No, Component_Name, Quantity, Entry_User, Last_Update_User)
VALUES 
(1, 'NONEXIST~TEST', 'Non-exist Base Component', 1, 'TEST_USER', 'TEST_USER');
```

2. 执行工艺分解
3. 检查结果：
   - 非标组件不会被创建
   - 该组件会被记录到 `container_components_breakdown_problems` 表
   - 日志中包含错误信息：`基础组件不存在，无法创建非标组件`

### 测试场景 4：多个非标组件

#### 目的
验证批量创建非标组件

#### 步骤

1. 准备多条非标组件数据：

```sql
INSERT INTO container_components 
(Container_ID, Component_No, Component_Name, Quantity, Entry_User, Last_Update_User)
VALUES 
(1, 'TTA0E104002~AA79375', 'Non-Standard Component 1', 5, 'TEST_USER', 'TEST_USER'),
(1, 'TTA0E104002~AA79376', 'Non-Standard Component 2', 3, 'TEST_USER', 'TEST_USER'),
(1, 'TTA0E104002~AA79377', 'Non-Standard Component 3', 2, 'TEST_USER', 'TEST_USER');
```

2. 执行工艺分解
3. 验证所有非标组件都被正确创建

### 测试场景 5：缓存验证

#### 目的
验证缓存机制是否正常工作

#### 步骤

1. 首次创建非标组件（触发数据库插入和缓存写入）
2. 清空分解结果：

```bash
curl -X DELETE http://localhost:8080/api/breakdown/container/1
```

3. 再次执行分解（应该从缓存读取非标组件）
4. 检查日志，应该看到 "从缓存获取零部件" 的消息

## 日志验证

### 成功创建的日志示例

```
INFO  检测到非标组件代码: TTA0E104002~AA79375
INFO  提取基础组件代码: TTA0E104002
INFO  找到基础组件: TTA0E104002, name=测试组件
INFO  创建非标组件成功: id=123, componentCode=TTA0E104002~AA79375
INFO  复制基础组件规格完成: 共3条
INFO  添加非标组件标记成功
INFO  复制基础组件关系完成: 共5条
INFO  非标组件创建完成: componentCode=TTA0E104002~AA79375, baseComponentCode=TTA0E104002, specs=4, relationships=5
```

### 重复创建的日志示例

```
INFO  检测到非标组件代码: TTA0E104002~AA79375
INFO  非标组件已存在: TTA0E104002~AA79375
```

### 基础组件不存在的日志示例

```
INFO  检测到非标组件代码: NONEXIST~TEST
INFO  提取基础组件代码: NONEXIST
ERROR 基础组件不存在，无法创建非标组件: baseComponentCode=NONEXIST
```

## 性能测试

### 测试目标
- 单个非标组件创建时间 < 500ms
- 10个非标组件批量创建时间 < 5s
- 缓存命中率 > 90%（重复查询场景）

### 测试方法

1. 使用 JMeter 或 Postman 进行压力测试
2. 监控日志中的时间戳
3. 使用 Redis Monitor 监控缓存命中情况

## 回滚测试

### 目的
验证事务回滚机制

### 步骤

1. 修改代码，在 `getOrCreateNonStandardComponent()` 方法的复制关系步骤后抛出异常
2. 执行工艺分解
3. 验证：
   - components 表中没有新增记录
   - components_spec 表中没有新增记录
   - components_relationship 表中没有新增记录

## 常见问题排查

### 1. 非标组件没有被创建

检查项：
- 基础组件是否存在
- componentCode 是否真的包含 `~` 符号
- 日志中是否有错误信息

### 2. 规格或关系没有被复制

检查项：
- 基础组件是否真的有规格和关系数据
- 数据库外键约束是否正常
- 日志中的复制数量统计

### 3. 缓存没有生效

检查项：
- Redis 服务是否正常运行
- 缓存配置是否正确
- 日志中是否有缓存写入失败的信息

## 清理测试数据

测试完成后，清理测试数据：

```sql
-- 删除非标组件
DELETE FROM components_relationship 
WHERE Parent_ID IN (
    SELECT id FROM components 
    WHERE component_code LIKE '%~%'
);

DELETE FROM components_spec 
WHERE component_id IN (
    SELECT id FROM components 
    WHERE component_code LIKE '%~%'
);

DELETE FROM components 
WHERE component_code LIKE '%~%';

-- 清理测试的箱包组件
DELETE FROM container_components 
WHERE Component_No LIKE '%~%';

-- 清理分解结果
DELETE FROM container_components_breakdown 
WHERE container_id = 1;
```

## 预期效果

完成测试后，应该能够确认：

1. ✅ 非标组件可以自动创建
2. ✅ 属性、规格、关系都被正确复制
3. ✅ 非标标记被正确添加
4. ✅ 不会重复创建
5. ✅ 基础组件不存在时有正确的错误处理
6. ✅ 缓存机制正常工作
7. ✅ 事务回滚机制正常
8. ✅ 性能满足要求

## 后续优化建议

1. 添加前端界面支持非标组件的查看和管理
2. 添加 API 接口手动创建非标组件
3. 支持批量删除非标组件
4. 添加非标组件的统计报表
5. 优化大批量非标组件创建的性能

