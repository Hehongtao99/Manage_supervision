-- 数据库结构迁移SQL
-- 以第二个SQL文件为标准，补充第一个SQL文件缺少的结构

-- 1. 为 running_records 表添加新字段和索引
ALTER TABLE `running_records` 
ADD COLUMN `record_datetime` datetime NULL DEFAULT NULL COMMENT '记录时间（精确到秒）';

-- 为 running_records 表添加新索引
ALTER TABLE `running_records` 
ADD INDEX `idx_running_records_datetime`(`record_datetime` ASC) USING BTREE;

ALTER TABLE `running_records` 
ADD INDEX `idx_running_records_user_datetime`(`user_id` ASC, `record_datetime` DESC) USING BTREE;

-- 修改 running_records 表的 duration 字段，允许为NULL
ALTER TABLE `running_records` 
MODIFY COLUMN `duration` int NULL DEFAULT NULL COMMENT '跑步时长，单位：秒';

-- 2. 创建 social_takedowns 表（社交内容下架记录表）
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社交内容下架记录表' ROW_FORMAT = Dynamic;

-- 3. 创建 teacher_student_relations 表（师生关系表）
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

-- 4. 更新 roles 表数据，修改角色描述和权限
UPDATE `roles` SET 
  `description` = '管理员角色，拥有跑步管理系统的所有权限',
  `permissions` = 'USER_VIEW,USER_EDIT,USER_DELETE,ROLE_VIEW,ROLE_EDIT,ROLE_DELETE,LOG_VIEW,SYSTEM_SETTINGS,RUNNING_MANAGEMENT,SOCIAL_MANAGEMENT'
WHERE `name` = 'ADMIN';

UPDATE `roles` SET 
  `description` = '跑步爱好者角色，可以记录跑步数据、参与社交活动',
  `permissions` = 'USER_VIEW,RUNNING_RECORD,SOCIAL_INTERACT'
WHERE `name` = 'USER';

-- 删除 SUPERVISOR 角色（如果存在）
DELETE FROM `user_roles` WHERE `role_id` = (SELECT `id` FROM `roles` WHERE `name` = 'SUPERVISOR');
DELETE FROM `roles` WHERE `name` = 'SUPERVISOR';

-- 5. 更新表的 ROW_FORMAT 为 Dynamic（如果需要）
ALTER TABLE `chat_messages` ROW_FORMAT = Dynamic;
ALTER TABLE `conversations` ROW_FORMAT = Dynamic;
ALTER TABLE `friend_requests` ROW_FORMAT = Dynamic;
ALTER TABLE `roles` ROW_FORMAT = Dynamic;
ALTER TABLE `running_preferences` ROW_FORMAT = Dynamic;
ALTER TABLE `running_records` ROW_FORMAT = Dynamic;
ALTER TABLE `running_steps` ROW_FORMAT = Dynamic;
ALTER TABLE `social_post_comments` ROW_FORMAT = Dynamic;
ALTER TABLE `social_post_forwards` ROW_FORMAT = Dynamic;
ALTER TABLE `social_post_images` ROW_FORMAT = Dynamic;
ALTER TABLE `social_post_likes` ROW_FORMAT = Dynamic;
ALTER TABLE `social_post_running_records` ROW_FORMAT = Dynamic;
ALTER TABLE `social_posts` ROW_FORMAT = Dynamic;
ALTER TABLE `user_friends` ROW_FORMAT = Dynamic;
ALTER TABLE `user_friendships` ROW_FORMAT = Dynamic;
ALTER TABLE `user_roles` ROW_FORMAT = Dynamic;
ALTER TABLE `users` ROW_FORMAT = Dynamic; 