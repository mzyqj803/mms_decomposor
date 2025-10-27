-- 添加 status 列到 components 表
-- 1 = active (活动)
-- 0 = deleted (已删除)

-- 添加 status 列，默认值为 1 (active)
ALTER TABLE components 
ADD COLUMN status TINYINT(1) DEFAULT 1 COMMENT '状态: 1=active, 0=deleted';

-- 更新现有记录的 status 为 1
UPDATE components SET status = 1 WHERE status IS NULL;

-- 添加索引以提高查询性能
CREATE INDEX idx_components_status ON components(status);

-- 注释：此迁移脚本为 components 表添加软删除功能

