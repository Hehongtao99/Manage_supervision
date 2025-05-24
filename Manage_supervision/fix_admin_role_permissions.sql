-- 修复管理员角色权限问题
-- 确保管理员角色有查看角色和编辑角色的权限

-- 查找管理员角色ID和权限ID
SET @admin_role_id = (SELECT id FROM roles WHERE name = 'ADMIN');
SET @role_view_permission_id = (SELECT id FROM permissions WHERE code = 'role:view');
SET @role_edit_permission_id = (SELECT id FROM permissions WHERE code = 'role:edit');

-- 检查并添加查看角色权限
DELETE FROM role_permissions 
WHERE role_id = @admin_role_id AND permission_id = @role_view_permission_id;

INSERT INTO role_permissions (role_id, permission_id, create_time)
VALUES (@admin_role_id, @role_view_permission_id, NOW());

-- 检查并添加编辑角色权限
DELETE FROM role_permissions 
WHERE role_id = @admin_role_id AND permission_id = @role_edit_permission_id;

INSERT INTO role_permissions (role_id, permission_id, create_time)
VALUES (@admin_role_id, @role_edit_permission_id, NOW());

-- 确认管理员角色拥有所有权限
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_time)
SELECT @admin_role_id, id, NOW() FROM permissions
WHERE id NOT IN (SELECT permission_id FROM role_permissions WHERE role_id = @admin_role_id); 