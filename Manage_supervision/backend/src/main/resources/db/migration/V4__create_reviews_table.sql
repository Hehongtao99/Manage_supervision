-- 创建评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    companion_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content TEXT,
    anonymous BOOLEAN DEFAULT FALSE NOT NULL,
    replied BOOLEAN DEFAULT FALSE NOT NULL,
    reply TEXT,
    reply_time DATETIME,
    review_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝',
    review_time DATETIME DEFAULT NULL COMMENT '审核时间',
    review_comment TEXT COMMENT '审核意见',
    reviewer_admin_id BIGINT DEFAULT NULL COMMENT '审核管理员ID',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    CONSTRAINT fk_reviews_orders FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_companion FOREIGN KEY (companion_id) REFERENCES users(id)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_reviews_order_id ON reviews(order_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_reviews_companion_id ON reviews(companion_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_replied ON reviews(replied);
CREATE INDEX idx_reviews_review_status ON reviews(review_status); 