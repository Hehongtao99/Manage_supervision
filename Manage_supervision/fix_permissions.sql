-- 修复角色权限数据脚本
-- 去除权限字符串中的前导逗号

-- 查看当前权限数据
SELECT 'Before fix:' as status, id, name, permissions FROM roles WHERE name IN ('ADMIN', 'USER');

-- 修复ADMIN角色的权限，去除前导逗号
UPDATE roles 
SET permissions = 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS'
WHERE name = 'ADMIN';

-- 确保USER角色有基本权限
UPDATE roles 
SET permissions = 'USER_VIEW'
WHERE name = 'USER';

-- 查看修复后的权限数据
SELECT 'After fix:' as status, id, name, permissions FROM roles WHERE name IN ('ADMIN', 'USER');

-- 验证权限是否正确设置
SELECT 
    r.name as role_name,
    r.permissions,
    LENGTH(r.permissions) as permission_length,
    CASE 
        WHEN r.permissions LIKE ',%' THEN 'Has leading comma'
        ELSE 'No leading comma'
    END as comma_status
FROM roles r 
WHERE r.name IN ('ADMIN', 'USER'); 