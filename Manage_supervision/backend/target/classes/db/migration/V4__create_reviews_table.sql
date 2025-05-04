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