-- 考勤面板功能（2023-11-05）
-- 说明：
-- 1. 增加了考勤面板功能，允许学生直接通过人脸识别进行考勤，无需先登录
-- 2. 修改了相关API和前端组件，实现管理员登录和考勤面板的切换
-- 3. 添加了相似度计算和展示功能，让考勤过程更直观可见
-- 4. 增加了未登录状态下的人脸识别和用户匹配功能

-- 确保users表有face_data字段用于存储人脸数据
ALTER TABLE users ADD COLUMN IF NOT EXISTS face_data TEXT COMMENT '用户人脸数据（Base64编码）';
ALTER TABLE users ADD COLUMN IF NOT EXISTS face_registered_time DATETIME COMMENT '人脸注册时间';

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS attendance;

-- 确保考勤表结构完整
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '考勤标题',
    description TEXT COMMENT '考勤描述',
    location VARCHAR(255) COMMENT '考勤地点',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active, inactive',
    CONSTRAINT FK_attendance_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 确保考勤记录表结构完整
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_id BIGINT NOT NULL COMMENT '考勤ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    check_in_time DATETIME NOT NULL COMMENT '签到时间',
    status VARCHAR(50) COMMENT '状态',
    location VARCHAR(255) COMMENT '签到位置',
    notes TEXT COMMENT '备注',
    face_verified BOOLEAN DEFAULT FALSE COMMENT '是否通过人脸验证',
    similarity FLOAT COMMENT '人脸相似度',
    CONSTRAINT FK_record_attendance FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE,
    CONSTRAINT FK_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT UK_attendance_user UNIQUE (attendance_id, user_id) COMMENT '确保每个用户在一次考勤中只有一条记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 添加索引提高查询性能
CREATE INDEX idx_attendance_status ON attendance(status);
CREATE INDEX idx_attendance_time ON attendance(start_time, end_time);
CREATE INDEX idx_attendance_record_user ON attendance_records(user_id);
CREATE INDEX idx_attendance_record_check_time ON attendance_records(check_in_time);

-- 更新考勤记录表，添加相似度字段（如果没有）
ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS similarity FLOAT COMMENT '人脸相似度'; 