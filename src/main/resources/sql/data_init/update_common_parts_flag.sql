-- 更新components表的common_parts_flag列
-- 根据components_spec表中spec_code='programCode'的spec_value值来设置common_parts_flag
-- 如果spec_value='仓库装箱'，则common_parts_flag=1
-- 如果spec_value='产线装配'，则common_parts_flag=2
-- 如果spec_value为空或null，则common_parts_flag=0

-- 首先更新有programCode规格的组件
UPDATE components c
INNER JOIN components_spec cs ON c.ID = cs.Component_ID
SET c.common_parts_flag = CASE 
    WHEN cs.spec_value = '仓库装箱' THEN 1
    WHEN cs.spec_value = '产线装配' THEN 2
    WHEN cs.spec_value IS NULL OR cs.spec_value = '' THEN 0
    ELSE c.common_parts_flag  -- 保持原值，如果spec_value是其他值
END,
c.Last_Update_TS = CURRENT_TIMESTAMP(),
c.Last_Update_User = 'SYS_USER'
WHERE cs.spec_code = 'programCode';

-- 对于没有programCode规格的组件，将common_parts_flag设置为0
UPDATE components c
LEFT JOIN components_spec cs ON c.ID = cs.Component_ID AND cs.spec_code = 'programCode'
SET c.common_parts_flag = 0,
c.Last_Update_TS = CURRENT_TIMESTAMP(),
c.Last_Update_User = 'SYS_USER'
WHERE cs.Component_ID IS NULL;

-- 显示更新结果统计
SELECT 
    '更新完成' as status,
    COUNT(*) as total_components,
    SUM(CASE WHEN common_parts_flag = 1 THEN 1 ELSE 0 END) as warehouse_packing_components,
    SUM(CASE WHEN common_parts_flag = 2 THEN 1 ELSE 0 END) as production_line_components,
    SUM(CASE WHEN common_parts_flag = 0 THEN 1 ELSE 0 END) as default_components
FROM components;



