-- 数据库更新脚本：更新 components_relationship 表的 quantity 字段
-- 执行时间：2025-09-28
-- 目的：从 components_spec 表中获取 spec_code='quantity' 的 spec_value 来更新 components_relationship.quantity

-- 更新 components_relationship 表的 quantity 字段
-- 如果子组件在 components_spec 表中有 spec_code='quantity' 的记录，使用其 spec_value
-- 如果没有，则保持默认值 1
UPDATE components_relationship cr
LEFT JOIN components_spec cs ON cr.child_id = cs.component_id AND cs.spec_code = 'quantity'
SET cr.quantity = CASE 
    WHEN cs.spec_value IS NOT NULL AND cs.spec_value != '' THEN 
        CASE 
            WHEN cs.spec_value REGEXP '^[0-9]+$' THEN CAST(cs.spec_value AS UNSIGNED)
            ELSE 1
        END
    ELSE 1
END;

-- 验证更新结果
SELECT 
    cr.id as relationship_id,
    p.component_code as parent_code,
    p.name as parent_name,
    c.component_code as child_code,
    c.name as child_name,
    cr.quantity as relationship_quantity,
    cs.spec_value as spec_quantity_value,
    CASE 
        WHEN cs.spec_code IS NOT NULL THEN 'From spec'
        ELSE 'Default value'
    END as quantity_source
FROM components_relationship cr
JOIN components p ON cr.parent_id = p.id
JOIN components c ON cr.child_id = c.id
LEFT JOIN components_spec cs ON cr.child_id = cs.component_id AND cs.spec_code = 'quantity'
ORDER BY cr.id
LIMIT 20;

-- 统计信息
SELECT 
    COUNT(*) as total_relationships,
    COUNT(CASE WHEN quantity = 1 THEN 1 END) as default_quantity_count,
    COUNT(CASE WHEN quantity > 1 THEN 1 END) as custom_quantity_count,
    MIN(quantity) as min_quantity,
    MAX(quantity) as max_quantity,
    AVG(quantity) as avg_quantity
FROM components_relationship;
