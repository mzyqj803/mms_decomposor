# 修复同一零部件下重复规格参数问题

## 问题描述

在 `components_spec` 表中，同一个零部件（Component_ID）存在多条相同的规格代码（Spec_Code）记录。

**示例：**
```
Component_ID | Spec_Code | Spec_Value
-------------|-----------|------------
10           | unit      | 套
10           | unit      | 套         ← 重复
10           | material  | Q235A
10           | material  | Q235A      ← 重复
```

**原因分析：**
1. `components_spec` 表缺少组合唯一约束 `(Component_ID, Spec_Code)`
2. 数据导入时可能重复插入了相同的规格参数
3. 应用层面没有检查重复

**影响：**
- 零部件详情页面显示重复的规格参数
- 数据冗余，占用不必要的存储空间
- 可能导致业务逻辑混乱

## 解决方案

### 方案一：使用批处理脚本（推荐）

**Windows环境：**
```bash
script\fix-duplicate-specs.bat
```

脚本会自动：
1. ✅ 检查数据库容器状态
2. ✅ 备份当前数据库
3. ✅ 执行修复SQL脚本
4. ✅ 清空Redis缓存
5. ✅ 显示修复结果

### 方案二：直接执行SQL脚本

**Docker环境：**
```bash
docker exec -i mms-mariadb mysql -u mms_user -pmms_password mms_db < src/main/resources/sql/data_init/fix_duplicate_component_specs.sql
```

**或在MySQL客户端中：**
```bash
docker exec -it mms-mariadb mysql -u mms_user -pmms_password mms_db
source src/main/resources/sql/data_init/fix_duplicate_component_specs.sql;
```

### 方案三：重新部署（全新环境）

如果是全新环境，修复脚本已添加到docker-compose.yml的初始化脚本列表中：

```bash
# 停止并清理
docker-compose down -v
docker volume rm mms_decomposor_mariadb_data

# 重新启动（会自动执行所有初始化脚本，包括清理重复记录）
docker-compose up -d
```

**初始化脚本执行顺序：**
1. `01-schema.sql` - 创建表结构（**不含**唯一约束，允许导入重复数据）
2. `02-data_init.sql` - 导入初始数据（可能包含重复）
3. `03-09` - 其他数据处理脚本
4. `10-fix_duplicate_specs.sql` - **清理重复的规格参数并添加唯一约束**（新增）

⚠️ **重要**：唯一约束在清理完重复数据后才添加，这样可以确保初始化过程不会因数据重复而失败。

详见：`docs/DOCKER_INIT_SCRIPTS_ORDER.md`

## 修复脚本功能详解

### 1. 查找重复记录
```sql
SELECT 
    Component_ID,
    Spec_Code,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(ID ORDER BY ID) as duplicate_ids
FROM components_spec
GROUP BY Component_ID, Spec_Code
HAVING COUNT(*) > 1;
```

### 2. 保留策略
- 保留每个 `(Component_ID, Spec_Code)` 组合的**第一条记录**（最小ID）
- 删除该组合的其他所有重复记录

### 3. 添加唯一约束
```sql
ALTER TABLE components_spec 
ADD CONSTRAINT uk_component_spec UNIQUE (Component_ID, Spec_Code);
```

## 验证修复结果

### 1. 检查是否还有重复记录

```sql
SELECT 
    Component_ID,
    Spec_Code,
    COUNT(*) as count
FROM components_spec
GROUP BY Component_ID, Spec_Code
HAVING COUNT(*) > 1;
```

**预期结果：** 空结果集（没有重复）

### 2. 检查唯一约束是否添加

```sql
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    TABLE_NAME
FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'mms_db'
AND TABLE_NAME = 'components_spec'
AND CONSTRAINT_NAME = 'uk_component_spec';
```

**预期结果：**
```
+-------------------+-----------------+----------------+
| CONSTRAINT_NAME   | CONSTRAINT_TYPE | TABLE_NAME     |
+-------------------+-----------------+----------------+
| uk_component_spec | UNIQUE          | components_spec|
+-------------------+-----------------+----------------+
```

### 3. 验证TTA4C033002的规格参数

```sql
SELECT 
    cs.Spec_Code,
    cs.Spec_Value,
    COUNT(*) as count
FROM components_spec cs
JOIN components c ON c.ID = cs.Component_ID
WHERE c.Component_Code = 'TTA4C033002'
GROUP BY cs.Spec_Code, cs.Spec_Value
HAVING COUNT(*) > 1;
```

**预期结果：** 空结果集（没有重复）

### 4. 查看零部件的完整规格参数

```sql
SELECT 
    cs.ID,
    cs.Spec_Code,
    cs.Spec_Value,
    cs.Comments
FROM components_spec cs
JOIN components c ON c.ID = cs.Component_ID
WHERE c.Component_Code = 'TTA4C033002'
ORDER BY cs.Spec_Code;
```

### 5. 测试唯一约束

```sql
-- 获取任意一个零部件ID和规格代码
SELECT Component_ID, Spec_Code FROM components_spec LIMIT 1;

-- 尝试插入重复的组合（应该失败）
INSERT INTO components_spec (Component_ID, Spec_Code, Spec_Value)
VALUES (10, 'unit', '测试重复');
```

**预期结果：** 报错 `Duplicate entry '10-unit' for key 'uk_component_spec'`

## 前端验证

1. **清空浏览器缓存**并刷新页面
2. **打开零部件详情页**，查看TTA4C033002的规格参数
3. **确认不再显示重复**的规格参数记录

