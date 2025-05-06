-- 修改social_posts表，添加转发相关字段
ALTER TABLE social_posts
    ADD COLUMN forward_count INT NOT NULL DEFAULT 0 COMMENT '转发数量',
    ADD COLUMN is_forward TINYINT NOT NULL DEFAULT 0 COMMENT '是否是转发的帖子，0-原创，1-转发',
    ADD COLUMN original_post_id BIGINT NOT NULL DEFAULT 0 COMMENT '原始帖子ID，如果是原创则为0';

-- 创建转发关系表
CREATE TABLE IF NOT EXISTS social_post_forwards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '转发ID',
    user_id BIGINT NOT NULL COMMENT '转发者用户ID',
    original_post_id BIGINT NOT NULL COMMENT '原始帖子ID',
    new_post_id BIGINT NOT NULL COMMENT '新帖子ID',
    forward_comment VARCHAR(500) DEFAULT NULL COMMENT '转发评论内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_original_post_id (original_post_id),
    INDEX idx_new_post_id (new_post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈帖子转发表'; 