# 数据库初始化脚本测试报告

## 测试概述

**测试日期：** 2025-10-21  
**测试目的：** 验证数据库初始化脚本（包括重复规格参数清理）能够正常执行  
**测试方式：** 删除数据卷，完全重新初始化数据库  

## 测试环境

- **操作系统：** Windows 10
- **Docker版本：** Docker Desktop
- **数据库：** MariaDB 11.8.3
- **初始化方式：** `docker-compose down -v && docker-compose up -d`

## 测试结果

### ✅ 整体测试：通过

所有初始化脚本执行成功，数据完整，无错误。

### 初始化脚本执行顺序

| 序号 | 脚本文件 | 状态 | 说明 |
|------|---------|------|------|
| 1 | `01-schema.sql` | ✅ 成功 | 创建表结构 |
| 2 | `02-data_init.sql` | ✅ 成功 | 导入初始数据（约6分钟） |
| 3 | `03-cleanup_duplicates.sql` | ✅ 成功 | 清理重复关系 |
| 4 | `04-add_quantity_field.sql` | ✅ 成功 | 添加数量字段 |
| 5 | `05-update_quantity_from_spec.sql` | ✅ 成功 | 更新数量数据 |
| 6 | `06-fastener_warehouse_data.sql` | ✅ 成功 | 导入紧固件数据 |
| 7 | `07-create_component_fastener_views.sql` | ✅ 成功 | 创建视图 |
| 8 | `08-update_procurement_flag.sql` | ✅ 成功 | 更新采购标志 |
| 9 | `09-update_common_parts_flag.sql` | ✅ 成功 | 更新通用件标志 |
| 10 | `10-fix_duplicate_specs.sql` | ✅ 成功 | **清理重复规格参数**（约3分钟） |

### 性能数据

- **总初始化时间：** ~10分钟
- **数据导入耗时：** ~6分钟（02-data_init.sql）
- **清理脚本耗时：** ~3分钟（10-fix_duplicate_specs.sql）
- **其他脚本耗时：** <1分钟

## 重复规格参数清理测试

### 测试重点

验证 `10-fix_duplicate_specs.sql` 脚本能够：
1. 识别并删除重复的 `(Component_ID, Spec_Code)` 组合
2. 保留每组的第一条记录（最小ID）
3. 添加唯一约束防止将来插入重复数据

### 清理前数据状态

```
总记录数（ID范围）：~398,789条
重复组数：32,084组
影响零部件数：10,740个
需要删除的记录：~259,169条
```

### 清理后数据状态

```
最终记录数：139,620条
重复组数：0组
唯一组合数：139,620个
删除的记录：~259,169条
数据完整性：✅ 正常
```

### 验证SQL

```sql
-- 检查重复记录（预期结果：0）
SELECT COUNT(*) as duplicates
FROM (
    SELECT Component_ID, Spec_Code
    FROM components_spec
    GROUP BY Component_ID, Spec_Code
    HAVING COUNT(*) > 1
) AS d;
-- 结果：0 ✅

-- 检查唯一约束
SHOW CREATE TABLE components_spec;
-- 结果：包含 UNIQUE KEY uk_component_spec (Component_ID, Spec_Code) ✅

-- 验证数据完整性
SELECT 
    COUNT(DISTINCT Component_ID) as components,
    COUNT(*) as total_records,
    COUNT(DISTINCT CONCAT(Component_ID, '-', Spec_Code)) as unique_combos
FROM components_spec;
-- 结果：
--   components: 10,740
--   total_records: 139,620
--   unique_combos: 139,620
-- total_records == unique_combos ✅
```

### 特定零部件验证（TTA4C033002）

```sql
SELECT cs.Spec_Code, cs.Spec_Value
FROM components_spec cs
JOIN components c ON c.ID = cs.Component_ID
WHERE c.Component_Code = 'TTA4C033002'
ORDER BY cs.Spec_Code;
```

**结果：** 13个规格参数，每个 `Spec_Code` 只有一条记录 ✅

```
Spec_Code    | Spec_Value
-------------|-------------
comments     | 
commonParts  | 合同
length       | 111
material     | Q235A
processes    | 剪板B,折弯B
procurement  | 自制
programCode  | *
quantity     | 1
shapeSpec    | 
surfaceTech  | 
thickness    | 1.5
unit         | 件
width        | 50
```

## 唯一约束测试

### 测试插入重复数据

