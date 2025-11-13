-- 添加缺失的权限（FASTENER, PRODUCTION, COST, BIDDING, HISTORY）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO permissions (name, code, description, resource, action, enabled, Entry_User) VALUES
('创建紧固件', 'FASTENER:CREATE', '创建新紧固件', 'FASTENER', 'CREATE', 1, 'SYS_USER'),
('编辑紧固件', 'FASTENER:UPDATE', '编辑现有紧固件', 'FASTENER', 'UPDATE', 1, 'SYS_USER'),
('删除紧固件', 'FASTENER:DELETE', '删除紧固件', 'FASTENER', 'DELETE', 1, 'SYS_USER'),
('查看紧固件', 'FASTENER:VIEW', '查看紧固件信息', 'FASTENER', 'VIEW', 1, 'SYS_USER'),
('创建生产计划', 'PRODUCTION:CREATE', '创建新生产计划', 'PRODUCTION', 'CREATE', 1, 'SYS_USER'),
('编辑生产计划', 'PRODUCTION:UPDATE', '编辑生产计划', 'PRODUCTION', 'UPDATE', 1, 'SYS_USER'),
('删除生产计划', 'PRODUCTION:DELETE', '删除生产计划', 'PRODUCTION', 'DELETE', 1, 'SYS_USER'),
('查看生产计划', 'PRODUCTION:VIEW', '查看生产计划信息', 'PRODUCTION', 'VIEW', 1, 'SYS_USER'),
('创建成本估算', 'COST:CREATE', '创建新成本估算', 'COST', 'CREATE', 1, 'SYS_USER'),
('编辑成本估算', 'COST:UPDATE', '编辑成本估算', 'COST', 'UPDATE', 1, 'SYS_USER'),
('删除成本估算', 'COST:DELETE', '删除成本估算', 'COST', 'DELETE', 1, 'SYS_USER'),
('查看成本估算', 'COST:VIEW', '查看成本估算信息', 'COST', 'VIEW', 1, 'SYS_USER'),
('创建投标报价', 'BIDDING:CREATE', '创建新投标报价', 'BIDDING', 'CREATE', 1, 'SYS_USER'),
('编辑投标报价', 'BIDDING:UPDATE', '编辑投标报价', 'BIDDING', 'UPDATE', 1, 'SYS_USER'),
('删除投标报价', 'BIDDING:DELETE', '删除投标报价', 'BIDDING', 'DELETE', 1, 'SYS_USER'),
('查看投标报价', 'BIDDING:VIEW', '查看投标报价信息', 'BIDDING', 'VIEW', 1, 'SYS_USER'),
('查看修改历史', 'HISTORY:VIEW', '查看修改历史记录', 'HISTORY', 'VIEW', 1, 'SYS_USER')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), resource=VALUES(resource), action=VALUES(action);

SET FOREIGN_KEY_CHECKS = 1;

