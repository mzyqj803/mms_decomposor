-- 创建用户和角色表
-- 此脚本创建用户权限管理相关的表结构

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) Roles表
CREATE TABLE IF NOT EXISTS roles (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  name              VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
  code              VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
  description       VARCHAR(255) COMMENT '角色描述',
  enabled           TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  Entry_TS          TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User        VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS    TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User   VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_roles_code (code),
  INDEX idx_roles_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) Users表
CREATE TABLE IF NOT EXISTS users (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  username          VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password          VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  name              VARCHAR(100) NOT NULL COMMENT '姓名',
  email             VARCHAR(100) COMMENT '邮箱',
  avatar            VARCHAR(255) COMMENT '头像URL',
  enabled           TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  Entry_TS          TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User        VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS    TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User   VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_users_username (username),
  INDEX idx_users_enabled (enabled),
  INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) User_Roles关联表
CREATE TABLE IF NOT EXISTS user_roles (
  user_id           INT NOT NULL,
  role_id           INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  INDEX idx_ur_user (user_id),
  INDEX idx_ur_role (role_id),
  CONSTRAINT fk_ur_user
    FOREIGN KEY (user_id) REFERENCES users(ID) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role
    FOREIGN KEY (role_id) REFERENCES roles(ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认角色（外键检查已在脚本开头关闭）
INSERT INTO roles (name, code, description, enabled, Entry_User) VALUES
('管理员', 'ADMIN', '系统管理员，拥有所有权限', 1, 'SYS_USER'),
('普通用户', 'USER', '普通用户，拥有基本权限', 1, 'SYS_USER'),
('只读用户', 'READONLY', '只读用户，只能查看数据，不能进行修改操作', 1, 'SYS_USER')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- 插入默认管理员用户（密码: admin123）
-- BCrypt加密后的密码（strength=12）: $2a$12$/hf/dDEN1GuvNBwiIwwpk.jlqgA5JAmHSWz3hC2zfb57NThHac7kO
INSERT INTO users (username, password, name, email, enabled, Entry_User) VALUES
('admin', '$2a$12$/hf/dDEN1GuvNBwiIwwpk.jlqgA5JAmHSWz3hC2zfb57NThHac7kO', '系统管理员', 'admin@mms.com', 1, 'SYS_USER')
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email);

-- 为管理员用户分配管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.ID, r.ID
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' AND r.code = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

