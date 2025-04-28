-- 创建通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sender_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建通知接收者表
CREATE TABLE IF NOT EXISTS notification_recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT'
);

-- 添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_notification_sender ON notifications (sender_id);
CREATE INDEX IF NOT EXISTS idx_notification_recipient_notification ON notification_recipients (notification_id);
CREATE INDEX IF NOT EXISTS idx_notification_recipient_user ON notification_recipients (recipient_id); 