-- 为 container_components_breakdown_problems 表添加 name 列
-- 用于存储不存在的零部件名称

ALTER TABLE container_components_breakdown_problems 
ADD COLUMN name VARCHAR(511) COMMENT '零部件名称';

-- 添加索引以提高查询性能
CREATE INDEX idx_ccbp_name ON container_components_breakdown_problems(name);
