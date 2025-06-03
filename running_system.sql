/*
 Navicat Premium Dump SQL

 Source Server         : 115.120.221.163
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : 115.120.221.163:3306
 Source Schema         : running_system

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 29/05/2025 08:28:07
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
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 1, 2, 3, '11', '2025-05-02 19:59:57', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (2, 1, 3, 2, '？', '2025-05-02 20:00:02', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (3, 1, 3, 2, '1', '2025-05-06 20:31:31', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (4, 1, 3, 2, '1', '2025-05-06 21:05:14', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (5, 1, 3, 2, '1', '2025-05-06 21:05:22', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (6, 1, 3, 2, '1', '2025-05-06 21:05:24', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (7, 1, 3, 2, '？', '2025-05-06 21:05:32', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (8, 1, 3, 2, '2', '2025-05-06 21:05:55', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (9, 1, 3, 2, '1', '2025-05-06 21:09:40', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (10, 1, 3, 2, '、', '2025-05-06 21:09:42', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (11, 1, 3, 2, '1', '2025-05-06 21:09:46', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (12, 1, 3, 2, '1', '2025-05-06 21:10:01', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (13, 1, 3, 2, '1', '2025-05-06 21:10:34', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (14, 1, 3, 2, '1', '2025-05-06 21:10:59', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (15, 1, 3, 2, '1', '2025-05-06 21:11:08', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (16, 1, 3, 2, '1', '2025-05-06 21:11:51', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (17, 1, 2, 3, '1', '2025-05-06 21:14:18', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (18, 1, 3, 2, '1', '2025-05-06 21:14:45', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (19, 1, 3, 2, '1', '2025-05-06 21:14:47', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (20, 1, 2, 3, '1', '2025-05-13 11:25:15', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (21, 1, 2, 3, 'www', '2025-05-13 15:03:56', 1, NULL, NULL, NULL, NULL);
INSERT INTO `chat_messages` VALUES (22, 1, 3, 2, '？', '2025-05-22 19:31:13', 0, NULL, NULL, NULL, NULL);

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
INSERT INTO `conversations` VALUES (1, 3, 2, '2025-05-02 19:19:35', '2025-05-22 19:31:13', 0, 1);

-- ----------------------------
-- Table structure for friend_requests
-- ----------------------------
DROP TABLE IF EXISTS `friend_requests`;
CREATE TABLE `friend_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '请求ID',
  `from_user_id` bigint NOT NULL COMMENT '发送请求的用户ID',
  `to_user_id` bigint NOT NULL COMMENT '接收请求的用户ID',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求消息',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已接受，2-已拒绝',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_user_id`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_to_user_id`(`to_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_request_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_request_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友请求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of friend_requests
-- ----------------------------
INSERT INTO `friend_requests` VALUES (1, 2, 3, '', 2, '2025-05-06 20:12:13', '2025-05-06 20:24:44');
INSERT INTO `friend_requests` VALUES (2, 2, 3, '', 1, '2025-05-06 20:27:04', '2025-05-06 20:27:08');
INSERT INTO `friend_requests` VALUES (3, 5, 3, '', 1, '2025-05-06 20:55:26', '2025-05-06 20:55:29');
INSERT INTO `friend_requests` VALUES (4, 3, 7, '', 1, '2025-05-13 11:53:58', '2025-05-13 11:54:06');
INSERT INTO `friend_requests` VALUES (5, 2, 3, '', 0, '2025-05-28 21:20:13', '2025-05-28 21:20:13');
INSERT INTO `friend_requests` VALUES (6, 2, 7, '', 0, '2025-05-28 21:20:21', '2025-05-28 21:20:21');

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
INSERT INTO `roles` VALUES (1, 'ADMIN', '管理员角色，拥有跑步管理系统的所有权限', 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS,RUNNING_MANAGEMENT,SOCIAL_MANAGEMENT', '2025-05-02 06:12:33');
INSERT INTO `roles` VALUES (2, 'USER', '跑步爱好者角色，可以记录跑步数据、参与社交活动', 'USER_VIEW,RUNNING_RECORD,SOCIAL_INTERACT', '2025-05-02 06:12:33');

-- ----------------------------
-- Table structure for running_preferences
-- ----------------------------
DROP TABLE IF EXISTS `running_preferences`;
CREATE TABLE `running_preferences`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `frequency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `preferred_distance` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `pace` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `environment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `motto` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_running_preferences_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_running_preferences_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK_running_preferences_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of running_preferences
-- ----------------------------
INSERT INTO `running_preferences` VALUES (1, 2, '每周3-5次', '5公里以内', '5-6分钟/公里', '公园', '啊啊啊啊啊啊啊啊啊啊啊', '2025-05-02 15:13:51', '2025-05-02 15:13:52');

-- ----------------------------
-- Table structure for running_records
-- ----------------------------
DROP TABLE IF EXISTS `running_records`;
CREATE TABLE `running_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `distance` decimal(10, 2) NOT NULL COMMENT '跑步距离，单位：公里',
  `duration` int NULL DEFAULT NULL COMMENT '跑步时长，单位：秒',
  `pace` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配速，格式：分钟:秒/公里',
  `record_date` date NOT NULL COMMENT '记录日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `record_datetime` datetime NULL DEFAULT NULL COMMENT '记录时间（精确到秒）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_running_records_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_running_records_record_date`(`record_date` ASC) USING BTREE,
  INDEX `idx_running_records_datetime`(`record_datetime` ASC) USING BTREE,
  INDEX `idx_running_records_user_datetime`(`user_id` ASC, `record_datetime` DESC) USING BTREE,
  CONSTRAINT `FK_running_records_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of running_records
-- ----------------------------
INSERT INTO `running_records` VALUES (1, 2, 6.00, 25, '4:10', '2025-05-02', '2025-05-02 16:10:45', '2025-05-28 13:23:34', '2025-05-02 00:00:00');
INSERT INTO `running_records` VALUES (2, 2, 5.23, 35, '6:42', '2025-05-02', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-05-02 00:00:00');
INSERT INTO `running_records` VALUES (3, 2, 3.11, 19, '6:07', '2025-04-30', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-30 00:00:00');
INSERT INTO `running_records` VALUES (4, 2, 8.45, 52, '6:09', '2025-04-28', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-28 00:00:00');
INSERT INTO `running_records` VALUES (5, 2, 4.78, 30, '6:16', '2025-04-25', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-25 00:00:00');
INSERT INTO `running_records` VALUES (6, 2, 6.32, 42, '6:39', '2025-04-23', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-23 00:00:00');
INSERT INTO `running_records` VALUES (7, 2, 5.91, 37, '6:15', '2025-04-22', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-22 00:00:00');
INSERT INTO `running_records` VALUES (8, 2, 7.14, 46, '6:26', '2025-04-17', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-17 00:00:00');
INSERT INTO `running_records` VALUES (9, 2, 3.56, 21, '5:53', '2025-04-16', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-16 00:00:00');
INSERT INTO `running_records` VALUES (10, 2, 5.02, 33, '6:34', '2025-04-14', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-14 00:00:00');
INSERT INTO `running_records` VALUES (11, 2, 6.75, 42, '6:13', '2025-04-11', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-11 00:00:00');
INSERT INTO `running_records` VALUES (12, 2, 4.33, 28, '6:28', '2025-04-09', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-09 00:00:00');
INSERT INTO `running_records` VALUES (13, 2, 5.67, 36, '6:21', '2025-04-07', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-07 00:00:00');
INSERT INTO `running_records` VALUES (14, 2, 4.21, 26, '6:10', '2025-03-31', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-31 00:00:00');
INSERT INTO `running_records` VALUES (15, 2, 7.88, 51, '6:29', '2025-03-28', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-28 00:00:00');
INSERT INTO `running_records` VALUES (16, 2, 5.46, 35, '6:24', '2025-03-25', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-25 00:00:00');
INSERT INTO `running_records` VALUES (17, 2, 6.33, 41, '6:29', '2025-03-23', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-23 00:00:00');
INSERT INTO `running_records` VALUES (18, 2, 3.22, 21, '6:31', '2025-03-21', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-21 00:00:00');
INSERT INTO `running_records` VALUES (19, 2, 8.10, 53, '6:33', '2025-03-18', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-18 00:00:00');
INSERT INTO `running_records` VALUES (20, 2, 5.76, 38, '6:36', '2025-03-11', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-11 00:00:00');
INSERT INTO `running_records` VALUES (21, 2, 4.89, 32, '6:33', '2025-03-08', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-08 00:00:00');
INSERT INTO `running_records` VALUES (22, 2, 7.21, 44, '6:06', '2025-03-05', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-05 00:00:00');
INSERT INTO `running_records` VALUES (23, 2, 3.45, 23, '6:40', '2025-03-03', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-03 00:00:00');
INSERT INTO `running_records` VALUES (24, 2, 6.78, 43, '6:20', '2025-03-01', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-01 00:00:00');
INSERT INTO `running_records` VALUES (25, 2, 5.12, 33, '6:26', '2025-02-26', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-02-26 00:00:00');
INSERT INTO `running_records` VALUES (26, 2, 4.56, 29, '6:22', '2025-02-19', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-02-19 00:00:00');
INSERT INTO `running_records` VALUES (27, 2, 7.33, 47, '6:25', '2025-02-16', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-02-16 00:00:00');
INSERT INTO `running_records` VALUES (28, 2, 5.89, 36, '6:07', '2025-02-13', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-02-13 00:00:00');
INSERT INTO `running_records` VALUES (29, 2, 6.44, 42, '6:31', '2025-02-11', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-02-11 00:00:00');
INSERT INTO `running_records` VALUES (30, 2, 3.77, 25, '6:37', '2025-02-06', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-02-06 00:00:00');
INSERT INTO `running_records` VALUES (31, 2, 8.22, 50, '6:04', '2025-02-02', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-02-02 00:00:00');
INSERT INTO `running_records` VALUES (32, 2, 6.12, 33, '5:23', '2025-05-01', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-05-01 00:00:00');
INSERT INTO `running_records` VALUES (33, 2, 4.33, 23, '5:19', '2025-04-29', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-29 00:00:00');
INSERT INTO `running_records` VALUES (34, 2, 9.27, 48, '5:11', '2025-04-27', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-27 00:00:00');
INSERT INTO `running_records` VALUES (35, 2, 5.46, 28, '5:08', '2025-04-24', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-24 00:00:00');
INSERT INTO `running_records` VALUES (36, 2, 7.85, 41, '5:13', '2025-04-22', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-22 00:00:00');
INSERT INTO `running_records` VALUES (37, 2, 6.33, 34, '5:22', '2025-04-20', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-20 00:00:00');
INSERT INTO `running_records` VALUES (38, 2, 8.75, 46, '5:17', '2025-04-17', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-17 00:00:00');
INSERT INTO `running_records` VALUES (39, 2, 4.22, 22, '5:14', '2025-04-14', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-14 00:00:00');
INSERT INTO `running_records` VALUES (40, 2, 6.91, 37, '5:21', '2025-04-12', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-04-12 00:00:00');
INSERT INTO `running_records` VALUES (41, 2, 5.33, 28, '5:15', '2025-03-31', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-31 00:00:00');
INSERT INTO `running_records` VALUES (42, 2, 8.45, 44, '5:12', '2025-03-28', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-28 00:00:00');
INSERT INTO `running_records` VALUES (43, 2, 6.77, 35, '5:10', '2025-03-25', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-25 00:00:00');
INSERT INTO `running_records` VALUES (44, 2, 7.12, 37, '5:12', '2025-03-22', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-22 00:00:00');
INSERT INTO `running_records` VALUES (45, 2, 3.56, 19, '5:20', '2025-03-19', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-19 00:00:00');
INSERT INTO `running_records` VALUES (46, 2, 10.21, 53, '5:11', '2025-03-16', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-16 00:00:00');
INSERT INTO `running_records` VALUES (47, 2, 6.33, 33, '5:13', '2025-03-11', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-11 00:00:00');
INSERT INTO `running_records` VALUES (48, 2, 5.44, 28, '5:09', '2025-03-08', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-08 00:00:00');
INSERT INTO `running_records` VALUES (49, 2, 8.90, 47, '5:17', '2025-03-05', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-05 00:00:00');
INSERT INTO `running_records` VALUES (50, 2, 4.35, 23, '5:18', '2025-03-02', '2025-05-02 08:20:53', '2025-05-28 13:23:34', '2025-03-02 00:00:00');
INSERT INTO `running_records` VALUES (51, 2, 100.00, 1200, '12:00', '2025-05-06', '2025-05-06 21:15:17', '2025-05-28 13:23:34', '2025-05-06 00:00:00');
INSERT INTO `running_records` VALUES (52, 3, 10.00, 120, '12:00', '2025-05-13', '2025-05-13 11:58:49', '2025-05-28 13:23:34', '2025-05-13 00:00:00');
INSERT INTO `running_records` VALUES (53, 3, 5.00, 82, '16:24', '2025-05-14', '2025-05-13 11:59:11', '2025-05-28 13:23:34', '2025-05-14 00:00:00');
INSERT INTO `running_records` VALUES (54, 3, 7.00, 80, '11:26', '2025-05-15', '2025-05-13 12:00:36', '2025-05-28 13:23:34', '2025-05-15 00:00:00');
INSERT INTO `running_records` VALUES (55, 3, 0.10, 1, '10:00', '2025-05-23', '2025-05-23 23:57:43', '2025-05-28 13:23:34', '2025-05-23 00:00:00');
INSERT INTO `running_records` VALUES (56, 3, 12.00, 11553, '16:03', '2025-05-28', '2025-05-28 21:34:18', '2025-05-28 21:34:18', '2025-05-28 21:31:16');

-- ----------------------------
-- Table structure for running_steps
-- ----------------------------
DROP TABLE IF EXISTS `running_steps`;
CREATE TABLE `running_steps`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `distance` decimal(10, 2) NOT NULL COMMENT '距离，单位：公里',
  `duration` int NOT NULL COMMENT '时长，单位：分钟',
  `pace` decimal(10, 2) NOT NULL COMMENT '配速，单位：分钟/公里',
  `run_date` date NOT NULL COMMENT '跑步日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_running_steps_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_running_steps_run_date`(`run_date` ASC) USING BTREE,
  CONSTRAINT `FK_running_steps_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of running_steps
-- ----------------------------

-- ----------------------------
-- Table structure for social_post_comments
-- ----------------------------
DROP TABLE IF EXISTS `social_post_comments`;
CREATE TABLE `social_post_comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父评论ID，如果是一级评论则为0',
  `reply_user_id` bigint NOT NULL DEFAULT 0 COMMENT '被回复的用户ID，如果是一级评论则为0',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '朋友圈帖子评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of social_post_comments
-- ----------------------------
INSERT INTO `social_post_comments` VALUES (1, 1, 2, '牛逼', 0, 0, 0, '2025-05-02 17:07:04', '2025-05-02 17:07:04', 0);
INSERT INTO `social_post_comments` VALUES (2, 9, 2, '牛逼牛逼', 0, 0, 0, '2025-05-02 20:08:18', '2025-05-02 20:08:18', 0);
INSERT INTO `social_post_comments` VALUES (3, 9, 3, '你也', 0, 0, 0, '2025-05-02 20:08:44', '2025-05-02 20:08:44', 0);
INSERT INTO `social_post_comments` VALUES (4, 9, 3, '1', 0, 0, 0, '2025-05-02 20:08:51', '2025-05-02 20:08:58', 1);
INSERT INTO `social_post_comments` VALUES (5, 9, 2, '11\n', 0, 0, 0, '2025-05-06 21:15:36', '2025-05-06 21:15:36', 0);
INSERT INTO `social_post_comments` VALUES (6, 10, 2, '1', 0, 0, 0, '2025-05-06 21:19:43', '2025-05-06 21:19:43', 0);
INSERT INTO `social_post_comments` VALUES (7, 10, 2, '2', 6, 2, 0, '2025-05-06 21:19:47', '2025-05-06 21:19:47', 0);
INSERT INTO `social_post_comments` VALUES (8, 10, 2, '1', 6, 2, 0, '2025-05-06 21:19:59', '2025-05-06 21:19:59', 0);
INSERT INTO `social_post_comments` VALUES (9, 10, 2, '2', 6, 2, 0, '2025-05-06 21:22:41', '2025-05-06 21:22:41', 0);
INSERT INTO `social_post_comments` VALUES (10, 10, 2, '2', 0, 0, 0, '2025-05-06 21:22:53', '2025-05-06 21:22:53', 0);
INSERT INTO `social_post_comments` VALUES (11, 10, 3, '1', 10, 2, 0, '2025-05-06 21:23:02', '2025-05-06 21:23:02', 0);
INSERT INTO `social_post_comments` VALUES (12, 10, 2, '1', 6, 2, 0, '2025-05-06 21:23:26', '2025-05-06 21:23:26', 0);
INSERT INTO `social_post_comments` VALUES (13, 10, 2, '12', 10, 2, 0, '2025-05-06 21:25:51', '2025-05-06 21:25:51', 0);
INSERT INTO `social_post_comments` VALUES (14, 10, 3, '111', 0, 0, 0, '2025-05-07 14:26:33', '2025-05-07 14:26:33', 0);

-- ----------------------------
-- Table structure for social_post_forwards
-- ----------------------------
DROP TABLE IF EXISTS `social_post_forwards`;
CREATE TABLE `social_post_forwards`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '转发用户ID',
  `original_post_id` bigint NOT NULL COMMENT '原始帖子ID',
  `new_post_id` bigint NOT NULL COMMENT '新帖子ID',
  `forward_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '转发评论',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_original_post_id`(`original_post_id` ASC) USING BTREE,
  INDEX `idx_new_post_id`(`new_post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '朋友圈帖子转发关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of social_post_forwards
-- ----------------------------
INSERT INTO `social_post_forwards` VALUES (1, 3, 10, 13, '你真的牛逼', '2025-05-07 14:48:42', '2025-05-07 14:48:42', 0);
INSERT INTO `social_post_forwards` VALUES (2, 3, 10, 14, '1', '2025-05-07 14:51:54', '2025-05-07 14:51:54', 0);
INSERT INTO `social_post_forwards` VALUES (3, 3, 10, 15, '666', '2025-05-07 15:07:19', '2025-05-07 15:07:19', 0);
INSERT INTO `social_post_forwards` VALUES (4, 2, 15, 16, '6', '2025-05-07 15:10:48', '2025-05-07 15:10:48', 0);
INSERT INTO `social_post_forwards` VALUES (5, 3, 10, 17, '1', '2025-05-07 15:17:39', '2025-05-07 15:17:39', 0);
INSERT INTO `social_post_forwards` VALUES (6, 3, 10, 18, '666', '2025-05-07 15:52:12', '2025-05-07 15:52:12', 0);

-- ----------------------------
-- Table structure for social_post_images
-- ----------------------------
DROP TABLE IF EXISTS `social_post_images`;
CREATE TABLE `social_post_images`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '朋友圈帖子图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of social_post_images
-- ----------------------------
INSERT INTO `social_post_images` VALUES (1, 1, '/uploads/social/20250502/bc9b81a2-24c1-4b94-8427-a789231f3337.jpg', 0, '2025-05-02 17:06:48', '2025-05-02 17:06:48');
INSERT INTO `social_post_images` VALUES (2, 1, '/uploads/social/20250502/3e3b05a7-b21c-4583-9a73-b439ba88d94c.png', 1, '2025-05-02 17:06:48', '2025-05-02 17:06:48');
INSERT INTO `social_post_images` VALUES (3, 4, '/uploads/social/20250502/5f6b174f-d240-4b83-b382-048f656562f1.jpg', 0, '2025-05-02 17:25:08', '2025-05-02 17:25:08');
INSERT INTO `social_post_images` VALUES (7, 8, '/uploads/social/20250502/c17309d5-8138-4072-98c2-5fe95f75bd27.jpg', 0, '2025-05-02 18:06:11', '2025-05-02 18:06:11');
INSERT INTO `social_post_images` VALUES (8, 9, '/uploads/social/20250502/dc3fa9ca-997f-48f4-8c18-b64da434880a.png', 0, '2025-05-02 20:08:01', '2025-05-02 20:08:01');
INSERT INTO `social_post_images` VALUES (11, 10, '/uploads/social/20250513/532a5151-9bda-4389-aab7-50e5a1262b9d.jpg', 0, '2025-05-13 11:39:50', '2025-05-13 11:39:50');

-- ----------------------------
-- Table structure for social_post_likes
-- ----------------------------
DROP TABLE IF EXISTS `social_post_likes`;
CREATE TABLE `social_post_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '朋友圈帖子点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of social_post_likes
-- ----------------------------
INSERT INTO `social_post_likes` VALUES (1, 1, 2, '2025-05-02 17:06:55');
INSERT INTO `social_post_likes` VALUES (2, 9, 2, '2025-05-02 20:08:08');
INSERT INTO `social_post_likes` VALUES (3, 10, 3, '2025-05-06 21:16:03');
INSERT INTO `social_post_likes` VALUES (5, 17, 3, '2025-05-22 09:41:39');
INSERT INTO `social_post_likes` VALUES (6, 18, 3, '2025-05-22 17:25:48');

-- ----------------------------
-- Table structure for social_post_running_records
-- ----------------------------
DROP TABLE IF EXISTS `social_post_running_records`;
CREATE TABLE `social_post_running_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `running_record_id` bigint NOT NULL COMMENT '跑步记录ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_running_record_id`(`running_record_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '朋友圈帖子关联跑步记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of social_post_running_records
-- ----------------------------
INSERT INTO `social_post_running_records` VALUES (1, 2, 1, '2025-05-02 17:21:32');
INSERT INTO `social_post_running_records` VALUES (2, 4, 1, '2025-05-02 17:25:08');
INSERT INTO `social_post_running_records` VALUES (3, 5, 1, '2025-05-02 17:30:52');
INSERT INTO `social_post_running_records` VALUES (4, 6, 32, '2025-05-02 17:33:18');
INSERT INTO `social_post_running_records` VALUES (5, 7, 32, '2025-05-02 17:37:04');
INSERT INTO `social_post_running_records` VALUES (8, 8, 34, '2025-05-02 18:06:11');
INSERT INTO `social_post_running_records` VALUES (11, 10, 51, '2025-05-13 11:39:50');

-- ----------------------------
-- Table structure for social_posts
-- ----------------------------
DROP TABLE IF EXISTS `social_posts`;
CREATE TABLE `social_posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '发布者用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '帖子内容',
  `visibility` tinyint NOT NULL DEFAULT 0 COMMENT '可见范围：0-全部可见，1-仅好友可见',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置信息',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数量',
  `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
  `forward_count` int NULL DEFAULT 0 COMMENT '转发数量',
  `is_forward` int NULL DEFAULT 0 COMMENT '是否是转发的帖子，0-原创，1-转发',
  `original_post_id` bigint NULL DEFAULT 0 COMMENT '原始帖子ID，如果是原创则为0',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '朋友圈帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of social_posts
-- ----------------------------
INSERT INTO `social_posts` VALUES (1, 2, 'QQ群前前前前', 0, NULL, 1, 1, '2025-05-02 17:06:48', '2025-05-02 17:06:48', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (2, 2, '我完成了 6公里 的跑步，用时 25分钟，配速 4:10。', 1, '', 0, 0, '2025-05-02 17:21:31', '2025-05-02 17:21:31', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (3, 2, '测试选择跑步记录', 1, '', 0, 0, '2025-05-02 17:22:10', '2025-05-02 17:22:10', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (4, 2, '有点垃圾了', 1, '', 0, 0, '2025-05-02 17:25:07', '2025-05-02 17:25:07', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (5, 2, '1', 1, '', 0, 0, '2025-05-02 17:30:51', '2025-05-02 17:30:51', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (6, 2, '11111111', 1, '', 0, 0, '2025-05-02 17:33:18', '2025-05-02 17:33:18', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (7, 2, '去', 1, '', 0, 0, '2025-05-02 17:37:04', '2025-05-02 17:37:04', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (8, 2, '111', 1, '', 0, 0, '2025-05-02 17:42:27', '2025-05-02 18:06:11', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (9, 3, '1', 1, '', 1, 3, '2025-05-02 20:08:01', '2025-05-02 20:08:01', 0, 0, 0, 0);
INSERT INTO `social_posts` VALUES (10, 2, '开始跑步', 1, '', 1, 9, '2025-05-06 21:15:55', '2025-05-13 11:39:50', 0, 5, 0, 0);
INSERT INTO `social_posts` VALUES (13, 3, '你真的牛逼', 1, NULL, 0, 0, '2025-05-07 14:48:42', '2025-05-07 15:05:40', 1, 0, 1, 10);
INSERT INTO `social_posts` VALUES (14, 3, '1', 1, NULL, 0, 0, '2025-05-07 14:51:54', '2025-05-07 15:05:35', 1, 0, 1, 10);
INSERT INTO `social_posts` VALUES (15, 3, '666', 1, NULL, 0, 0, '2025-05-07 15:07:19', '2025-05-07 15:07:19', 0, 1, 1, 10);
INSERT INTO `social_posts` VALUES (16, 2, '6', 1, NULL, 0, 0, '2025-05-07 15:10:48', '2025-05-07 15:11:07', 1, 0, 1, 15);
INSERT INTO `social_posts` VALUES (17, 3, '1', 1, NULL, 1, 0, '2025-05-07 15:17:39', '2025-05-07 15:17:39', 0, 0, 1, 10);
INSERT INTO `social_posts` VALUES (18, 3, '666', 1, NULL, 1, 0, '2025-05-07 15:52:12', '2025-05-20 17:41:52', 0, 0, 1, 10);

-- ----------------------------
-- Table structure for social_takedowns
-- ----------------------------
DROP TABLE IF EXISTS `social_takedowns`;
CREATE TABLE `social_takedowns`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `content_type` tinyint NOT NULL COMMENT '内容类型：1-帖子，2-评论',
  `content_id` bigint NOT NULL COMMENT '内容ID（帖子ID或评论ID）',
  `admin_id` bigint NOT NULL COMMENT '操作管理员ID',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架原因',
  `takedown_time` datetime NOT NULL COMMENT '下架时间',
  `restore_time` datetime NULL DEFAULT NULL COMMENT '恢复时间，为null表示未恢复',
  `restore_admin_id` bigint NULL DEFAULT NULL COMMENT '恢复操作管理员ID，为null表示未恢复',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_content`(`content_type` ASC, `content_id` ASC) USING BTREE,
  INDEX `idx_admin`(`admin_id` ASC) USING BTREE,
  INDEX `idx_takedown_time`(`takedown_time` ASC) USING BTREE,
  INDEX `idx_restore_time`(`restore_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社交内容下架记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of social_takedowns
-- ----------------------------
INSERT INTO `social_takedowns` VALUES (1, 1, 18, 4, '', '2025-05-20 17:41:48', '2025-05-20 17:41:52', 4, '2025-05-20 17:41:48', '2025-05-20 17:41:52');

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
-- Table structure for user_friends
-- ----------------------------
DROP TABLE IF EXISTS `user_friends`;
CREATE TABLE `user_friends`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '好友关系ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_friend_id`(`friend_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_friends
-- ----------------------------
INSERT INTO `user_friends` VALUES (5, 2, 3, '2025-05-02 20:07:04');
INSERT INTO `user_friends` VALUES (6, 3, 2, '2025-05-02 20:07:04');

-- ----------------------------
-- Table structure for user_friendships
-- ----------------------------
DROP TABLE IF EXISTS `user_friendships`;
CREATE TABLE `user_friendships`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `status` int NULL DEFAULT 1 COMMENT '好友关系状态：0-待接受，1-已接受',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id` ASC, `friend_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_friend_id`(`friend_id` ASC) USING BTREE,
  CONSTRAINT `fk_friendship_friend` FOREIGN KEY (`friend_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_friendship_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_friendships
-- ----------------------------
INSERT INTO `user_friendships` VALUES (3, 5, 3, '2025-05-06 20:55:29', 1, 0);
INSERT INTO `user_friendships` VALUES (4, 3, 5, '2025-05-06 20:55:29', 1, 0);
INSERT INTO `user_friendships` VALUES (5, 3, 7, '2025-05-13 11:54:06', 1, 0);
INSERT INTO `user_friendships` VALUES (6, 7, 3, '2025-05-13 11:54:06', 1, 0);

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
INSERT INTO `user_roles` VALUES (4, 1);
INSERT INTO `user_roles` VALUES (2, 2);
INSERT INTO `user_roles` VALUES (3, 2);
INSERT INTO `user_roles` VALUES (5, 2);
INSERT INTO `user_roles` VALUES (6, 2);
INSERT INTO `user_roles` VALUES (7, 2);

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
  `social_statement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人宣言',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active',
  `user_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `user_number`(`user_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$X7aPRYS9WF0cGHV9lOJhQO3YfpJiA4SZ5uE5MUmjHjjQEa5LsPzWe', '2025-05-02 06:12:33', NULL, '系统管理员', 'Admin', 'admin@example.com', NULL, NULL, NULL, 'active', 'S25000016');
INSERT INTO `users` VALUES (2, '3', '$2a$12$vz8Ezo.a7qAXJZN9tX3hRuhtuvthtwLxOXlN5LoJkn9zB8czlTATK', '2025-05-02 15:11:10', '/uploads/f413b130-27be-49dc-846b-6c69e6cf6bf6.jpg', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25334721');
INSERT INTO `users` VALUES (3, '2', '$2a$12$mlRRlzJUN1dVQ5/47u5LjOZ4lNhVCnGFqFK7fnjBo/NokMzhS75om', '2025-05-02 18:30:02', '/uploads/202c5cc8-aa95-4a25-87dd-6ab5f3dfbca2.jpg', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25109895');
INSERT INTO `users` VALUES (4, '1', '$2a$12$tur3WnO9eT21X4v1qVeyM.zPv.iU5.Ws681IIEa3KfY6j6nUHBkfm', '2025-05-06 20:02:34', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25501326');
INSERT INTO `users` VALUES (5, '4', '$2a$12$oNXmNAKg7fGyqD2wZlxM5uWw70BrFdlA8n.Dl0wdP9IEpvMVcNikm', '2025-05-06 20:34:05', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25212952');
INSERT INTO `users` VALUES (6, 'yutian', '$2a$12$J65SQgLTIB.iAonGSYpWX.kqvQ6p7LD9vTRBAw.DpqKMKO8dGu7YS', '2025-05-13 10:26:33', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25514141');
INSERT INTO `users` VALUES (7, '22', '$2a$12$SjsYQyRdJlEQO3JFX.e1b.BvSqKFwm.jRmkwFh0wh9MBwI6U4uTSa', '2025-05-13 11:53:23', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25107364');

SET FOREIGN_KEY_CHECKS = 1;
