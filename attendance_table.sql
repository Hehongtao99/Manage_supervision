-- 创建考勤表
DROP TABLE IF EXISTS `attendances`;
CREATE TABLE `attendances` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `check_in_time` datetime NOT NULL COMMENT '考勤时间',
  `face_recognized` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否成功识别人脸',
  `recognition_details` varchar(500) DEFAULT NULL COMMENT '识别详情',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id` (`student_id`) USING BTREE,
  INDEX `idx_class_id` (`class_id`) USING BTREE,
  INDEX `idx_check_in_time` (`check_in_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生考勤表' ROW_FORMAT = Dynamic; 