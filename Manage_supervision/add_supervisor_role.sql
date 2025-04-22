-- 添加SUPERVISOR角色
INSERT INTO `roles` (`name`, `create_time`, `description`, `permissions`) 
VALUES ('SUPERVISOR', NOW(), '督导员角色，可以管理学生', ',USER_VIEW,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW');

-- 更新USER角色的描述，使其更清晰表示为学生角色
UPDATE `roles` SET `description` = '学生角色，基本用户权限' WHERE `name` = 'USER'; 