## 数据库表结构更新

**更新前：**
```sql
CREATE TABLE components_spec (
  ID INT PRIMARY KEY AUTO_INCREMENT,
  Component_ID INT,
  Spec_Code VARCHAR(50),
  Spec_Value VARCHAR(511),
  ...
);
```

**更新后：**
```sql
CREATE TABLE components_spec (
  ID INT PRIMARY KEY AUTO_INCREMENT,
  Component_ID INT,
  Spec_Code VARCHAR(50),
  Spec_Value VARCHAR(511),
  ...
  CONSTRAINT uk_component_spec UNIQUE (Component_ID, Spec_Code)
);
```

## 预防措施

### 1. 数据库层面
- ✅ 已添加 `(Component_ID, Spec_Code)` 组合唯一约束
- ✅ 防止将来插入重复数据

### 2. 应用层面

建议在创建规格参数时添加检查：

```java
// 检查规格代码是否已存在
Optional<ComponentsSpec> existing = componentsSpecRepository
    .findByComponentIdAndSpecCode(componentId, specCode);
    
if (existing.isPresent()) {
    // 更新现有记录
    existing.get().setSpecValue(newValue);
} else {
    // 创建新记录
    componentsSpecRepository.save(newSpec);
}
```

### 3. 数据导入

导入规格参数时使用：
```sql
INSERT INTO components_spec (Component_ID, Spec_Code, Spec_Value)
VALUES (?, ?, ?)
ON DUPLICATE KEY UPDATE Spec_Value = VALUES(Spec_Value);
```

## 常见问题

### Q1: 执行修复脚本报外键约束错误？

**A:** 脚本已包含 `SET FOREIGN_KEY_CHECKS = 0;`，如果还报错，请检查数据库权限。

### Q2: 修复后前端还是显示重复参数？

**A:** 清除缓存：
```bash
# 清空Redis缓存
docker exec mms-redis redis-cli FLUSHALL

# 重启后端服务
docker-compose restart backend

# 清空浏览器缓存并强制刷新（Ctrl+Shift+R）
```

### Q3: 如何恢复误删的数据？

**A:** 使用修复脚本自动创建的备份文件：
```bash
docker exec -i mms-mariadb mysql -u mms_user -pmms_password mms_db < backup_before_fix_specs_YYYYMMDD_HHMMSS.sql
```

### Q4: 删除重复记录时如何选择保留哪一条？

**A:** 脚本保留最小ID的记录（最早创建的）。如果需要保留其他记录，可以在删除前手动调整。

## 影响范围统计

执行脚本后会显示：
```sql
SELECT 
    COUNT(DISTINCT Component_ID) as affected_components,
    COUNT(*) as total_duplicate_groups,
    SUM(cnt - 1) as records_to_delete
FROM (
    SELECT Component_ID, Spec_Code, COUNT(*) as cnt
    FROM components_spec
    GROUP BY Component_ID, Spec_Code
    HAVING COUNT(*) > 1
) as duplicates;
```

## 文件清单

| 文件 | 说明 |
|------|------|
| `src/main/resources/sql/data_init/fix_duplicate_component_specs.sql` | 修复脚本 |
| `src/main/resources/sql/data_init/schema.sql` | 表结构（已更新） |
| `script/fix-duplicate-specs.bat` | Windows批处理脚本 |
| `docs/FIX_DUPLICATE_COMPONENT_SPECS.md` | 本文档 |

## 实际测试结果

### 测试环境
- 数据库：MariaDB 11.8.3
- 初始化方式：`docker-compose up -d` (全新部署)
- 执行时间：2025-10-21

### 清理前数据状态
- 总记录数（ID范围）：~398,789条
- 重复组数：32,084组
- 需要删除的记录：~259,169条

### 清理后数据状态
- ✅ 最终记录数：139,620条
- ✅ 重复组数：0组
- ✅ 唯一组合数：139,620个（与记录数完全匹配）
- ✅ 涉及零部件数：10,740个
- ✅ 唯一约束：已成功添加

### 性能数据
- 初始化总耗时：~10分钟
- 清理脚本耗时：~3分钟
- 删除记录数：~259,169条

### 验证结果
```sql
-- 重复检查
SELECT COUNT(*) FROM (
  SELECT Component_ID, Spec_Code 
  FROM components_spec 
  GROUP BY Component_ID, Spec_Code 
  HAVING COUNT(*) > 1
) AS d;
-- 结果：0

-- TTA4C033002 规格参数
Component_ID | Spec_Code    | Spec_Value
-------------|-------------|-------------
xxx          | comments    | 
xxx          | commonParts | 合同
xxx          | length      | 111
xxx          | material    | Q235A
xxx          | processes   | 剪板B,折弯B
xxx          | procurement | 自制
xxx          | programCode | *
xxx          | quantity    | 1
xxx          | shapeSpec   | 
xxx          | surfaceTech | 
xxx          | thickness   | 1.5
xxx          | unit        | 件
xxx          | width       | 50
-- 无重复，每个spec_code只有一条记录
```

## 更新记录

- **2025-10-21**: 创建修复脚本，添加组合唯一约束
  - 问题：同一零部件有多条相同spec_code的记录
  - 解决：保留第一条记录（最小ID），删除其他重复
  - 优化：简化脚本输出，避免大量日志导致初始化卡顿
  - 测试：成功清理32,084组重复记录，删除约26万条冗余数据

