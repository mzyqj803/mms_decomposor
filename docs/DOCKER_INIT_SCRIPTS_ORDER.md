# Docker数据库初始化脚本执行顺序

## 概述

在使用`docker-compose up`首次启动MariaDB容器时，会自动执行`/docker-entrypoint-initdb.d/`目录下的SQL脚本。
脚本按照文件名的字母顺序执行。

## 当前执行顺序

| 顺序 | 容器内文件名 | 源文件路径 | 说明 |
|------|-------------|-----------|------|
| 1 | `01-schema.sql` | `src/main/resources/sql/data_init/schema.sql` | 创建数据库表结构，包含唯一约束 |
| 2 | `02-data_init.sql` | `src/main/resources/sql/data_init/data_init.sql` | 导入初始数据（零部件、合同等） |
| 3 | `03-cleanup_duplicates.sql` | `src/main/resources/sql/data_init/cleanup_duplicate_components_with_fk.sql` | 清理重复的零部件记录 |
| 4 | `04-add_quantity_field.sql` | `src/main/resources/sql/data_init/add_quantity_to_components_relationship.sql` | 添加数量字段到组件关系表 |
| 5 | `05-update_quantity_from_spec.sql` | `src/main/resources/sql/data_init/update_components_relationship_quantity_from_spec.sql` | 从规格表更新数量 |
| 6 | `06-fastener_warehouse_data.sql` | `src/main/resources/sql/data_init/fastener_warehouse_data.sql` | 导入紧固件仓库数据 |
| 7 | `07-create_component_fastener_views.sql` | `src/main/resources/sql/data_init/create_component_fastener_views.sql` | 创建紧固件视图 |
| 8 | `08-update_procurement_flag.sql` | `src/main/resources/sql/data_init/update_procurement_flag.sql` | 更新采购标识 |
| 9 | `09-update_common_parts_flag.sql` | `src/main/resources/sql/data_init/update_common_parts_flag.sql` | 更新通用件标识 |
| 10 | `10-fix_duplicate_specs.sql` | `src/main/resources/sql/data_init/fix_duplicate_component_specs.sql` | **清理重复的规格参数记录** ⭐ |

## 重要说明

### 脚本01: schema.sql
- 创建所有表结构
- ⚠️ **不包含** `components_spec` 表的唯一约束（避免数据导入时因重复而失败）
- 如果表已存在则跳过（使用`CREATE TABLE IF NOT EXISTS`）

### 脚本10: fix_duplicate_specs.sql
- 清理同一零部件下重复的规格参数
- 保留每个`(Component_ID, Spec_Code)`组合的第一条记录（最小ID）
- ✅ **在清理完成后**通过`ALTER TABLE`添加唯一约束：`UNIQUE (Component_ID, Spec_Code)`
- 这样可以确保即使数据中有重复也不会导致初始化失败

### 执行条件

这些脚本**只在首次创建容器时执行**。如果数据库已存在，脚本不会重新执行。

要重新执行初始化脚本，需要：
```bash
# 停止并删除容器和数据卷
docker-compose down -v
docker volume rm mms_decomposor_mariadb_data

# 重新启动（会自动执行所有初始化脚本）
docker-compose up -d
```

## 如何添加新的初始化脚本

1. 将SQL脚本放在 `src/main/resources/sql/data_init/` 目录
2. 在 `docker-compose.yml` 的 `mariadb` 服务下添加volume挂载：
   ```yaml
   - ./src/main/resources/sql/data_init/your_script.sql:/docker-entrypoint-initdb.d/11-your_script.sql
   ```
3. 确保文件名前缀数字按照执行顺序排列
4. 更新本文档记录新脚本的用途

## 常见问题

### Q: 如何跳过某个初始化脚本？

**A:** 临时注释掉docker-compose.yml中对应的volume挂载行：
```yaml
# - ./src/main/resources/sql/data_init/fix_duplicate_component_specs.sql:/docker-entrypoint-initdb.d/10-fix_duplicate_specs.sql
```

### Q: 脚本执行失败怎么办？

**A:** 
1. 查看容器日志：`docker-compose logs mariadb`
2. 检查SQL语法错误
3. 确保脚本之间的依赖关系正确
4. 如果需要，删除数据卷重新初始化

### Q: 如何单独执行某个修复脚本？

**A:** 
```bash
# 对已运行的数据库执行脚本
docker exec -i mms-mariadb mariadb -u mms_user -pmms_password mms_db < src/main/resources/sql/data_init/fix_duplicate_component_specs.sql
```

## 最后更新

- **日期**: 2025-10-21
- **修改**: 添加第10个初始化脚本 `fix_duplicate_component_specs.sql`
- **目的**: 清理重复的规格参数记录并添加唯一约束

