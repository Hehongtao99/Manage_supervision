-- 检查教师学生关系表是否存在正确数据
SELECT
    ts.id AS relation_id,
    t.id AS teacher_id,
    t.username AS teacher_username,
    t.real_name AS teacher_real_name,
    s.id AS student_id,
    s.username AS student_username,
    s.real_name AS student_real_name,
    pc.id AS parent_child_relation_id,
    p.id AS parent_id,
    p.username AS parent_username
FROM
    teacher_student_relations ts
JOIN 
    users t ON ts.teacher_id = t.id
JOIN 
    users s ON ts.student_id = s.id
LEFT JOIN 
    parent_child_relations pc ON pc.child_id = s.id
LEFT JOIN 
    users p ON p.id = pc.parent_id
WHERE
    ts.status = 'active';

-- 检查有子女关系但没有教师关系的记录
SELECT
    pc.id AS parent_child_relation_id,
    p.id AS parent_id,
    p.username AS parent_username,
    c.id AS student_id, 
    c.username AS student_username,
    c.real_name AS student_real_name,
    (SELECT COUNT(*) FROM teacher_student_relations ts WHERE ts.student_id = c.id AND ts.status = 'active') AS teacher_count
FROM
    parent_child_relations pc
JOIN 
    users p ON pc.parent_id = p.id  
JOIN 
    users c ON pc.child_id = c.id
WHERE
    pc.status = 'confirmed'
    AND (SELECT COUNT(*) FROM teacher_student_relations ts WHERE ts.student_id = c.id AND ts.status = 'active') = 0;

-- 修复测试: 在没有教师的情况下，为学生分配一个默认教师(假设ID为1的用户是系统中的一个教师)
-- 注意：这只是测试示例，实际执行前请确认教师ID

-- INSERT INTO teacher_student_relations (teacher_id, student_id, status, assign_time)
-- SELECT 
--     1 AS teacher_id, -- 修改为真实的教师ID
--     c.id AS student_id,
--     'active' AS status,
--     NOW() AS assign_time
-- FROM 
--     parent_child_relations pc
-- JOIN 
--     users c ON pc.child_id = c.id
-- WHERE 
--     pc.status = 'confirmed'
--     AND NOT EXISTS (
--         SELECT 1 FROM teacher_student_relations ts 
--         WHERE ts.student_id = c.id AND ts.status = 'active'
--     );

-- 检查用户角色，确认哪些用户是教师角色
SELECT 
    u.id, 
    u.username, 
    u.real_name,
    r.name AS role_name
FROM 
    users u
JOIN 
    user_roles ur ON u.id = ur.user_id
JOIN 
    roles r ON ur.role_id = r.id
WHERE 
    r.name = 'SUPERVISOR';

-- 检查家长-子女关系
SELECT
    pc.id,
    p.username AS parent_username,
    p.real_name AS parent_real_name,
    c.username AS child_username,
    c.real_name AS child_real_name,
    pc.relation_type,
    pc.status
FROM
    parent_child_relations pc
JOIN
    users p ON pc.parent_id = p.id
JOIN
    users c ON pc.child_id = c.id; 