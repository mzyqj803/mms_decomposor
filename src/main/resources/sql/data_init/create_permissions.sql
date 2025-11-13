-- 创建权限表和角色权限关联表
-- 此脚本创建权限管理相关的表结构

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) Permissions表
CREATE TABLE IF NOT EXISTS permissions (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  name              VARCHAR(100) NOT NULL UNIQUE COMMENT '权限名称',
  code              VARCHAR(50) NOT NULL UNIQUE COMMENT '权限代码',
  description       VARCHAR(255) COMMENT '权限描述',
  resource          VARCHAR(50) COMMENT '资源类型',
  action            VARCHAR(50) COMMENT '操作类型',
  enabled           TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  Entry_TS          TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User        VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS    TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User   VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_permissions_code (code),
  INDEX idx_permissions_resource (resource),
  INDEX idx_permissions_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) Role_Permissions关联表
CREATE TABLE IF NOT EXISTS role_permissions (
  role_id           INT NOT NULL,
  permission_id     INT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  INDEX idx_rp_role (role_id),
  INDEX idx_rp_permission (permission_id),
  CONSTRAINT fk_rp_role
    FOREIGN KEY (role_id) REFERENCES roles(ID) ON DELETE CASCADE,
  CONSTRAINT fk_rp_permission
    FOREIGN KEY (permission_id) REFERENCES permissions(ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- 插入权限数据
INSERT INTO permissions (name, code, description, resource, action, enabled, Entry_User) VALUES
-- 合同相关权限
('创建合同', 'CONTRACT:CREATE', '创建新合同', 'CONTRACT', 'CREATE', 1, 'SYS_USER'),
('编辑合同', 'CONTRACT:UPDATE', '编辑现有合同', 'CONTRACT', 'UPDATE', 1, 'SYS_USER'),
('删除合同', 'CONTRACT:DELETE', '删除合同', 'CONTRACT', 'DELETE', 1, 'SYS_USER'),
('查看合同', 'CONTRACT:VIEW', '查看合同信息', 'CONTRACT', 'VIEW', 1, 'SYS_USER'),

-- 装箱单相关权限
('创建装箱单', 'CONTAINER:CREATE', '创建新装箱单', 'CONTAINER', 'CREATE', 1, 'SYS_USER'),
('编辑装箱单', 'CONTAINER:UPDATE', '编辑装箱单', 'CONTAINER', 'UPDATE', 1, 'SYS_USER'),
('删除装箱单', 'CONTAINER:DELETE', '删除装箱单', 'CONTAINER', 'DELETE', 1, 'SYS_USER'),
('查看装箱单', 'CONTAINER:VIEW', '查看装箱单信息', 'CONTAINER', 'VIEW', 1, 'SYS_USER'),

-- 工艺分解相关权限
('执行工艺分解', 'BREAKDOWN:EXECUTE', '执行工艺分解操作', 'BREAKDOWN', 'EXECUTE', 1, 'SYS_USER'),
('查看工艺分解', 'BREAKDOWN:VIEW', '查看工艺分解结果', 'BREAKDOWN', 'VIEW', 1, 'SYS_USER'),

-- 零部件相关权限
('创建零部件', 'COMPONENT:CREATE', '创建新零部件', 'COMPONENT', 'CREATE', 1, 'SYS_USER'),
('编辑零部件', 'COMPONENT:UPDATE', '编辑现有零部件', 'COMPONENT', 'UPDATE', 1, 'SYS_USER'),
('删除零部件', 'COMPONENT:DELETE', '删除零部件', 'COMPONENT', 'DELETE', 1, 'SYS_USER'),
('查看零部件', 'COMPONENT:VIEW', '查看零部件信息', 'COMPONENT', 'VIEW', 1, 'SYS_USER'),

-- 用户相关权限
('创建用户', 'USER:CREATE', '创建新用户', 'USER', 'CREATE', 1, 'SYS_USER'),
('编辑用户', 'USER:UPDATE', '编辑用户信息', 'USER', 'UPDATE', 1, 'SYS_USER'),
('删除用户', 'USER:DELETE', '删除用户', 'USER', 'DELETE', 1, 'SYS_USER'),
('查看用户', 'USER:VIEW', '查看用户信息', 'USER', 'VIEW', 1, 'SYS_USER'),

-- 角色相关权限
('创建角色', 'ROLE:CREATE', '创建新角色', 'ROLE', 'CREATE', 1, 'SYS_USER'),
('编辑角色', 'ROLE:UPDATE', '编辑角色信息', 'ROLE', 'UPDATE', 1, 'SYS_USER'),
('删除角色', 'ROLE:DELETE', '删除角色', 'ROLE', 'DELETE', 1, 'SYS_USER'),
('查看角色', 'ROLE:VIEW', '查看角色信息', 'ROLE', 'VIEW', 1, 'SYS_USER')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), resource=VALUES(resource), action=VALUES(action);

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.ID, p.ID
FROM roles r, permissions p
WHERE r.code = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 为普通用户角色分配基础查看权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.ID, p.ID
FROM roles r, permissions p
WHERE r.code = 'USER' 
  AND p.code IN ('CONTRACT:VIEW', 'CONTAINER:VIEW', 'BREAKDOWN:VIEW', 'COMPONENT:VIEW')
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);


