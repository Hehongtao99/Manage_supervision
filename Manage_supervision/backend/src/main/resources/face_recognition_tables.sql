-- 用户人脸特征表
CREATE TABLE IF NOT EXISTS user_face_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    face_feature LONGBLOB NOT NULL, -- 存储人脸特征向量数据
    face_image LONGBLOB, -- 可选存储人脸图像数据
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_user_face_user_id ON user_face_features(user_id); 