-- 更新角色描述脚本
-- 将USER角色的描述从"学生角色"更新为"跑步爱好者角色"

UPDATE roles 
SET description = '跑步爱好者角色，基本用户权限'
WHERE name = 'USER' AND description LIKE '%学生%';

-- 验证更新结果
SELECT id, name, description, permissions, create_time 
FROM roles 
WHERE name = 'USER'; 