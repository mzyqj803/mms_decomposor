# Component 软删除功能实现总结

## 概述
为 `components` 表添加了 `status` 列，实现软删除功能。1代表active（活动），0代表deleted（已删除）。

## 更改内容

### 1. 数据库层面

#### 1.1 迁移脚本
**文件**: `src/main/resources/sql/migration/add_component_status.sql`
- 新增迁移脚本，添加 `status` 列到 `components` 表
- 默认值为 1 (active)
- 为现有记录设置 status = 1
- 添加索引 `idx_components_status` 以提高查询性能

#### 1.2 Schema 更新
**文件**: `src/main/resources/sql/data_init/schema.sql`
- 更新初始建表脚本，包含 `status` 列定义
- 添加索引定义

### 2. 实体类层面

#### 2.1 Components 实体
**文件**: `src/main/java/com/mms/entity/Components.java`
- 添加 `status` 字段，类型为 `Integer`
- 默认值为 1 (active)

#### 2.2 ComponentDetailDTO
**文件**: `src/main/java/com/mms/dto/ComponentDetailDTO.java`
- 添加 `status` 字段，用于返回组件详情时包含状态信息

### 3. Repository 层面

#### 3.1 ComponentsRepository
**文件**: `src/main/java/com/mms/repository/ComponentsRepository.java`

修改的方法：
- `findByConditions()`: 添加 `status = 1` 过滤条件
- `findByKeywordContaining()`: 添加 `status = 1` 过滤条件
- `findDistinctCategoryCodes()`: 添加 `status = 1` 过滤条件

新增的方法：
- `findActiveByComponentCode()`: 专门用于查找 active 状态的组件

#### 3.2 ComponentsRelationshipRepository
**文件**: `src/main/java/com/mms/repository/ComponentsRelationshipRepository.java`

修改的方法：
- `findByParentId()`: 只返回父子组件都为 active (status=1) 的关系
- `findByChildId()`: 只返回父子组件都为 active (status=1) 的关系

### 4. Service 层面

#### 4.1 ComponentsServiceImpl
**文件**: `src/main/java/com/mms/service/impl/ComponentsServiceImpl.java`

主要修改：

1. **createComponent()** - 创建组件
   - 检查组件代号时只查找 active 的组件（允许重用已删除组件的代号）
   - 创建新组件时自动设置 `status = 1`
   - 查找父工件时只查找 active 的组件

2. **updateComponent()** - 更新组件
   - 检查代号冲突时只查找 active 的组件

3. **deleteComponent()** - 删除组件（改为软删除）
   - **重要变更**: 不再物理删除组件
   - 改为将 `status` 设置为 0 (deleted)
   - **不检查任何引用**：移除了所有关联数据检查（工艺数据、子组件关系、父组件关系）
   - **不删除关联数据**：规格、工艺、父子关系等关联数据全部保留
   - 只做两件事：设置 status=0 和清除缓存

4. **getComponentSpecsByCode()** - 获取组件规格
   - 只查找 active 的组件

5. **getComponentDetail()** - 获取组件详情
   - 在返回的 DTO 中包含 status 字段

#### 4.2 BreakdownServiceImpl
**文件**: `src/main/java/com/mms/service/impl/BreakdownServiceImpl.java`

主要修改：

1. **getComponentByCode()** - 根据代码获取组件
   - 从数据库查询时使用 `findActiveByComponentCode()`
   - 只获取 active 状态的组件
   - 添加日志记录，当组件被删除时会记录

2. **getOrCreateNonStandardComponent()** - 获取或创建非标组件
   - 第一次检查时使用 `findActiveByComponentCode()`
   - 第二次检查（在事务中）时使用 `findActiveByComponentCode()`
   - 查找基础组件时使用 `findActiveByComponentCode()`
   - 创建非标组件时设置 `status = 1`
   - 异常重试逻辑中也使用 `findActiveByComponentCode()`

**影响**: 工艺分解时只会匹配和使用 status=1 (active) 的组件

### 5. 零部件管理页面

**自动过滤**: 由于修改了 Repository 层的查询方法，零部件管理页面的以下功能会自动过滤已删除的组件：
- 列表查询
- 搜索功能
- 分类列表
- 父子组件关系显示
- 组件详情中的关联关系

## 功能验证点

### 1. 创建组件
- ✅ 新建组件默认 status = 1
- ✅ 可以重用已删除组件的代号
- ✅ 父工件必须是 active 状态

### 2. 查询组件
- ✅ 列表只显示 active 组件
- ✅ 搜索只返回 active 组件
- ✅ 父子关系只显示 active 组件
- ✅ 组件详情包含 status 字段

### 3. 删除组件（软删除）
- ✅ 软删除：status 设置为 0
- ✅ **不检查任何引用**：无论组件是否被引用、是否有子组件，都可以直接删除
- ✅ **不删除关联数据**：规格、工艺、父子关系等数据全部保留
- ✅ 不再物理删除组件和关联数据
- ✅ 清除相关缓存，确保列表不再显示

### 4. 工艺分解
- ✅ 只匹配 active (status=1) 的组件
- ✅ 找不到组件时记录为问题部件
- ✅ 非标组件创建时基于 active 的基础组件
- ✅ 创建的非标组件默认为 active

## 执行迁移

在生产环境执行以下步骤：

1. **备份数据库**
```bash
mysqldump -u username -p database_name > backup_before_status_migration.sql
```

2. **执行迁移脚本**
```bash
mysql -u username -p database_name < src/main/resources/sql/migration/add_component_status.sql
```

3. **验证迁移**
```sql
-- 检查列是否添加成功
DESCRIBE components;

-- 检查所有现有记录的 status 是否为 1
SELECT COUNT(*) FROM components WHERE status = 1;

-- 检查索引是否创建成功
SHOW INDEX FROM components WHERE Key_name = 'idx_components_status';
```

4. **重启应用**
```bash
# 根据部署方式重启应用
```

## 注意事项

1. **无限制删除**: 软删除时不检查任何引用关系，即使组件被其他组件引用或有子组件，也可以直接删除
2. **数据保留**: 删除组件后，其规格、工艺、父子关系等所有关联数据都会保留在数据库中
3. **隐藏显示**: 虽然数据保留，但由于查询时过滤 status=0 的记录，这些组件在列表、搜索、工艺分解中都不会显示
4. **缓存一致性**: 删除组件时会自动清除相关缓存
5. **历史数据**: 迁移后所有现有组件的 status 都会设置为 1 (active)
6. **性能**: 添加了索引，查询性能不会受影响
7. **API 兼容性**: API 接口保持不变，只是删除行为从物理删除改为软删除
8. **数据恢复**: 如需恢复已删除的组件，可以手动将 status 改回 1，所有关联数据依然存在

## 后续建议

1. 考虑添加管理界面功能：
   - 查看已删除的组件列表
   - 恢复已删除的组件（将 status 改回 1）
   - 彻底删除组件（物理删除）

2. 考虑添加审计功能：
   - 记录谁删除了组件
   - 记录删除时间
   - 记录删除原因

3. 考虑添加定期清理功能：
   - 自动清理超过一定时间的已删除组件
   - 或者提供手动批量清理功能

