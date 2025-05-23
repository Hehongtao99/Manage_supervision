-- 考勤打卡记录系统数据库修改脚本
-- 执行日期：2025-01-23
-- 修改说明：删除外键约束，支持特殊考勤记录功能

-- 1. 删除attendance_records表的外键约束
ALTER TABLE `attendance_records` DROP FOREIGN KEY `FK_record_attendance`;
ALTER TABLE `attendance_records` DROP FOREIGN KEY `FK_record_user`;

-- 2. 删除唯一约束索引，允许用户在特殊记录中有多条记录
ALTER TABLE `attendance_records` DROP INDEX `UK_attendance_user`;

-- 3. 修改attendance_id字段允许NULL值（用于特殊记录）
ALTER TABLE `attendance_records` MODIFY COLUMN `attendance_id` bigint NULL COMMENT '考勤ID，-1表示特殊记录（无活动考勤时的系统记录）';

-- 4. 添加新的索引
ALTER TABLE `attendance_records` ADD INDEX `idx_attendance_record_attendance_id`(`attendance_id` ASC);

-- 5. 删除其他表的外键约束（按照要求禁止外键约束）

-- 删除attendance表的外键约束
ALTER TABLE `attendance` DROP FOREIGN KEY `FK_attendance_creator`;

-- 删除chat_messages表的外键约束
ALTER TABLE `chat_messages` DROP FOREIGN KEY `FK_chat_messages_conversation`;
ALTER TABLE `chat_messages` DROP FOREIGN KEY `FK_chat_messages_recipient`;
ALTER TABLE `chat_messages` DROP FOREIGN KEY `FK_chat_messages_sender`;

-- 删除conversations表的外键约束
ALTER TABLE `conversations` DROP FOREIGN KEY `FK_conversations_user1`;
ALTER TABLE `conversations` DROP FOREIGN KEY `FK_conversations_user2`;

-- 删除face_recognition_logs表的外键约束
ALTER TABLE `face_recognition_logs` DROP FOREIGN KEY `FK_face_logs_operator`;
ALTER TABLE `face_recognition_logs` DROP FOREIGN KEY `FK_face_logs_user`;

-- 删除teacher_student_relations表的外键约束
ALTER TABLE `teacher_student_relations` DROP FOREIGN KEY `FK_teacher_student_student`;
ALTER TABLE `teacher_student_relations` DROP FOREIGN KEY `FK_teacher_student_teacher`;

-- 删除user_roles表的外键约束
ALTER TABLE `user_roles` DROP FOREIGN KEY `FK_user_roles_role`;
ALTER TABLE `user_roles` DROP FOREIGN KEY `FK_user_roles_user`;

-- 6. 重命名一些索引名称以更规范
ALTER TABLE `attendance` RENAME INDEX `FK_attendance_creator` TO `idx_attendance_creator`;
ALTER TABLE `face_recognition_logs` RENAME INDEX `FK_face_logs_operator` TO `idx_face_logs_operator`;
ALTER TABLE `user_roles` ADD INDEX `idx_user_roles_user`(`user_id` ASC);

-- 修改完成
-- 注意：执行此脚本后，数据库将不再有外键约束，需要在应用层保证数据一致性 