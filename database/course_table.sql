-- 课程表
CREATE TABLE IF NOT EXISTS `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name` varchar(100) NOT NULL COMMENT '课程名称',
  `course_description` text COMMENT '课程简介',
  `teacher_id` bigint NOT NULL COMMENT '授课教师ID',
  `teacher_name` varchar(50) NOT NULL COMMENT '授课教师姓名',
  `course_duration` int NOT NULL COMMENT '课程时长(小时)',
  `course_category` varchar(50) NOT NULL COMMENT '课程类别',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态(active:启用 inactive:禁用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NOT NULL COMMENT '创建人ID',
  `updated_by` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_course_category` (`course_category`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 插入课程类别基础数据（可选）
INSERT INTO `courses` (`course_name`, `course_description`, `teacher_id`, `teacher_name`, `course_duration`, `course_category`, `status`, `created_by`, `updated_by`) VALUES
('Java基础编程', 'Java语言基础知识和面向对象编程', 1, '张老师', 40, '计算机科学', 'active', 1, 1),
('高等数学', '微积分、线性代数等数学基础', 2, '李老师', 60, '数学', 'active', 1, 1),
('大学英语', '英语听说读写综合训练', 3, '王老师', 32, '英语', 'active', 1, 1); 