-- 创建跑步偏好表
CREATE TABLE running_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    frequency VARCHAR(50),
    preferred_distance VARCHAR(50),
    pace VARCHAR(50),
    environment VARCHAR(50),
    motto VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_running_preferences_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT UK_running_preferences_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引
CREATE INDEX idx_running_preferences_user_id ON running_preferences(user_id); 