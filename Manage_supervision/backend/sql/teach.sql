/*
 Navicat Premium Dump SQL

 Source Server         : 115.120.221.163
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : 115.120.221.163:3306
 Source Schema         : teach

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 10/05/2025 17:19:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NULL DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  `recipient_id` bigint NOT NULL,
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sent_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_messages_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_chat_messages_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_chat_messages_recipient_id`(`recipient_id` ASC) USING BTREE,
  CONSTRAINT `FK_chat_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_chat_messages_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_chat_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 1, 4, 3, '1', '2025-05-09 23:24:02', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (2, 1, 4, 3, '在吗', '2025-05-09 23:27:25', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (3, 1, 3, 4, '在吗', '2025-05-09 23:27:34', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (4, 1, 3, 4, '再', '2025-05-09 23:27:36', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (5, 1, 3, 4, '干啥', '2025-05-09 23:27:37', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (6, 1, 3, 4, '？》', '2025-05-09 23:27:39', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (7, 1, 3, 4, '1', '2025-05-09 23:27:40', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (8, 1, 3, 4, '1', '2025-05-09 23:27:41', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (9, 1, 3, 4, '1', '2025-05-09 23:27:42', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (10, 1, 3, 4, '1', '2025-05-09 23:27:42', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (11, 1, 3, 4, '1', '2025-05-09 23:27:42', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (12, 1, 3, 4, '1', '2025-05-09 23:27:43', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (13, 1, 3, 4, '1', '2025-05-09 23:27:43', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (15, 1, 3, 4, '1', '2025-05-09 23:27:49', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (16, 1, 3, 4, '1', '2025-05-09 23:27:51', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (17, 1, 4, 3, '1', '2025-05-09 23:31:53', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (18, 1, 4, 3, '1', '2025-05-09 23:31:54', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (19, 1, 4, 3, '1', '2025-05-09 23:56:49', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (20, 1, 4, 3, '1', '2025-05-09 23:56:52', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (21, 1, 4, 3, '1', '2025-05-10 16:10:50', 1, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for conversations
-- ----------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user1_id` bigint NOT NULL,
  `user2_id` bigint NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_message_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `unread_count_user1` int NOT NULL DEFAULT 0,
  `unread_count_user2` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversations_user1_id`(`user1_id` ASC) USING BTREE,
  INDEX `idx_conversations_user2_id`(`user2_id` ASC) USING BTREE,
  CONSTRAINT `FK_conversations_user1` FOREIGN KEY (`user1_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_conversations_user2` FOREIGN KEY (`user2_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversations
-- ----------------------------
INSERT INTO `conversations` VALUES (1, 4, 3, '2025-05-09 10:11:52', '2025-05-10 16:10:50', 0, 0);

-- ----------------------------
-- Table structure for course_applications
-- ----------------------------
DROP TABLE IF EXISTS `course_applications`;
CREATE TABLE `course_applications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NULL DEFAULT NULL,
  `teacher_id` bigint NOT NULL,
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '课程科目',
  `hourly_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '每小时价格',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程描述',
  `work_time_start` int NOT NULL DEFAULT 8 COMMENT '工作开始时间（小时）',
  `work_time_end` int NOT NULL DEFAULT 18 COMMENT '工作结束时间（小时）',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程标题',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending',
  `rejection_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `applied_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `review_time` datetime NULL DEFAULT NULL,
  `reviewer_id` bigint NULL DEFAULT NULL,
  `image_paths` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程图片路径(JSON数组)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK_course_applications_reviewer`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_course_applications_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_course_applications_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_course_applications_status`(`status` ASC) USING BTREE,
  CONSTRAINT `FK_course_applications_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_course_applications_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `FK_course_applications_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_applications
-- ----------------------------
INSERT INTO `course_applications` VALUES (2, NULL, 3, '化学', 111.00, '11111111111111', 8, 12, '1111111111', 'PENDING', NULL, '2025-05-09 01:22:02', '2025-05-08 17:29:29', '2025-05-08 17:22:02', NULL, NULL, '[\"/uploads/0e3dbb17-5af2-482b-95c8-53f7c11d4753.jpg\"]');
INSERT INTO `course_applications` VALUES (3, NULL, 3, '化学', 222.00, '222222222222222', 8, 11, '2222222222', 'APPROVED', '不逊', '2025-05-09 01:31:44', '2025-05-09 01:54:34', '2025-05-08 17:31:43', NULL, NULL, '[\"/uploads/course-images/f2f6895a-5f96-49ab-9788-3bf641061021.jpg\",\"/uploads/course-images/a397c814-3a8b-4c04-84c7-53920c94ebfe.jpg\"]');
INSERT INTO `course_applications` VALUES (4, NULL, 3, '历史', 33.00, '1111111111111111111111', 9, 12, '33333333333', 'APPROVED', NULL, '2025-05-09 01:41:15', '2025-05-09 01:53:29', '2025-05-08 17:41:15', NULL, NULL, '[\"/uploads/course-images/ad43804b-1dc9-42de-b7dd-a1f996882b80.jpg\",\"/uploads/course-images/04478ab2-3a3a-4b5a-b027-1b566d9f0cf9.jpg\"]');

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL,
  `subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `hourly_rate` decimal(10, 2) NOT NULL,
  `daily_rate` decimal(10, 2) NULL DEFAULT NULL,
  `daily_hours` int NULL DEFAULT NULL,
  `weekly_rate` decimal(10, 2) NULL DEFAULT NULL,
  `weekly_hours` int NULL DEFAULT NULL,
  `monthly_rate` decimal(10, 2) NULL DEFAULT NULL,
  `monthly_hours` int NULL DEFAULT NULL,
  `daily_discount` int NULL DEFAULT NULL,
  `weekly_discount` int NULL DEFAULT NULL,
  `monthly_discount` int NULL DEFAULT NULL,
  `work_start_time` int NULL DEFAULT NULL,
  `work_end_time` int NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_courses_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_courses_status`(`status` ASC) USING BTREE,
  CONSTRAINT `FK_courses_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of courses
-- ----------------------------

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `order_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `price` decimal(10, 2) NOT NULL COMMENT '课程价格',
  `hours` int NOT NULL COMMENT '购买小时数',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING(待确认), ACCEPTED(已接受), REJECTED(已拒绝), CANCELED(已取消), COMPLETED(已完成)',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学生留言',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_number`(`order_number` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_orders_course` FOREIGN KEY (`course_id`) REFERENCES `course_applications` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_orders_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_orders_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `permissions` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'ADMIN', '管理员角色，拥有最高权限', 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS', '2025-05-08 16:09:28');
INSERT INTO `roles` VALUES (2, 'USER', '学生角色，基本用户权限', 'USER_VIEW', '2025-05-08 16:09:28');
INSERT INTO `roles` VALUES (3, 'SUPERVISOR', '督导员角色，可以管理学生', 'USER_VIEW,USER_EDIT,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW', '2025-05-08 16:09:28');

-- ----------------------------
-- Table structure for subjects
-- ----------------------------
DROP TABLE IF EXISTS `subjects`;
CREATE TABLE `subjects`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '科目名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程科目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of subjects
-- ----------------------------
INSERT INTO `subjects` VALUES (1, '语文', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (2, '数学', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (3, '英语', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (4, '物理', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (5, '化学', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (6, '生物', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (7, '地理', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (8, '政治', '2025-05-08 16:46:04', '2025-05-08 16:46:04');
INSERT INTO `subjects` VALUES (9, '历史', '2025-05-08 16:46:04', '2025-05-08 16:46:04');

-- ----------------------------
-- Table structure for teacher_student_relations
-- ----------------------------
DROP TABLE IF EXISTS `teacher_student_relations`;
CREATE TABLE `teacher_student_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `assign_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_teacher_student`(`teacher_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_teacher_student_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_teacher_student_student_id`(`student_id` ASC) USING BTREE,
  CONSTRAINT `FK_teacher_student_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_teacher_student_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher_student_relations
-- ----------------------------

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `FK_user_roles_role`(`role_id` ASC) USING BTREE,
  CONSTRAINT `FK_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 1);
INSERT INTO `user_roles` VALUES (2, 1);
INSERT INTO `user_roles` VALUES (4, 2);
INSERT INTO `user_roles` VALUES (3, 3);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active',
  `user_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `graduation_school` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '毕业学校',
  `teaching_subjects` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '主要教授科目',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `user_number`(`user_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$X7aPRYS9WF0cGHV9lOJhQO3YfpJiA4SZ5uE5MUmjHjjQEa5LsPzWe', '2025-05-08 16:09:28', NULL, '系统管理员', 'Admin', 'admin@example.com', NULL, NULL, 'active', 'S25951466', NULL, NULL);
INSERT INTO `users` VALUES (2, '1', '$2a$12$niV6hiPbw8ZE1NY8bjAsa.hHqeRCa2ReLI3gK9SYfgAjCjAIFoKsa', '2025-05-09 00:36:03', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25600694', NULL, NULL);
INSERT INTO `users` VALUES (3, '2', '$2a$12$5ezMuR6b7bn60zSE39jUbu8l2kghd0Hg60YuaCkxx1QjYKeTo9xE2', '2025-05-09 00:36:07', '/uploads/20e86439-a7f4-49b6-8bc7-7a5221199356.jpg', '李白', '剑仙', '3401611059@qq.com', '13384403671', '我超级66666666666666666', 'active', 'S25674269', '北京理工大学', '语文，英语');
INSERT INTO `users` VALUES (4, '3', '$2a$12$tziSlw7gvYq8KMOYzYf1E./LwfNYgINd6WXOTlF2zbvf6PVy3r7t6', '2025-05-09 00:36:10', '/uploads/3502ef94-e741-458d-9473-b07a11d7ab30.jpg', NULL, NULL, NULL, NULL, NULL, 'active', 'S25630096', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
