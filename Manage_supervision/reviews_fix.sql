-- 检查reviews表是否存在review_status列，如果不存在则添加
DELIMITER //
CREATE PROCEDURE AddReviewStatusColumn()
BEGIN
    DECLARE CONTINUE HANDLER FOR 1060 BEGIN END;  -- 忽略列已存在的错误
    
    -- 添加审核状态列
    ALTER TABLE reviews 
    ADD COLUMN review_status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝';
    
    -- 添加审核时间列
    ALTER TABLE reviews 
    ADD COLUMN review_time DATETIME COMMENT '审核时间';
    
    -- 添加审核意见列
    ALTER TABLE reviews 
    ADD COLUMN review_comment TEXT COMMENT '审核意见';
    
    -- 添加审核管理员ID列
    ALTER TABLE reviews 
    ADD COLUMN reviewer_admin_id BIGINT COMMENT '审核管理员ID';
    
    -- 设置所有现有评论的状态为pending
    UPDATE reviews SET review_status = 'pending' WHERE review_status IS NULL;
END //
DELIMITER ;

-- 执行存储过程
CALL AddReviewStatusColumn();

-- 删除存储过程
DROP PROCEDURE AddReviewStatusColumn;

-- 创建索引，提高查询性能
CREATE INDEX IF NOT EXISTS idx_reviews_order_id ON reviews(order_id);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_companion_id ON reviews(companion_id);
CREATE INDEX IF NOT EXISTS idx_reviews_review_status ON reviews(review_status);

-- 兼容不支持IF NOT EXISTS的MySQL版本
DELIMITER //
CREATE PROCEDURE CreateIndexIfNotExists()
BEGIN
    DECLARE CONTINUE HANDLER FOR 1061 BEGIN END;  -- 忽略索引已存在的错误
    
    CREATE INDEX idx_reviews_order_id ON reviews(order_id);
    CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
    CREATE INDEX idx_reviews_companion_id ON reviews(companion_id);
    CREATE INDEX idx_reviews_review_status ON reviews(review_status);
END //
DELIMITER ;

-- 执行存储过程
CALL CreateIndexIfNotExists();

-- 删除存储过程
DROP PROCEDURE CreateIndexIfNotExists; 