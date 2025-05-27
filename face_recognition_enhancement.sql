-- 人脸识别功能增强SQL更新
-- 创建时间：2024年
-- 说明：本文件包含了人脸识别功能增强所需的数据库结构更新

-- 1. 增加人脸识别日志表索引，提高查询性能
ALTER TABLE face_recognition_logs ADD INDEX idx_face_logs_create_time_desc (create_time DESC);
ALTER TABLE face_recognition_logs ADD INDEX idx_face_logs_user_success (user_id, success);

-- 2. 增加考勤记录表的索引，提高人脸识别查询性能
ALTER TABLE attendance_records ADD INDEX idx_attendance_face_verified (face_verified, check_in_time);
ALTER TABLE attendance_records ADD INDEX idx_attendance_user_face (user_id, face_verified);

-- 3. 增加人脸识别配置表（如果需要存储识别参数）
CREATE TABLE IF NOT EXISTS face_recognition_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人脸识别配置表';

-- 4. 插入默认配置（包含多次验证相关参数）
INSERT INTO face_recognition_config (config_key, config_value, description) VALUES
('lbph_threshold', '35.0', 'LBPH算法置信度阈值，值越低越严格'),
('ssim_threshold', '0.80', '结构相似度阈值，值越高越严格'),
('detection_scale_factor', '1.1', '多尺度检测的缩放因子'),
('detection_min_neighbors', '3', '检测到的最小邻居数'),
('face_min_size', '80', '人脸最小尺寸（像素）'),
('face_max_size', '500', '人脸最大尺寸（像素）'),
('detection_interval', '200', '人脸检测间隔（毫秒）'),
('continuous_detections_required', '5', '连续检测次数要求'),
('multi_verification_times', '5', '多次验证的次数（建议3-5次）'),
('single_verification_threshold', '0.75', '单次验证通过阈值'),
('average_similarity_threshold', '0.70', '平均相似度通过阈值'),
('min_success_rate', '0.60', '最小成功率要求（60%以上）')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- 5. 增加人脸识别性能统计表
CREATE TABLE IF NOT EXISTS face_recognition_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL COMMENT '统计日期',
    total_detections INT DEFAULT 0 COMMENT '总检测次数',
    successful_detections INT DEFAULT 0 COMMENT '成功检测次数',
    total_recognitions INT DEFAULT 0 COMMENT '总识别次数',
    successful_recognitions INT DEFAULT 0 COMMENT '成功识别次数',
    avg_detection_time DECIMAL(10,2) DEFAULT 0 COMMENT '平均检测时间（毫秒）',
    avg_recognition_time DECIMAL(10,2) DEFAULT 0 COMMENT '平均识别时间（毫秒）',
    multi_verification_count INT DEFAULT 0 COMMENT '多次验证次数',
    multi_verification_success INT DEFAULT 0 COMMENT '多次验证成功次数',
    avg_similarity DECIMAL(5,4) DEFAULT 0 COMMENT '平均相似度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人脸识别性能统计表';

-- 6. 增加多次验证详细记录表
CREATE TABLE IF NOT EXISTS face_multiple_verification_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    verification_session_id VARCHAR(64) NOT NULL COMMENT '验证会话ID',
    verification_round INT NOT NULL COMMENT '验证轮次（1-N）',
    similarity DECIMAL(8,6) NOT NULL COMMENT '本轮相似度',
    lbph_similarity DECIMAL(8,6) NOT NULL COMMENT 'LBPH相似度',
    structural_similarity DECIMAL(8,6) NOT NULL COMMENT '结构相似度',
    verification_passed BOOLEAN NOT NULL COMMENT '本轮是否通过',
    processing_time_ms INT DEFAULT 0 COMMENT '处理时间（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_session (user_id, verification_session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多次验证详细记录表';

-- 7. 为users表的face_data字段增加索引（仅在face_data不为空时）
ALTER TABLE users ADD INDEX idx_users_has_face_data ((CASE WHEN face_data IS NOT NULL AND face_data != '' THEN 1 ELSE NULL END));

-- 8. 增加人脸质量评分字段（可选）
ALTER TABLE face_recognition_logs ADD COLUMN IF NOT EXISTS face_quality_score DECIMAL(5,2) COMMENT '人脸质量评分（0-100）';
ALTER TABLE face_recognition_logs ADD COLUMN IF NOT EXISTS detection_confidence DECIMAL(5,2) COMMENT '检测置信度';
ALTER TABLE face_recognition_logs ADD COLUMN IF NOT EXISTS verification_session_id VARCHAR(64) COMMENT '关联的多次验证会话ID';

-- 9. 为考勤记录表增加多次验证相关字段
ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS verification_session_id VARCHAR(64) COMMENT '多次验证会话ID';
ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS average_similarity DECIMAL(8,6) COMMENT '平均相似度';
ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS verification_success_rate DECIMAL(5,4) COMMENT '验证成功率';
ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS total_verification_attempts INT DEFAULT 1 COMMENT '总验证次数';

-- 10. 增加索引优化查询性能
ALTER TABLE face_multiple_verification_logs ADD INDEX idx_verification_passed (verification_passed, create_time);
ALTER TABLE attendance_records ADD INDEX idx_verification_session (verification_session_id);

-- 输出完成信息
SELECT '人脸识别多次验证功能增强SQL执行完成' as message; 