```sql
-- 尝试插入重复的(Component_ID, Spec_Code)组合
INSERT INTO components_spec (Component_ID, Spec_Code, Spec_Value)
SELECT Component_ID, Spec_Code, '测试重复' 
FROM components_spec LIMIT 1;
```

**预期结果：** 报错 `Duplicate entry for key 'uk_component_spec'`  
**实际结果：** ✅ 正确报错，唯一约束生效

## 数据库表结构验证

### components_spec表结构

```sql
CREATE TABLE `components_spec` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Component_ID` int(11) DEFAULT NULL,
  `Spec_Code` varchar(50) DEFAULT NULL,
  `Spec_Value` varchar(511) DEFAULT NULL,
  `Comments` text DEFAULT NULL,
  `Entry_TS` timestamp NULL DEFAULT current_timestamp(),
  `Entry_User` varchar(50) DEFAULT 'SYS_USER',
  `Last_Update_TS` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `Last_Update_User` varchar(50) DEFAULT 'SYS_USER',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_component_spec` (`Component_ID`,`Spec_Code`),  ← ✅ 唯一约束已添加
  KEY `idx_cs_component` (`Component_ID`),
  CONSTRAINT `fk_cs_component` FOREIGN KEY (`Component_ID`) REFERENCES `components` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=398789 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```

**验证项：**
- ✅ 主键 `PRIMARY KEY (ID)`
- ✅ 唯一约束 `UNIQUE KEY uk_component_spec (Component_ID, Spec_Code)`
- ✅ 索引 `idx_cs_component (Component_ID)`
- ✅ 外键 `fk_cs_component` 关联到 `components(ID)`

## 脚本优化说明

### 问题

初始版本的清理脚本包含大量的 `SELECT` 输出语句，导致：
- 日志文件过大（数百MB）
- 初始化过程可能卡顿
- 难以查看关键的执行结果

### 优化措施

1. **简化输出**：移除所有详细的重复记录列表输出
2. **精简逻辑**：直接使用 `DELETE... NOT IN (SELECT MIN(ID)...)` 而不是临时表
3. **关键信息**：只输出清理状态和剩余重复组数
4. **性能提升**：减少了不必要的查询和连接操作

### 优化效果

| 指标 | 优化前 | 优化后 |
|------|--------|--------|
| 脚本行数 | 187行 | 54行 |
| 日志输出大小 | ~100MB+ | <1MB |
| 执行时间 | 可能超时 | ~3分钟 |
| 可读性 | 详细但冗长 | 简洁清晰 |

## 问题与解决

### 问题1：初次测试时删除操作未执行

**原因：** 脚本中的 `GROUP_CONCAT` 输出了大量的ID列表（有的组有744条重复记录），导致日志过大。

**解决：** 简化脚本，移除所有详细输出，只保留核心的DELETE和ADD CONSTRAINT操作。

### 问题2：如何验证清理是否成功

**原因：** 日志输出太多，难以找到关键信息。

**解决：** 
1. 优化脚本输出，只显示简洁的状态信息
2. 添加剩余重复组数统计
3. 使用SQL查询直接验证数据库状态

## 测试结论

### ✅ 测试通过

1. **初始化成功**：所有10个初始化脚本按顺序执行完成
2. **数据完整**：139,620条规格参数记录，涉及10,740个零部件
3. **无重复数据**：32,084组重复记录全部清除
4. **约束生效**：唯一约束正确添加并生效
5. **性能良好**：总初始化时间约10分钟，符合预期

### 建议

1. **✅ 已实施**：清理脚本已添加到 `docker-compose.yml` 初始化列表
2. **✅ 已实施**：脚本已优化，减少了不必要的输出
3. **建议**：定期备份数据库，以防数据丢失
4. **建议**：监控数据库大小，及时清理不必要的数据

## 相关文档

- `docs/FIX_DUPLICATE_COMPONENT_SPECS.md` - 重复规格参数修复详细文档
- `docs/DOCKER_INIT_SCRIPTS_ORDER.md` - 初始化脚本执行顺序说明
- `src/main/resources/sql/data_init/fix_duplicate_component_specs.sql` - 清理脚本源码

## 测试人员

- AI Assistant
- 测试时间：2025-10-21
- 测试环境：Windows 10 + Docker Desktop

---

**测试状态：** ✅ **通过**  
**是否可以部署：** ✅ **可以**  
**备注：** 所有功能正常，数据完整，无已知问题。

