-- 创建跑步数据记录表
CREATE TABLE running_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    distance DECIMAL(10, 2) NOT NULL COMMENT '跑步距离，单位：公里',
    duration INT NOT NULL COMMENT '跑步时长，单位：分钟',
    pace VARCHAR(20) NOT NULL COMMENT '配速，格式：分钟:秒/公里',
    record_date DATE NOT NULL COMMENT '记录日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_running_records_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引
CREATE INDEX idx_running_records_user_id ON running_records(user_id);
CREATE INDEX idx_running_records_record_date ON running_records(record_date); 