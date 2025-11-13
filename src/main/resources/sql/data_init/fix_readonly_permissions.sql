-- 修正 READONLY 角色的权限：只保留 VIEW 权限，移除 CREATE、UPDATE、DELETE 权限
-- 此脚本用于修正数据库中已存在的 READONLY 角色权限配置

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 删除 READONLY 角色的所有非 VIEW 权限
DELETE rp FROM role_permissions rp
INNER JOIN roles r ON rp.role_id = r.ID
INNER JOIN permissions p ON rp.permission_id = p.ID
WHERE r.code = 'READONLY' AND p.action != 'VIEW';

-- 确保 READONLY 角色拥有所有 VIEW 权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.ID, p.ID
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'READONLY'
  AND p.action = 'VIEW'  -- 只分配 VIEW 权限
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp2 
    WHERE rp2.role_id = r.ID AND rp2.permission_id = p.ID
  )
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证结果
SELECT r.code, r.name, COUNT(p.ID) as view_permission_count
FROM roles r
LEFT JOIN role_permissions rp ON r.ID = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.ID AND p.action = 'VIEW'
WHERE r.code = 'READONLY'
GROUP BY r.ID, r.code, r.name;

