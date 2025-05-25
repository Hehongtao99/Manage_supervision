-- 学院专业班级管理功能数据库修改语句
-- 执行时间：请在系统维护时间执行

-- 1. 创建学院表
CREATE TABLE IF NOT EXISTS `colleges` (
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

-- 2. 创建专业表
CREATE TABLE IF NOT EXISTS `majors` (
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

-- 3. 创建班级表
CREATE TABLE IF NOT EXISTS `classes` (
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

-- 4. 创建教师班级关系表
CREATE TABLE IF NOT EXISTS `teacher_class_relations` (
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

-- 5. 修改用户表，添加学院、专业、班级字段
ALTER TABLE `users` 
ADD COLUMN IF NOT EXISTS `college_id` bigint NULL DEFAULT NULL COMMENT '所属学院ID' AFTER `user_number`,
ADD COLUMN IF NOT EXISTS `major_id` bigint NULL DEFAULT NULL COMMENT '所属专业ID' AFTER `college_id`,
ADD COLUMN IF NOT EXISTS `class_id` bigint NULL DEFAULT NULL COMMENT '所属班级ID（学生专用）' AFTER `major_id`;

-- 6. 添加索引
ALTER TABLE `users` 
ADD INDEX IF NOT EXISTS `idx_college_id`(`college_id` ASC),
ADD INDEX IF NOT EXISTS `idx_major_id`(`major_id` ASC),
ADD INDEX IF NOT EXISTS `idx_class_id`(`class_id` ASC);

-- 7. 插入示例数据（可选）
-- 插入示例学院
INSERT IGNORE INTO `colleges` (`college_name`, `college_code`, `description`, `status`) VALUES
('计算机科学与技术学院', 'CS', '计算机科学与技术相关专业', 'active'),
('经济管理学院', 'EM', '经济管理相关专业', 'active'),
('外国语学院', 'FL', '外语相关专业', 'active');

-- 插入示例专业
INSERT IGNORE INTO `majors` (`major_name`, `major_code`, `college_id`, `description`, `status`) VALUES
('计算机科学与技术', 'CS01', 1, '计算机科学与技术专业', 'active'),
('软件工程', 'CS02', 1, '软件工程专业', 'active'),
('工商管理', 'EM01', 2, '工商管理专业', 'active'),
('英语', 'FL01', 3, '英语专业', 'active');

-- 插入示例班级
INSERT IGNORE INTO `classes` (`class_name`, `class_code`, `college_id`, `major_id`, `grade`, `description`, `status`) VALUES
('计科2021级1班', 'CS2101', 1, 1, '2021', '计算机科学与技术2021级1班', 'active'),
('计科2021级2班', 'CS2102', 1, 1, '2021', '计算机科学与技术2021级2班', 'active'),
('软工2021级1班', 'CS2201', 1, 2, '2021', '软件工程2021级1班', 'active'),
('工管2021级1班', 'EM2101', 2, 3, '2021', '工商管理2021级1班', 'active'),
('英语2021级1班', 'FL2101', 3, 4, '2021', '英语2021级1班', 'active');

-- 执行完成后请重启应用服务器以确保缓存更新 