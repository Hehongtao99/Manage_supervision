-- 修复管理员角色权限问题 - 直接方式
-- 确保管理员角色有查看角色和编辑角色的权限

-- 查找管理员角色ID
SET @admin_role_id = (SELECT id FROM roles WHERE name = 'ADMIN');

-- 直接插入所有权限到管理员角色
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_time)
SELECT @admin_role_id, id, NOW() FROM permissions;

-- 确保特别关注的权限被添加
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_time)
SELECT @admin_role_id, id, NOW() FROM permissions WHERE code = 'role:view';

INSERT IGNORE INTO role_permissions (role_id, permission_id, create_time)
SELECT @admin_role_id, id, NOW() FROM permissions WHERE code = 'role:edit';

-- 更新用户表中的角色缓存（如果有的话）
-- 这可能需要根据实际应用架构调整
-- UPDATE users SET permissions_cache = NULL WHERE id IN (
--   SELECT user_id FROM user_roles WHERE role_id = @admin_role_id
-- ); 