-- 用户学院专业班级功能测试数据
-- 执行时间：在系统启动后执行

-- 1. 插入测试学院数据
INSERT INTO `colleges` (`college_name`, `college_code`, `description`, `status`) VALUES
('计算机学院', 'CS', '计算机科学与技术学院', 'active'),
('电子信息学院', 'EI', '电子信息工程学院', 'active'),
('机械工程学院', 'ME', '机械工程学院', 'active'),
('经济管理学院', 'EM', '经济管理学院', 'active');

-- 2. 插入测试专业数据
INSERT INTO `majors` (`major_name`, `major_code`, `college_id`, `description`, `status`) VALUES
('计算机科学与技术', 'CS01', 1, '计算机科学与技术专业', 'active'),
('软件工程', 'CS02', 1, '软件工程专业', 'active'),
('网络工程', 'CS03', 1, '网络工程专业', 'active'),
('电子信息工程', 'EI01', 2, '电子信息工程专业', 'active'),
('通信工程', 'EI02', 2, '通信工程专业', 'active'),
('机械设计制造及其自动化', 'ME01', 3, '机械设计制造及其自动化专业', 'active'),
('工商管理', 'EM01', 4, '工商管理专业', 'active'),
('会计学', 'EM02', 4, '会计学专业', 'active');

-- 3. 插入测试班级数据
INSERT INTO `classes` (`class_name`, `class_code`, `college_id`, `major_id`, `grade`, `student_count`, `description`, `status`) VALUES
('计科2021-1班', 'CS2101', 1, 1, '2021', 30, '计算机科学与技术2021级1班', 'active'),
('计科2021-2班', 'CS2102', 1, 1, '2021', 32, '计算机科学与技术2021级2班', 'active'),
('软工2021-1班', 'CS2201', 1, 2, '2021', 28, '软件工程2021级1班', 'active'),
('软工2021-2班', 'CS2202', 1, 2, '2021', 30, '软件工程2021级2班', 'active'),
('网工2021-1班', 'CS2301', 1, 3, '2021', 25, '网络工程2021级1班', 'active'),
('电信2021-1班', 'EI2101', 2, 4, '2021', 35, '电子信息工程2021级1班', 'active'),
('通信2021-1班', 'EI2201', 2, 5, '2021', 33, '通信工程2021级1班', 'active'),
('机械2021-1班', 'ME2101', 3, 6, '2021', 40, '机械设计制造及其自动化2021级1班', 'active'),
('工管2021-1班', 'EM2101', 4, 7, '2021', 38, '工商管理2021级1班', 'active'),
('会计2021-1班', 'EM2201', 4, 8, '2021', 42, '会计学2021级1班', 'active');

-- 4. 更新现有用户的学院专业班级信息（示例）
-- 注意：这里需要根据实际的用户ID进行调整

-- 假设用户ID为2的是学生，设置为计科2021-1班
UPDATE `users` SET `college_id` = 1, `major_id` = 1, `class_id` = 1 WHERE `id` = 2 AND JSON_CONTAINS(`roles`, '"学生"');

-- 假设用户ID为3的是教师，设置为计算机学院软件工程专业（教师不设置班级）
UPDATE `users` SET `college_id` = 1, `major_id` = 2 WHERE `id` = 3 AND JSON_CONTAINS(`roles`, '"教师"');

-- 查询验证数据
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.roles,
    c.college_name,
    m.major_name,
    cl.class_name
FROM users u
LEFT JOIN colleges c ON u.college_id = c.id
LEFT JOIN majors m ON u.major_id = m.id
LEFT JOIN classes cl ON u.class_id = cl.id
WHERE u.college_id IS NOT NULL
ORDER BY u.id; 