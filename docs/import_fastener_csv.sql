-- 导入 docs/fastener.csv 到 fastener_warehouse 表
-- Import fastener.csv data to fastener_warehouse table

-- 清空现有数据（可选，如果需要重新导入）
-- TRUNCATE TABLE fastener_warehouse;

-- 导入CSV数据
-- 注意：这里使用LOAD DATA INFILE命令，需要确保MySQL有FILE权限
-- 如果无法使用LOAD DATA INFILE，可以使用INSERT语句

-- 方法1：使用LOAD DATA INFILE（推荐，性能更好）
LOAD DATA INFILE 'docs/fastener.csv'
INTO TABLE fastener_warehouse
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(erp_code, specs, product_code, name, level, material, surface_treatment, remark)
SET 
    default_flag = 0,
    entry_user = 'SYS_USER',
    entry_ts = CURRENT_TIMESTAMP(),
    last_update_user = 'SYS_USER',
    last_update_ts = CURRENT_TIMESTAMP();

-- 方法2：如果LOAD DATA INFILE不可用，可以使用以下INSERT语句
-- 注意：由于数据量较大（874条记录），建议分批插入

-- 示例：插入前几条记录
INSERT INTO fastener_warehouse (
    erp_code, specs, product_code, name, level, material, surface_treatment, remark, 
    default_flag, entry_user, entry_ts, last_update_user, last_update_ts
) VALUES 
('07.0100.00001', 'M10x35', 'GB12', '圆头方颈螺栓', '4.8级', '碳钢', '镀锌等', '', 0, 'SYS_USER', CURRENT_TIMESTAMP(), 'SYS_USER', CURRENT_TIMESTAMP()),
('07.0101.00001', 'M6x12', 'GB16674.1', '法兰面螺栓', '8.8级', '碳钢', '镀锌等', '', 0, 'SYS_USER', CURRENT_TIMESTAMP(), 'SYS_USER', CURRENT_TIMESTAMP()),
('07.0101.00002', 'M6x14', 'GB16674.1', '法兰面螺栓', '8.8级', '碳钢', '镀锌等', '', 0, 'SYS_USER', CURRENT_TIMESTAMP(), 'SYS_USER', CURRENT_TIMESTAMP()),
('07.0102.00001', 'M6x60', 'GB22795', '膨胀螺栓NP', '', '304', '钝化', '内迫型NP', 0, 'SYS_USER', CURRENT_TIMESTAMP(), 'SYS_USER', CURRENT_TIMESTAMP()),
('07.0102.00002', 'M12x50', 'GB22795', '膨胀螺栓NP', '', '碳钢', '达克罗', '内迫型NP', 0, 'SYS_USER', CURRENT_TIMESTAMP(), 'SYS_USER', CURRENT_TIMESTAMP());

-- 验证导入结果
SELECT COUNT(*) as total_records FROM fastener_warehouse;
SELECT * FROM fastener_warehouse LIMIT 10;

