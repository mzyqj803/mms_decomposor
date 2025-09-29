-- 使用 LOAD DATA INFILE 导入 fastener.csv 到 fastener_warehouse 表
-- 这是最高效的导入方法

-- 清空现有数据（可选）
-- TRUNCATE TABLE fastener_warehouse;

-- 使用 LOAD DATA INFILE 导入数据
LOAD DATA INFILE '/tmp/fastener.csv'
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

-- 验证导入结果
SELECT COUNT(*) as total_records FROM fastener_warehouse;
SELECT * FROM fastener_warehouse LIMIT 10;
