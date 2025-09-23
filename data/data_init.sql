-- MMS Data Initialization Script
-- Generated from data_init directory
-- _comp.sql files are executed first, then other files

-- ==============================================
-- COMPONENT DATA (_comp.sql files)
-- ==============================================

-- Insert sample components data
INSERT INTO components (ID, Category_Code, Component_Code, Name, Comment, procurement_flag, common_parts_flag) VALUES
(1, 'ELEVATOR', 'ELE-001', '电梯主机', '电梯核心动力设备', false, false),
(2, 'DOOR', 'DOOR-001', '电梯门机', '电梯门开关控制设备', false, true),
(3, 'CABIN', 'CABIN-001', '电梯轿厢', '电梯载客厢体', false, false),
(4, 'RAIL', 'RAIL-001', '电梯导轨', '电梯运行轨道', true, true),
(5, 'CABLE', 'CABLE-001', '电梯电缆', '电梯电力传输线缆', true, true);

-- ==============================================
-- OTHER DATA (process, relationship, spec files)
-- ==============================================

-- Insert sample contracts data
INSERT INTO contracts (Contract_No, Client_Name, Project_Name, Quantity, Status) VALUES
('CT-2024-001', '某大厦有限公司', '某大厦电梯项目', 2, 'DRAFT'),
('CT-2024-002', '商业中心开发公司', '商业中心电梯项目', 4, 'PROCESSING'),
('CT-2024-003', '住宅小区物业', '住宅小区电梯项目', 6, 'COMPLETED');

-- Insert sample containers data
INSERT INTO containers (Contract_ID, Container_No, Name, Container_Size, Container_Weight, Comments) VALUES
(1, 'CNT-001', '电梯主机容器', '20ft', '5000kg', '电梯主机专用容器'),
(1, 'CNT-002', '电梯门机容器', '20ft', '3000kg', '电梯门机专用容器'),
(2, 'CNT-003', '电梯轿厢容器', '40ft', '8000kg', '电梯轿厢专用容器');

-- Insert sample container components data
INSERT INTO container_components (Container_ID, Component_No, Component_Name, Unit_Code, Quantity, Comments) VALUES
(1, 'ELE-001', '电梯主机', 'PCS', 1, '电梯核心动力设备'),
(2, 'DOOR-001', '电梯门机', 'PCS', 2, '电梯门开关控制设备'),
(3, 'CABIN-001', '电梯轿厢', 'PCS', 1, '电梯载客厢体');

-- Insert sample components spec data
INSERT INTO components_spec (Component_ID, Spec_Code, Spec_Value, Comments) VALUES
(1, 'POWER', '15KW', '电梯主机功率'),
(1, 'SPEED', '1.75m/s', '电梯运行速度'),
(2, 'TYPE', '中分门', '电梯门类型'),
(3, 'CAPACITY', '1000kg', '电梯载重能力');

-- Insert sample components relationship data
INSERT INTO components_relationship (Parent_ID, Child_ID) VALUES
(1, 2),
(1, 3),
(2, 4),
(3, 5);

-- Insert sample components processes data
INSERT INTO components_processes (Component_ID, Seq_No, Name, Comments) VALUES
(1, 1, '主机安装', '电梯主机安装工序'),
(1, 2, '主机调试', '电梯主机调试工序'),
(2, 1, '门机安装', '电梯门机安装工序'),
(2, 2, '门机调试', '电梯门机调试工序');

-- Insert sample contract parameters data
INSERT INTO contract_parameters (Contract_ID, Param_Name, Param_Value) VALUES
(1, '楼层数', '20'),
(1, '载重', '1000kg'),
(1, '速度', '1.75m/s'),
(2, '楼层数', '15'),
(2, '载重', '800kg'),
(2, '速度', '1.5m/s');

-- Insert sample containers components summary data
INSERT INTO containers_components_summary (Contract_ID, Component_ID, Container_ID, Quantity) VALUES
(1, 1, 1, 1),
(1, 2, 2, 2),
(2, 3, 3, 1);

-- Insert sample container components breakdown data
INSERT INTO container_components_breakdown (Container_Component_ID, Sub_Component_ID, Container_ID, Quantity) VALUES
(1, 4, 1, 4),
(2, 5, 2, 2),
(3, 1, 3, 1);
