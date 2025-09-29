-- 工件装配类型测试数据
-- 为一些组件设置装配类型规格

-- 插入产线装配紧固件测试数据
INSERT INTO components_spec (component_id, spec_code, spec_value, comments, entry_ts, entry_user) VALUES
-- 假设已有的工件ID为1-5，设置为产线装配
(1, 'programCode', '产线装配', '测试产线装配紧固件1', NOW(), 'TEST_USER'),
(2, 'programCode', '产线装配', '测试产线装配紧固件2', NOW(), 'TEST_USER'),
(3, 'programCode', '产线装配', '测试产线装配紧固件3', NOW(), 'TEST_USER');

-- 插入仓库装箱紧固件测试数据
INSERT INTO components_spec (component_id, spec_code, spec_value, comments, entry_ts, entry_user) VALUES
-- 假设已有的工件ID为4-6，设置为仓库装箱
(4, 'programCode', '仓库装箱', '测试仓库装箱紧固件1', NOW(), 'TEST_USER'),
(5, 'programCode', '仓库装箱', '测试仓库装箱紧固件2', NOW(), 'TEST_USER'),
(6, 'programCode', '仓库装箱', '测试仓库装箱紧固件3', NOW(), 'TEST_USER');

-- 注意：确保component_id（1-6）在components表中存在
-- 如果components表中没有这些数据，请先插入基础组件数据
