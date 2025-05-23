-- 清理角色脚本
-- 删除SUPERVISOR角色及其关联的用户-角色关系

-- 1. 删除SUPERVISOR角色的用户-角色关联
DELETE FROM user_roles 
WHERE role_id IN (
    SELECT id FROM roles 
    WHERE name = 'SUPERVISOR'
);

-- 2. 删除SUPERVISOR角色
DELETE FROM roles 
WHERE name = 'SUPERVISOR';

-- 3. 删除其他可能存在的多余角色（保留ADMIN和USER）
DELETE FROM user_roles 
WHERE role_id NOT IN (
    SELECT id FROM roles 
    WHERE name IN ('ADMIN', 'USER')
);

DELETE FROM roles 
WHERE name NOT IN ('ADMIN', 'USER');

-- 4. 验证清理结果
SELECT 'Remaining roles:' as info;
SELECT id, name, description, create_time 
FROM roles 
ORDER BY id;

SELECT 'Role distribution:' as info;
SELECT r.name as role_name, COUNT(ur.user_id) as user_count
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id
GROUP BY r.id, r.name
ORDER BY r.id; 