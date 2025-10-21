-- ============================================
-- 修复同一零部件下重复的规格参数问题
-- 问题：同一个Component_ID有多条相同的Spec_Code记录
-- 解决：保留每个(Component_ID, Spec_Code)组合的第一条记录（最小ID），删除其他重复记录
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- 执行清理：直接删除重复记录，保留每组中ID最小的记录
DELETE cs FROM components_spec cs
WHERE cs.ID NOT IN (
    SELECT * FROM (
        SELECT MIN(ID)
        FROM components_spec
        GROUP BY Component_ID, Spec_Code
    ) AS keep_ids
);

-- 验证清理结果 - 检查是否还有重复
SET @remaining_duplicates = (
    SELECT COUNT(*)
    FROM (
        SELECT Component_ID, Spec_Code
        FROM components_spec
        GROUP BY Component_ID, Spec_Code
        HAVING COUNT(*) > 1
    ) AS d
);

SELECT CONCAT('清理完成！剩余重复组数: ', @remaining_duplicates) as status;

-- 添加组合唯一约束，防止将来再次插入重复数据
-- 先检查约束是否已存在
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'components_spec'
    AND CONSTRAINT_NAME = 'uk_component_spec'
);

-- 如果约束不存在，则添加
SET @sql_add_constraint = IF(@constraint_exists = 0,
    'ALTER TABLE components_spec ADD CONSTRAINT uk_component_spec UNIQUE (Component_ID, Spec_Code)',
    'SELECT "唯一约束已存在" as message'
);

PREPARE stmt FROM @sql_add_constraint;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '重复规格参数清理完成！已添加唯一约束。' as final_status;

