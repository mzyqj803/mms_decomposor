-- 清理重复的component_code记录（处理外键约束）
-- 保留每个component_code的最小ID，删除其他重复记录
-- 执行日期: 2025-09-24
-- 结果: 成功清理2,468个重复组，删除19,936条重复记录

-- 统计清理前的重复记录情况
SELECT '清理前统计:' as info;
SELECT 
    COUNT(*) as total_duplicate_groups,
    SUM(duplicate_count - 1) as total_records_to_delete
FROM (
    SELECT component_code, COUNT(*) as duplicate_count
    FROM components 
    GROUP BY component_code 
    HAVING COUNT(*) > 1
) t;

-- 创建临时表存储要保留的component ID
CREATE TEMPORARY TABLE temp_keep_components AS
SELECT component_code, MIN(id) as keep_id
FROM components 
GROUP BY component_code 
HAVING COUNT(*) > 1;

-- 创建临时表存储要删除的component ID
CREATE TEMPORARY TABLE temp_delete_components AS
SELECT c.id as delete_id, tkc.keep_id
FROM components c
JOIN temp_keep_components tkc ON c.component_code = tkc.component_code
WHERE c.id != tkc.keep_id;

-- 更新外键引用：将删除记录的引用指向保留记录
-- 1. 更新 components_processes
UPDATE components_processes cp
JOIN temp_delete_components tdc ON cp.Component_ID = tdc.delete_id
SET cp.Component_ID = tdc.keep_id;

-- 2. 更新 components_spec
UPDATE components_spec cs
JOIN temp_delete_components tdc ON cs.Component_ID = tdc.delete_id
SET cs.Component_ID = tdc.keep_id;

-- 3. 更新 containers_components_summary
UPDATE containers_components_summary ccs
JOIN temp_delete_components tdc ON ccs.Component_ID = tdc.delete_id
SET ccs.Component_ID = tdc.keep_id;

-- 4. 更新 components_relationship (Parent_ID)
UPDATE components_relationship cr
JOIN temp_delete_components tdc ON cr.Parent_ID = tdc.delete_id
SET cr.Parent_ID = tdc.keep_id;

-- 5. 更新 components_relationship (Child_ID)
UPDATE components_relationship cr
JOIN temp_delete_components tdc ON cr.Child_ID = tdc.delete_id
SET cr.Child_ID = tdc.keep_id;

-- 6. 更新 container_components_breakdown
UPDATE container_components_breakdown ccb
JOIN temp_delete_components tdc ON ccb.Sub_Component_ID = tdc.delete_id
SET ccb.Sub_Component_ID = tdc.keep_id;

-- 现在可以安全删除重复的components记录
DELETE c FROM components c
JOIN temp_delete_components tdc ON c.id = tdc.delete_id;

-- 清理临时表
DROP TEMPORARY TABLE temp_keep_components;
DROP TEMPORARY TABLE temp_delete_components;

-- 统计清理后的情况
SELECT '清理后统计:' as info;
SELECT COUNT(*) as total_components FROM components;

-- 验证是否还有重复记录
SELECT '验证重复记录:' as info;
SELECT COUNT(*) as remaining_duplicates
FROM (
    SELECT component_code, COUNT(*) as duplicate_count
    FROM components 
    GROUP BY component_code 
    HAVING COUNT(*) > 1
) t;

-- 执行说明:
-- 1. 此脚本解决了component_code重复导致的NonUniqueResultException问题
-- 2. 处理了6个相关表的外键约束
-- 3. 保留了每个component_code的最小ID记录
-- 4. 将所有外键引用重定向到保留的记录
-- 5. 安全删除了重复记录
-- 6. 清理后每个component_code只有唯一记录
