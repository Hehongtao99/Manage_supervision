/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : 560

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 25/05/2025 17:08:13
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
  INDEX `idx_chat_messages_recipient_id`(`recipient_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------

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
  INDEX `idx_conversations_user2_id`(`user2_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course_categories
-- ----------------------------
DROP TABLE IF EXISTS `course_categories`;
CREATE TABLE `course_categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类别名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '类别描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_name`(`category_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程类别表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_categories
-- ----------------------------
INSERT INTO `course_categories` VALUES (1, '计算机科学', '计算机科学相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (2, '数学', '数学相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (3, '英语', '英语相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (4, '物理', '物理相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (5, '化学', '化学相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (6, '生物', '生物相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (7, '历史', '历史相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (8, '地理', '地理相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (9, '政治', '政治相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (10, '经济学', '经济学相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (11, '管理学', '管理学相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (12, '艺术设计', '艺术设计相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (13, '体育', '体育相关课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (14, '其他', '其他类别课程', 'active', '2025-05-25 08:26:34', '2025-05-25 08:26:34', NULL, NULL);
INSERT INTO `course_categories` VALUES (15, '12', '12', 'active', '2025-05-25 16:31:42', '2025-05-25 16:31:42', NULL, NULL);

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `course_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程描述',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '教师ID',
  `teacher_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '教师姓名',
  `course_duration` int NOT NULL COMMENT '课程时长（小时）',
  `course_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程类别',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-活跃，inactive-停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_category`(`course_category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of courses
-- ----------------------------
INSERT INTO `courses` VALUES (1, '123', '123', 3, '2', 1, '计算机科学', 'active', '2025-05-25 16:37:14', '2025-05-25 16:37:14', NULL, NULL);

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
INSERT INTO `roles` VALUES (1, 'ADMIN', '管理员角色，拥有最高权限', 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS', '2025-05-25 06:30:03');
INSERT INTO `roles` VALUES (2, 'USER', '学生角色，基本用户权限', 'USER_VIEW', '2025-05-25 06:30:03');
INSERT INTO `roles` VALUES (3, 'SUPERVISOR', '督导员角色，可以管理学生', 'USER_VIEW,USER_EDIT,STUDENT_MANAGEMENT,STUDENT_PROGRESS_VIEW', '2025-05-25 06:30:03');

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
  INDEX `idx_teacher_student_student_id`(`student_id` ASC) USING BTREE
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
  INDEX `FK_user_roles_role`(`role_id` ASC) USING BTREE
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
  `college_id` bigint NULL DEFAULT NULL COMMENT '所属学院ID',
  `major_id` bigint NULL DEFAULT NULL COMMENT '所属专业ID',
  `class_id` bigint NULL DEFAULT NULL COMMENT '所属班级ID（学生专用）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `user_number`(`user_number` ASC) USING BTREE,
  INDEX `idx_college_id`(`college_id` ASC),
  INDEX `idx_major_id`(`major_id` ASC),
  INDEX `idx_class_id`(`class_id` ASC)
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$X7aPRYS9WF0cGHV9lOJhQO3YfpJiA4SZ5uE5MUmjHjjQEa5LsPzWe', '2025-05-25 06:30:03', NULL, '系统管理员', 'Admin', 'admin@example.com', NULL, NULL, 'active', 'S25656740', NULL, NULL, NULL);
INSERT INTO `users` VALUES (2, '1', '$2a$12$33gUcD8oK8z6Ys.kMhdJ4.CormaGdyNp/lulOVKBqhGGUpqC91N3m', '2025-05-25 14:41:53', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25745772', NULL, NULL, NULL);
INSERT INTO `users` VALUES (3, '2', '$2a$12$qfggQ7GQca3NeOsk8rCWkePCA8TXcks/Z0vV5G3N8QtvyS6V/jYYa', '2025-05-25 14:42:40', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'T2547440', NULL, NULL, NULL);
INSERT INTO `users` VALUES (4, '3', '$2a$12$FiERG9jRGGF3RPn36t.Io.d8jidAK8Qqvkpb9P5KD0iRKUzaU.iMO', '2025-05-25 14:42:43', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25176110', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for colleges
-- ----------------------------
DROP TABLE IF EXISTS `colleges`;
CREATE TABLE `colleges`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学院名称',
  `college_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学院代码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学院描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_college_name`(`college_name` ASC) USING BTREE,
  UNIQUE INDEX `uk_college_code`(`college_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学院表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for majors
-- ----------------------------
DROP TABLE IF EXISTS `majors`;
CREATE TABLE `majors`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `major_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专业名称',
  `major_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专业代码',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '专业描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_major_code`(`major_code` ASC) USING BTREE,
  INDEX `idx_college_id`(`college_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for classes
-- ----------------------------
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级名称',
  `class_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '班级代码',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `major_id` bigint NOT NULL COMMENT '所属专业ID',
  `grade` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '年级',
  `student_count` int NOT NULL DEFAULT 0 COMMENT '学生人数',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '班级描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_code`(`class_code` ASC) USING BTREE,
  INDEX `idx_college_id`(`college_id` ASC) USING BTREE,
  INDEX `idx_major_id`(`major_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '班级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for teacher_class_relations
-- ----------------------------
DROP TABLE IF EXISTS `teacher_class_relations`;
CREATE TABLE `teacher_class_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `assign_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_class`(`teacher_id` ASC, `class_id` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_class_id`(`class_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师班级关系表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
