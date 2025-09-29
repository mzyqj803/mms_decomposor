-- 创建工件装配类型相关视图
-- 根据 components_spec 表中 spec_code='programCode' 的值来区分装配类型

-- 产线装配紧固件视图 - spec_value='产线装配'
CREATE OR REPLACE VIEW components_fastener_assembled AS
SELECT DISTINCT cs.component_id
FROM components_spec cs
WHERE cs.spec_code = 'programCode' 
  AND cs.spec_value = '产线装配'
ORDER BY cs.component_id;

-- 仓库装箱紧固件视图 - spec_value='仓库装箱'  
CREATE OR REPLACE VIEW components_fastener_unassembled AS
SELECT DISTINCT cs.component_id
FROM components_spec cs
WHERE cs.spec_code = 'programCode'
  AND cs.spec_value = '仓库装箱'
ORDER BY cs.component_id;
