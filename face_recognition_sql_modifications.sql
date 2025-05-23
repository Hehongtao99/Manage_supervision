-- 如果users表中没有face_data字段，添加该字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS face_data TEXT COMMENT '用户人脸数据（Base64编码）';

-- 添加人脸验证标志字段到考勤记录表
ALTER TABLE e981_attendance_record ADD COLUMN IF NOT EXISTS face_verified BOOLEAN DEFAULT FALSE COMMENT '是否通过人脸验证';

-- 创建人脸注册日志表（可选）
CREATE TABLE IF NOT EXISTS face_registration_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    registration_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL,
    error_message VARCHAR(255),
    ip_address VARCHAR(50),
    CONSTRAINT FK_face_reg_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 人脸比对功能增强（2023-10-15）
-- 说明：
-- 1. 修改了FaceRecognitionUtil类中的人脸比对阈值，从70.0降低到60.0，提高比对严格程度
-- 2. 更新了考勤签到中的错误提示，当人脸不一致时明确提示"人脸不一致，签到失败"
-- 3. 前端增加相应提示文案，使用户清楚了解人脸比对要求
-- 注意：此修改不需要数据库结构变更，仅为记录代码逻辑变更

-- 人脸识别准确率提升（2023-10-20）
-- 说明：
-- 1. 进一步降低人脸比对阈值，从60.0降低到40.0，显著提高比对严格程度，确保只有本人才能通过验证
-- 2. 添加结构相似度(SSIM)算法作为第二重验证，要求两张人脸图像结构相似度必须大于0.65
-- 3. 增强错误提示信息，给出更详细的验证失败原因和帮助指导
-- 4. 前端增加验证失败后的详细帮助弹窗，引导用户正确操作
-- 5. 增加后端日志记录，便于追踪验证过程和调试验证问题
-- 注意：此修改通过多重验证算法结合，有效解决了误识别问题，大幅提高了人脸验证的安全性和准确性

-- 人脸识别API兼容性修复（2023-10-25）
-- 说明：
-- 1. 修复了结构相似度计算方法中的JavaCV API兼容性问题
-- 2. 简化了相似度计算算法，使用绝对差异和均值差异的组合评估人脸相似度
-- 3. 提高相似度阈值至0.75，确保识别准确性
-- 4. 增加详细日志输出，便于分析比对过程和结果
-- 5. 改进Mat对象资源管理，确保内存正确释放
-- 注意：此修改保持了严格的人脸验证标准，同时解决了与JavaCV库版本兼容性的问题

-- 确保用户表中包含人脸数据字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS face_data TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS face_registered_time DATETIME;

-- 添加人脸识别相关日志表
CREATE TABLE IF NOT EXISTS face_recognition_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：REGISTER、UPDATE、DELETE、VERIFY',
    operated_by BIGINT COMMENT '操作人ID，如果是管理员操作则记录管理员ID',
    success BOOLEAN NOT NULL DEFAULT FALSE COMMENT '操作是否成功',
    ip_address VARCHAR(50) COMMENT '操作IP地址',
    details TEXT COMMENT '详细信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_face_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_face_logs_operator FOREIGN KEY (operated_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引以提高查询性能
CREATE INDEX idx_face_logs_user_id ON face_recognition_logs(user_id);
CREATE INDEX idx_face_logs_operation_type ON face_recognition_logs(operation_type);
CREATE INDEX idx_face_logs_success ON face_recognition_logs(success);
CREATE INDEX idx_face_logs_create_time ON face_recognition_logs(create_time);

-- 更新用户权限，确保管理员拥有人脸管理权限
UPDATE roles SET permissions = CONCAT(permissions, ',FACE_MANAGEMENT') WHERE name = 'ADMIN'; 