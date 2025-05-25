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

 Date: 25/05/2025 09:43:55
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of conversations
-- ----------------------------

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码，唯一标识',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限描述',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父权限ID，0表示顶级权限',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限类型：menu-菜单，button-按钮，api-接口',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径（前端路由）',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径（前端组件）',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_visible` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_permission_code`(`code` ASC) USING BTREE,
  INDEX `idx_permission_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 59 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of permissions
-- ----------------------------
INSERT INTO `permissions` VALUES (1, 'system', '系统管理', '系统管理模块', 0, 'menu', '/system', 'Layout', 'setting', 1, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (2, 'user', '用户管理', '用户管理模块', 1, 'menu', '/system/user', 'system/user/index', 'user', 1, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (3, 'user:view', '查看用户', '查看用户列表和详情', 2, 'button', '', '', '', 1, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (4, 'user:add', '添加用户', '添加新用户', 2, 'button', '', '', '', 2, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (5, 'user:edit', '编辑用户', '编辑用户信息', 2, 'button', '', '', '', 3, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (6, 'user:delete', '删除用户', '删除用户', 2, 'button', '', '', '', 4, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (7, 'user:reset_password', '重置密码', '重置用户密码', 2, 'button', '', '', '', 5, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (8, 'role', '角色管理', '角色管理模块', 1, 'menu', '/system/role', 'system/role/index', 'peoples', 2, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (9, 'role:view', '查看角色', '查看角色列表和详情', 8, 'button', '', '', '', 1, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (10, 'role:add', '添加角色', '添加新角色', 8, 'button', '', '', '', 2, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (11, 'role:edit', '编辑角色', '编辑角色信息', 8, 'button', '', '', '', 3, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (12, 'role:delete', '删除角色', '删除角色', 8, 'button', '', '', '', 4, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (13, 'permission', '权限管理', '权限管理模块', 1, 'menu', '/system/permission', 'system/permission/index', 'lock', 3, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (14, 'permission:view', '查看权限', '查看权限列表和详情', 13, 'button', '', '', '', 1, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (15, 'permission:add', '添加权限', '添加新权限', 13, 'button', '', '', '', 2, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (16, 'permission:edit', '编辑权限', '编辑权限信息', 13, 'button', '', '', '', 3, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (17, 'permission:delete', '删除权限', '删除权限', 13, 'button', '', '', '', 4, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (18, 'permission:assign', '分配权限', '为角色分配权限', 13, 'button', '', '', '', 5, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (19, 'student', '学生管理', '学生管理模块', 0, 'menu', '/student', 'Layout', 'peoples', 2, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (20, 'student:view', '查看学生', '查看学生列表和详情', 19, 'button', '', '', '', 1, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (21, 'student:add', '添加学生', '添加新学生', 19, 'button', '', '', '', 2, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (22, 'student:edit', '编辑学生', '编辑学生信息', 19, 'button', '', '', '', 3, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (23, 'student:delete', '删除学生', '删除学生', 19, 'button', '', '', '', 4, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (24, 'student:assign', '分配导师', '为学生分配导师', 19, 'button', '', '', '', 5, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (25, 'chat', '对话管理', '对话管理模块', 0, 'menu', '/chat', 'Layout', 'message', 3, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (26, 'chat:send', '发送消息', '发送聊天消息', 25, 'button', '', '', '', 1, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (27, 'chat:view', '查看消息', '查看聊天消息', 25, 'button', '', '', '', 2, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (28, 'chat:delete', '删除消息', '删除聊天消息', 25, 'button', '', '', '', 3, 1, '2025-05-24 11:17:06', '2025-05-24 11:17:06');
INSERT INTO `permissions` VALUES (50, 'course', '课程管理', '课程管理模块', 0, 'menu', '/course', 'Layout', 'education', 4, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (51, 'course:view', '查看课程', '查看课程列表和详情', 50, 'button', '', '', '', 1, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (52, 'course:add', '添加课程', '添加新课程', 50, 'button', '', '', '', 2, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (53, 'course:edit', '编辑课程', '编辑课程信息', 50, 'button', '', '', '', 3, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (54, 'course:delete', '删除课程', '删除课程', 50, 'button', '', '', '', 4, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (55, 'category', '课程类别管理', '课程类别管理模块', 50, 'menu', '/admin/course-categories', 'course/CategoryManagement', 'list', 5, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (56, 'category:view', '查看类别', '查看课程类别列表和详情', 55, 'button', '', '', '', 1, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (57, 'category:add', '添加类别', '添加新课程类别', 55, 'button', '', '', '', 2, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (58, 'category:edit', '编辑类别', '编辑课程类别信息', 55, 'button', '', '', '', 3, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `permissions` VALUES (59, 'category:delete', '删除类别', '删除课程类别', 55, 'button', '', '', '', 4, 1, '2025-05-25 10:00:00', '2025-05-25 10:00:00');

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_permissions_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_role_permissions_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 716 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (35, 3, 25, '2025-05-24 11:17:06');
INSERT INTO `role_permissions` VALUES (36, 3, 28, '2025-05-24 11:17:06');
INSERT INTO `role_permissions` VALUES (37, 3, 26, '2025-05-24 11:17:06');
INSERT INTO `role_permissions` VALUES (38, 3, 27, '2025-05-24 11:17:06');
INSERT INTO `role_permissions` VALUES (39, 3, 19, '2025-05-24 11:17:06');
INSERT INTO `role_permissions` VALUES (40, 3, 22, '2025-05-24 11:17:06');
INSERT INTO `role_permissions` VALUES (41, 3, 20, '2025-05-24 11:17:06');
INSERT INTO `role_permissions` VALUES (194, 1, 1, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (195, 1, 2, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (196, 1, 3, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (197, 1, 4, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (198, 1, 5, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (199, 1, 6, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (200, 1, 8, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (201, 1, 9, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (202, 1, 10, '2025-05-24 20:06:15');
INSERT INTO `role_permissions` VALUES (203, 1, 11, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (204, 1, 12, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (205, 1, 13, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (206, 1, 14, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (207, 1, 15, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (208, 1, 16, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (209, 1, 17, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (210, 1, 18, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (211, 1, 19, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (212, 1, 20, '2025-05-24 20:06:16');
INSERT INTO `role_permissions` VALUES (213, 1, 21, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (214, 1, 22, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (215, 1, 23, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (216, 1, 24, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (217, 1, 25, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (218, 1, 26, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (219, 1, 27, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (220, 1, 28, '2025-05-24 20:06:17');
INSERT INTO `role_permissions` VALUES (551, 1, 50, '2025-05-24 15:17:37');
INSERT INTO `role_permissions` VALUES (552, 1, 51, '2025-05-24 15:17:37');
INSERT INTO `role_permissions` VALUES (553, 1, 52, '2025-05-24 15:17:37');
INSERT INTO `role_permissions` VALUES (554, 1, 53, '2025-05-24 15:17:37');
INSERT INTO `role_permissions` VALUES (555, 1, 54, '2025-05-24 15:17:37');
INSERT INTO `role_permissions` VALUES (565, 3, 50, '2025-05-24 15:17:37');
INSERT INTO `role_permissions` VALUES (701, 2, 1, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (702, 2, 2, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (703, 2, 3, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (704, 2, 5, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (705, 2, 8, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (706, 2, 9, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (707, 2, 13, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (708, 2, 14, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (709, 2, 19, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (710, 2, 20, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (711, 2, 24, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (712, 2, 25, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (713, 2, 26, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (714, 2, 27, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (715, 2, 28, '2025-05-24 23:31:23');
INSERT INTO `role_permissions` VALUES (556, 1, 50, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (557, 1, 51, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (558, 1, 52, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (559, 1, 53, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (560, 1, 54, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (561, 3, 50, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (562, 3, 51, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (563, 2, 50, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (564, 2, 51, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (570, 1, 55, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (571, 1, 56, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (572, 1, 57, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (573, 1, 58, '2025-05-25 10:00:00');
INSERT INTO `role_permissions` VALUES (574, 1, 59, '2025-05-25 10:00:00');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'ADMIN', '管理员角色，拥有最高权限', '2025-05-24 11:17:06');
INSERT INTO `roles` VALUES (2, 'USER', '学生角色，基本用户权限', '2025-05-24 11:17:06');
INSERT INTO `roles` VALUES (3, 'SUPERVISOR', '督导员角色，可以管理学生', '2025-05-24 11:17:06');

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
  UNIQUE INDEX `uk_teacher_student`(`teacher_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_teacher_student_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_teacher_student_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
  INDEX `idx_user_roles_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_roles_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 1);
INSERT INTO `user_roles` VALUES (2, 1);
INSERT INTO `user_roles` VALUES (3, 3);
INSERT INTO `user_roles` VALUES (4, 2);

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$X7aPRYS9WF0cGHV9lOJhQO3YfpJiA4SZ5uE5MUmjHjjQEa5LsPzWe', '2025-05-24 11:17:06', NULL, '系统管理员', 'Admin', 'admin@example.com', NULL, NULL, 'active', 'S25112300');
INSERT INTO `users` VALUES (2, '1', '$2a$12$B9CjP9hQRXcGC/tBrjgnWeELSh1p1yTBLWzwjANojlpSDTSDOEXD.', '2025-05-24 19:21:36', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25672688');
INSERT INTO `users` VALUES (3, '2', '$2a$12$5.lvqoZGCs62ylruYJ7OWOPvA7OieTFLRsrN3/idXvyWy/j8KsMK.', '2025-05-24 19:58:37', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'T2556670');
INSERT INTO `users` VALUES (4, '3', '$2a$12$7SM3n67QX0aKm68GUzjGo.el28dvsZ00UV5BQ0lxtStnhx8NK4YBS', '2025-05-24 21:24:07', NULL, NULL, NULL, NULL, NULL, NULL, 'active', 'S25570717');

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程简介',
  `teacher_id` bigint NOT NULL COMMENT '授课教师ID',
  `duration` int NULL DEFAULT 0 COMMENT '课程时长(分钟)',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程类别',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程封面图片',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '课程状态: active-活跃, inactive-未激活, deleted-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_courses_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_courses_category`(`category` ASC) USING BTREE,
  INDEX `idx_courses_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of courses
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for course_categories
-- ----------------------------
DROP TABLE IF EXISTS `course_categories`;
CREATE TABLE `course_categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类别名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '类别描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of course_categories
-- ----------------------------
INSERT INTO `course_categories` VALUES (1, '计算机科学', '计算机科学与技术相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (2, '数学', '数学相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (3, '物理', '物理相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (4, '化学', '化学相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (5, '生物', '生物相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (6, '经济学', '经济学相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (7, '历史', '历史相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (8, '文学', '文学相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
INSERT INTO `course_categories` VALUES (9, '艺术', '艺术相关课程', '2025-05-25 10:00:00', '2025-05-25 10:00:00');
