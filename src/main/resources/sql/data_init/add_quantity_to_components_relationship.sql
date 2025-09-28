-- 数据库迁移脚本：为 components_relationship 表添加 quantity 字段
-- 执行时间：2025-09-28
-- 目的：在components_relationship表中添加quantity字段，用于存储子组件相对于父组件的数量

-- 添加 quantity 字段，默认值为 1
ALTER TABLE components_relationship 
ADD COLUMN quantity INT DEFAULT 1 COMMENT '子组件相对于父组件的数量';

-- 更新现有记录的 quantity 字段为 1（如果为 NULL）
UPDATE components_relationship 
SET quantity = 1 
WHERE quantity IS NULL;

-- 添加索引以提高查询性能
CREATE INDEX idx_cr_quantity ON components_relationship(quantity);

-- 验证修改
SELECT COUNT(*) as total_records, 
       COUNT(CASE WHEN quantity IS NOT NULL THEN 1 END) as records_with_quantity,
       MIN(quantity) as min_quantity,
       MAX(quantity) as max_quantity
FROM components_relationship;
