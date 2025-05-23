-- 角色更新脚本
-- 将原有的 ADMIN、USER、SUPERVISOR 角色更新为新的四个角色

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 删除旧的角色数据
DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE name IN ('ADMIN', 'USER', 'SUPERVISOR'));
DELETE FROM roles WHERE name IN ('ADMIN', 'USER', 'SUPERVISOR');

-- 2. 插入新的角色数据
INSERT INTO roles (name, description, permissions, create_time) VALUES 
('ADMIN_END', '管理员端，拥有最高权限', 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS,AD_MANAGEMENT', NOW()),
('MERCHANT_END', '商家端，商家相关权限', 'USER_VIEW,AD_MANAGEMENT', NOW()),
('CITYIN_END', '城投端，城投相关权限', 'USER_VIEW,AD_MANAGEMENT,REGION_MANAGEMENT', NOW()),
('ENTERPRISE_END', '企业端，企业相关权限', 'USER_VIEW,AD_MANAGEMENT', NOW());

-- 3. 为现有的admin用户分配ADMIN_END角色
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN_END'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- 4. 如果存在其他用户，可以根据需要为他们分配合适的角色
-- 例如：将所有非admin用户设置为企业端角色
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username != 'admin' AND r.name = 'ENTERPRISE_END'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id
);

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1; 