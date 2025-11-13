-- 更新components表的procurement_flag列
-- 根据components_spec表中spec_code='procurement'的spec_value值来设置procurement_flag
-- 如果spec_value是'自制'或空白或null，则procurement_flag为0
-- 如果spec_value是'外购'，则procurement_flag为1

-- 首先更新有procurement规格的组件
UPDATE components c
INNER JOIN components_spec cs ON c.ID = cs.Component_ID
SET c.procurement_flag = CASE 
    WHEN cs.spec_value = '外购' THEN 1
    WHEN cs.spec_value = '自制' OR cs.spec_value IS NULL OR cs.spec_value = '' THEN 0
    ELSE c.procurement_flag  -- 保持原值，如果spec_value是其他值
END,
c.Last_Update_TS = CURRENT_TIMESTAMP(),
c.Last_Update_User = 'SYS_USER'
WHERE cs.spec_code = 'procurement';

-- 对于没有procurement规格的组件，将procurement_flag设置为0（自制）
UPDATE components c
LEFT JOIN components_spec cs ON c.ID = cs.Component_ID AND cs.spec_code = 'procurement'
SET c.procurement_flag = 0,
c.Last_Update_TS = CURRENT_TIMESTAMP(),
c.Last_Update_User = 'SYS_USER'
WHERE cs.Component_ID IS NULL;

-- 显示更新结果统计
SELECT 
    '更新完成' as status,
    COUNT(*) as total_components,
    SUM(CASE WHEN procurement_flag = 1 THEN 1 ELSE 0 END) as procurement_components,
    SUM(CASE WHEN procurement_flag = 0 THEN 1 ELSE 0 END) as self_made_components
FROM components;











