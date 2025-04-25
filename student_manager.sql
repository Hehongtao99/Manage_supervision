/*
 Navicat Premium Dump SQL

 Source Server         : 115.120.221.163
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : 115.120.221.163:3306
 Source Schema         : student_manager

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 23/04/2025 14:34:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bill
-- ----------------------------
DROP TABLE IF EXISTS `bill`;
CREATE TABLE `bill`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `fee_standard_id` bigint NOT NULL COMMENT '费用标准ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '账单金额',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态：0未支付，1已支付',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `payment_method` varchar(20) NULL DEFAULT NULL COMMENT '支付方式：WECHAT微信，ALIPAY支付宝',
  `payment_message` varchar(255) NULL DEFAULT NULL COMMENT '支付消息',
  `due_date` datetime NOT NULL COMMENT '截止日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `sent_time` datetime(6) NOT NULL,
  `conversation_id` bigint NULL DEFAULT NULL,
  `recipient_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKc8ljv426x8fj9tcywei40stu9`(`conversation_id` ASC) USING BTREE,
  INDEX `FK9cy5qdbo924k3jflvj0y04s6y`(`recipient_id` ASC) USING BTREE,
  INDEX `FKgiqeap8ays4lf684x7m0r2729`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `FK9cy5qdbo924k3jflvj0y04s6y` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKc8ljv426x8fj9tcywei40stu9` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKgiqeap8ays4lf684x7m0r2729` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for class_student_relations
-- ----------------------------
DROP TABLE IF EXISTS `class_student_relations`;
CREATE TABLE `class_student_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assign_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active',
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_class_id`(`class_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  UNIQUE INDEX `UK_student_id`(`student_id` ASC) USING BTREE COMMENT '确保学生只能属于一个班级',
  UNIQUE INDEX `UK9p4hgv380mflpmtsrkf3tmtaw`(`class_id` ASC, `student_id` ASC) USING BTREE,
  CONSTRAINT `FK5xl5efgeplpwfe2b7106oi96j` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKlt3w4wsdui1mhlj6psbc54s6p` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for class_teacher_relations
-- ----------------------------
DROP TABLE IF EXISTS `class_teacher_relations`;
CREATE TABLE `class_teacher_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assign_time` datetime(6) NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `class_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKs9q1w24esrnjuc5j5sf9o6fqu`(`class_id` ASC) USING BTREE,
  INDEX `FKa5uni18h2a6b60jlh0r2grgkp`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `FKa5uni18h2a6b60jlh0r2grgkp` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKs9q1w24esrnjuc5j5sf9o6fqu` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for classes
-- ----------------------------
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grade` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for conversations
-- ----------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_time` datetime(6) NOT NULL,
  `last_message_time` datetime(6) NULL DEFAULT NULL,
  `unread_count_user1` int NOT NULL,
  `unread_count_user2` int NOT NULL,
  `user1_id` bigint NOT NULL,
  `user2_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK8wv0rmd8jb3cqcbyng15ubrmk`(`user1_id` ASC) USING BTREE,
  INDEX `FKe7w0k1xem21pp85wxh5moodnk`(`user2_id` ASC) USING BTREE,
  CONSTRAINT `FK8wv0rmd8jb3cqcbyng15ubrmk` FOREIGN KEY (`user1_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKe7w0k1xem21pp85wxh5moodnk` FOREIGN KEY (`user2_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fee_standard
-- ----------------------------
DROP TABLE IF EXISTS `fee_standard`;
CREATE TABLE `fee_standard`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fee_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '费用名称',
  `amount` decimal(10, 2) NOT NULL COMMENT '费用金额',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '费用说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notification_class_relations
-- ----------------------------
DROP TABLE IF EXISTS `notification_class_relations`;
CREATE TABLE `notification_class_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `notification_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1fv5m9u00dj3hj00b0x1jd8l5`(`class_id` ASC) USING BTREE,
  INDEX `FKf99brlbd8ae15b2agj1obd6bc`(`notification_id` ASC) USING BTREE,
  CONSTRAINT `FK1fv5m9u00dj3hj00b0x1jd8l5` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKf99brlbd8ae15b2agj1obd6bc` FOREIGN KEY (`notification_id`) REFERENCES `notifications` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_global` tinyint(1) NOT NULL DEFAULT 0,
  `publish_time` datetime(6) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime(6) NOT NULL,
  `recipient_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `class_id` bigint NULL DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKnnqaodanprg5jr3mvesfh6o5d`(`class_id` ASC) USING BTREE,
  INDEX `FK13vcnq3ukas06ho1yrbc5lrb5`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `FK13vcnq3ukas06ho1yrbc5lrb5` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKnnqaodanprg5jr3mvesfh6o5d` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignee_id` bigint NULL DEFAULT NULL,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `end_time` datetime(6) NULL DEFAULT NULL,
  `requirements` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `resources` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `start_time` datetime(6) NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `supervisor_id` bigint NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `permissions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_ofx66keruapi6vyqpv6f2or37`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for student_class_history
-- ----------------------------
DROP TABLE IF EXISTS `student_class_history`;
CREATE TABLE `student_class_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operation_time` datetime(6) NOT NULL,
  `operation_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `class_id` bigint NOT NULL,
  `operator_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKqo878lje713cy81v5e9epvwql`(`class_id` ASC) USING BTREE,
  INDEX `FKrmgtckvg8jbxtadca6wix4kqa`(`operator_id` ASC) USING BTREE,
  INDEX `FKdtuj61hdqtuabnwj7637rxnj7`(`student_id` ASC) USING BTREE,
  CONSTRAINT `FKdtuj61hdqtuabnwj7637rxnj7` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqo878lje713cy81v5e9epvwql` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKrmgtckvg8jbxtadca6wix4kqa` FOREIGN KEY (`operator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_evaluations
-- ----------------------------
DROP TABLE IF EXISTS `task_evaluations`;
CREATE TABLE `task_evaluations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `evaluated_by` bigint NOT NULL,
  `evaluation_time` datetime(6) NULL DEFAULT NULL,
  `score` int NULL DEFAULT NULL,
  `task_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_submissions
-- ----------------------------
DROP TABLE IF EXISTS `task_submissions`;
CREATE TABLE `task_submissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `submission_time` datetime(6) NOT NULL,
  `submitter_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignee_id` bigint NULL DEFAULT NULL,
  `completed` bit(1) NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `end_time` datetime(6) NULL DEFAULT NULL,
  `priority` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `project_id` bigint NULL DEFAULT NULL,
  `start_time` datetime(6) NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `supervisor_id` bigint NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for teacher_student_relations
-- ----------------------------
DROP TABLE IF EXISTS `teacher_student_relations`;
CREATE TABLE `teacher_student_relations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assign_time` datetime(6) NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `student_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKcfc1v559atfa75e2pixabrb2x`(`student_id` ASC) USING BTREE,
  INDEX `FKnid6wr2g8u9wuj0e9g6eokl3q`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `FKcfc1v559atfa75e2pixabrb2x` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKnid6wr2g8u9wuj0e9g6eokl3q` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for timetable_items
-- ----------------------------
DROP TABLE IF EXISTS `timetable_items`;
CREATE TABLE `timetable_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `timetable_id` bigint NOT NULL,
  `teacher_id` bigint NULL DEFAULT NULL,
  `course_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `day_of_week` int NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `period_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `period_number` int NOT NULL,
  `classroom` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_timetable_items_timetable_id`(`timetable_id` ASC) USING BTREE,
  INDEX `idx_timetable_items_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_timetable_items_day_of_week`(`day_of_week` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for timetables
-- ----------------------------
DROP TABLE IF EXISTS `timetables`;
CREATE TABLE `timetables`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `week_number` int NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_timetables_class_id`(`class_id` ASC) USING BTREE,
  INDEX `idx_timetables_week_number`(`week_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_notifications
-- ----------------------------
DROP TABLE IF EXISTS `user_notifications`;
CREATE TABLE `user_notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `notification_id` bigint NOT NULL COMMENT '通知ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC, `notification_id` ASC) USING BTREE COMMENT '用户-通知唯一索引',
  INDEX `notification_id`(`notification_id` ASC) USING BTREE,
  CONSTRAINT `user_notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_notifications_ibfk_2` FOREIGN KEY (`notification_id`) REFERENCES `notifications` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户通知关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `FKh8ciramu9cc9q3qcqiv4ue8a6`(`role_id` ASC) USING BTREE,
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_r43af9ap4edm43mmtq01oddj6`(`username` ASC) USING BTREE,
  UNIQUE INDEX `UK_g3brvi3cpqs10ebdf6eqh7wv9`(`user_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for parent_child_relations
-- ----------------------------
DROP TABLE IF EXISTS `parent_child_relations`;
CREATE TABLE `parent_child_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL COMMENT '家长ID',
  `child_id` bigint NOT NULL COMMENT '子女ID',
  `relation_type` varchar(20) NOT NULL COMMENT '关系类型：father, mother, guardian',
  `status` varchar(20) NOT NULL COMMENT '关系状态：pending, confirmed, rejected',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_parent_child`(`parent_id`, `child_id`) COMMENT '家长-子女唯一索引',
  CONSTRAINT `fk_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_child_id` FOREIGN KEY (`child_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家长-子女关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for parent_teacher_messages
-- ----------------------------
DROP TABLE IF EXISTS `parent_teacher_messages`;
CREATE TABLE `parent_teacher_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL COMMENT '家长ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '留言内容',
  `reply_content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回复内容',
  `status` varchar(20) NOT NULL DEFAULT 'unread' COMMENT '状态：unread未读, read已读, replied已回复',
  `is_parent_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '家长是否已读回复',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件URL',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件名称',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件类型',
  `file_size` bigint NULL DEFAULT NULL COMMENT '附件大小',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_ptm_parent` FOREIGN KEY (`parent_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ptm_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ptm_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家长教师留言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Procedure structure for transfer_student
-- ----------------------------
DROP PROCEDURE IF EXISTS `transfer_student`;
delimiter ;;
CREATE PROCEDURE `transfer_student`(IN p_student_id BIGINT,
    IN p_new_class_id BIGINT)
BEGIN
    -- 删除学生当前的班级关系
    DELETE FROM `class_student_relations` WHERE `student_id` = p_student_id;
    
    -- 插入新的班级关系
    INSERT INTO `class_student_relations` 
        (`assign_time`, `status`, `class_id`, `student_id`) 
    VALUES 
        (NOW(), 'active', p_new_class_id, p_student_id);
    
    SELECT CONCAT('学生 ', p_student_id, ' 已成功转入班级 ', p_new_class_id) AS message;
END
;;
delimiter ;

-- 插入角色数据
INSERT INTO roles (name, description, permissions, create_time) VALUES ('ADMIN', '管理员', 'admin:all,system:all,user:all,role:all', NOW());
INSERT INTO roles (name, description, permissions, create_time) VALUES ('SUPERVISOR', '教师', 'supervisor:all,student:view', NOW());
INSERT INTO roles (name, description, permissions, create_time) VALUES ('USER', '学生', 'student:all', NOW());
INSERT INTO roles (name, description, permissions, create_time) VALUES ('PARENT', '家长', 'parent:all,student:view', NOW());

SET FOREIGN_KEY_CHECKS = 1;
