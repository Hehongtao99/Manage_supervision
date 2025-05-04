-- 创建评价表
CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  reviewer_id BIGINT NOT NULL COMMENT '评价人ID（玩家ID）',
  companion_id BIGINT NOT NULL COMMENT '被评价人ID（陪玩ID）',
  rating INT NOT NULL COMMENT '评分（1-5星）',
  content TEXT COMMENT '评价内容',
  anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名',
  replied BOOLEAN DEFAULT FALSE COMMENT '是否已回复',
  reply TEXT COMMENT '回复内容',
  reply_time DATETIME COMMENT '回复时间',
  review_status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝',
  review_time DATETIME COMMENT '审核时间',
  review_comment TEXT COMMENT '审核意见',
  reviewer_admin_id BIGINT COMMENT '审核管理员ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  CONSTRAINT FK_reviews_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
  CONSTRAINT FK_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT FK_reviews_companion FOREIGN KEY (companion_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT FK_reviews_admin FOREIGN KEY (reviewer_admin_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 如果表已存在，则添加缺少的列
ALTER TABLE reviews 
ADD COLUMN IF NOT EXISTS review_status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝' AFTER reply_time,
ADD COLUMN IF NOT EXISTS review_time DATETIME COMMENT '审核时间' AFTER review_status,
ADD COLUMN IF NOT EXISTS review_comment TEXT COMMENT '审核意见' AFTER review_time,
ADD COLUMN IF NOT EXISTS reviewer_admin_id BIGINT COMMENT '审核管理员ID' AFTER review_comment;

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_reviews_order_id ON reviews(order_id);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_companion_id ON reviews(companion_id);
CREATE INDEX IF NOT EXISTS idx_reviews_review_status ON reviews(review_status); 