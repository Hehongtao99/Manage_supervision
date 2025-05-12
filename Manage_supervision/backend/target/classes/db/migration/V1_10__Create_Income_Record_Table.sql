-- 创建收入记录表
CREATE TABLE IF NOT EXISTS income_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额（正数表示收入，负数表示扣除）',
    type VARCHAR(20) NOT NULL COMMENT '类型：ORDER-订单收入, REFUND-退款扣除, APPEAL-申诉扣除',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_order_id (order_id),
    INDEX idx_create_time (create_time)
) COMMENT '收入记录表';

-- 添加外键约束
ALTER TABLE income_record
ADD CONSTRAINT fk_income_record_teacher
FOREIGN KEY (teacher_id) REFERENCES user(id);

ALTER TABLE income_record
ADD CONSTRAINT fk_income_record_order
FOREIGN KEY (order_id) REFERENCES `order`(id); 