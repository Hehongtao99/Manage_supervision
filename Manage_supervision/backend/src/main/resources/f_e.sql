/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : f_e

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 14/05/2025 23:14:32
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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 1, 4, 3, '1', '2025-04-29 09:30:46', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (2, 1, 3, 4, '11', '2025-04-29 09:30:52', 1, NULL, NULL, NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of conversations
-- ----------------------------
INSERT INTO `conversations` VALUES (1, 4, 3, '2025-04-29 09:30:41', '2025-04-29 09:30:52', 0, 0);

-- ----------------------------
-- Table structure for exam_answers
-- ----------------------------
DROP TABLE IF EXISTS `exam_answers`;
CREATE TABLE `exam_answers`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `exam_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_correct` tinyint(1) NULL DEFAULT NULL,
  `score` decimal(38, 2) NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_answers_exam_id`(`exam_id` ASC) USING BTREE,
  INDEX `idx_exam_answers_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_exam_answers_question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `FK36fhn2x04ghokcj68si7iovhi` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK58jpsqvhuexpjnuf5awfaqk9g` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKa5smxoto4oun5q5gffv7ly5yl` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_answers
-- ----------------------------

-- ----------------------------
-- Table structure for exam_questions
-- ----------------------------
DROP TABLE IF EXISTS `exam_questions`;
CREATE TABLE `exam_questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `exam_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `question_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `question_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `question_score` decimal(38, 2) NOT NULL,
  `display_order` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_questions_exam_id`(`exam_id` ASC) USING BTREE,
  INDEX `idx_exam_questions_question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `FK5cd6sjmccb11rrwpyabyc81c0` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKs0t1710in6q97whp93ggrs1wg` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_questions
-- ----------------------------

-- ----------------------------
-- Table structure for exam_students
-- ----------------------------
DROP TABLE IF EXISTS `exam_students`;
CREATE TABLE `exam_students`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `exam_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NOT_STARTED',
  `start_time` datetime NULL DEFAULT NULL,
  `submit_time` datetime NULL DEFAULT NULL,
  `score` decimal(38, 2) NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_students_exam_id`(`exam_id` ASC) USING BTREE,
  INDEX `idx_exam_students_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_exam_students_status`(`status` ASC) USING BTREE,
  CONSTRAINT `FK6r5kumkfq7e03btoddiva3jdt` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKmdnjlfyewwrbddgysm2go0bwl` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_students
-- ----------------------------

-- ----------------------------
-- Table structure for exams
-- ----------------------------
DROP TABLE IF EXISTS `exams`;
CREATE TABLE `exams`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DRAFT',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `duration` int NOT NULL,
  `passing_score` decimal(38, 2) NOT NULL,
  `total_score` decimal(38, 2) NOT NULL,
  `creator_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exams_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_exams_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exams
-- ----------------------------

-- ----------------------------
-- Table structure for notification_recipients
-- ----------------------------
DROP TABLE IF EXISTS `notification_recipients`;
CREATE TABLE `notification_recipients`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notification_id` bigint NOT NULL,
  `recipient_id` bigint NOT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification_recipients
-- ----------------------------

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notifications
-- ----------------------------

-- ----------------------------
-- Table structure for project_evaluations
-- ----------------------------
DROP TABLE IF EXISTS `project_evaluations`;
CREATE TABLE `project_evaluations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `evaluated_by` bigint NOT NULL,
  `evaluation_time` datetime(6) NULL DEFAULT NULL,
  `project_id` bigint NOT NULL,
  `score` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_evaluations
-- ----------------------------

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '未开始',
  `supervisor_id` bigint NULL DEFAULT NULL,
  `assignee_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `requirements` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `resources` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_projects_supervisor_id`(`supervisor_id` ASC) USING BTREE,
  INDEX `idx_projects_assignee_id`(`assignee_id` ASC) USING BTREE,
  CONSTRAINT `FK_projects_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `FK_projects_supervisor` FOREIGN KEY (`supervisor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of projects
-- ----------------------------

-- ----------------------------
-- Table structure for question_bank_questions
-- ----------------------------
DROP TABLE IF EXISTS `question_bank_questions`;
CREATE TABLE `question_bank_questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_bank_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qbq_question_bank_id`(`question_bank_id` ASC) USING BTREE,
  INDEX `idx_qbq_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question_bank_questions
-- ----------------------------

-- ----------------------------
-- Table structure for question_banks
-- ----------------------------
DROP TABLE IF EXISTS `question_banks`;
CREATE TABLE `question_banks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `creator_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_banks_creator_id`(`creator_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question_banks
-- ----------------------------

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `difficulty` int NOT NULL DEFAULT 3,
  `creator_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_questions_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_questions_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of questions
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'ADMIN', '管理员角色，拥有最高权限', ',USER_VIEW,ROLE_VIEW,LOG_VIEW,USER_EDIT,ROLE_EDIT,SYSTEM_SETTINGS,USER_DELETE,ROLE_DELETE', '2025-04-27 13:40:59');
INSERT INTO `roles` VALUES (2, 'USER', '学生角色，基本用户权限', '', '2025-04-27 13:40:59');
INSERT INTO `roles` VALUES (3, 'SUPERVISOR', '督导员角色，可以管理学生', ',USER_VIEW,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW,QUESTION_MANAGE,QUESTION_BANK_MANAGE,EXAM_MANAGE', '2025-04-27 13:40:59');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of teacher_student_relations
-- ----------------------------
INSERT INTO `teacher_student_relations` VALUES (1, 3, 4, '2025-05-14 22:43:19', 'active');
INSERT INTO `teacher_student_relations` VALUES (2, 3, 5, '2025-05-14 22:19:43', 'active');
INSERT INTO `teacher_student_relations` VALUES (3, 3, 6, '2025-05-14 22:45:13', 'active');

-- ----------------------------
-- Table structure for user_face_features
-- ----------------------------
DROP TABLE IF EXISTS `user_face_features`;
CREATE TABLE `user_face_features`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `face_feature` mediumblob NOT NULL,
  `face_image` mediumblob NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `feature_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_face_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_face_features
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 1);
INSERT INTO `user_roles` VALUES (2, 1);
INSERT INTO `user_roles` VALUES (4, 2);
INSERT INTO `user_roles` VALUES (5, 2);
INSERT INTO `user_roles` VALUES (6, 2);
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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `user_number`(`user_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$X7aPRYS9WF0cGHV9lOJhQO3YfpJiA4SZ5uE5MUmjHjjQEa5LsPzWe', '2025-04-27 13:40:59', NULL, '系统管理员', 'Admin', 'admin@example.com', NULL, NULL, 'active', 'S25936566');
INSERT INTO `users` VALUES (2, '1', '$2a$12$jZS4B9MixyDpsU8JlZYZP.ZEQua8NPKNq9fBltEssgYW/NRTRvgTK', '2025-04-27 21:41:44', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25743230');
INSERT INTO `users` VALUES (3, '2', '$2a$12$U/sD8AUqd29HtXDcReP20.LzdAib7EaEEWQHuvl3YBVq9zg49Sf8m', '2025-04-27 21:42:17', '/uploads/81feeff3-5cab-4215-b636-8c4b259f2e49.png', '测试', '', NULL, NULL, '', 'active', 'T2502072');
INSERT INTO `users` VALUES (4, '3', '$2a$12$KJi5Y69SyZezTfDiNrvAoOWI2Av5yfKwSfZIuVtd.1bmS9xgOTMXy', '2025-04-27 21:42:24', '/uploads/5987f5ec-7acc-416a-8904-a8b3db6e2b11.png', '真实姓名', '昵称', '1@1.com', '13384403671', '', 'active', 'S25521158');
INSERT INTO `users` VALUES (5, '4', '$2a$12$EfTLtUlGG2VqvR7xvcIoYOLj.WIJMkBNOoAOWSZJu3lln2DkEE33i', '2025-05-07 07:07:17', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25422418');
INSERT INTO `users` VALUES (6, '5', '$2a$12$IJD9hUG6e40tRHFbnZi1heN2FUfjJW5aHF75cyOKcKNs32F2AHgt2', '2025-05-13 16:42:09', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25999454');

SET FOREIGN_KEY_CHECKS = 1;
