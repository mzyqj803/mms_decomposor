-- 删除contracts表中的quantity字段
-- 执行前请备份数据库

-- 检查字段是否存在
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'contracts' 
  AND COLUMN_NAME = 'quantity';

-- 删除quantity字段
ALTER TABLE contracts DROP COLUMN IF EXISTS quantity;

-- 验证字段已删除
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'contracts' 
  AND COLUMN_NAME = 'quantity';

