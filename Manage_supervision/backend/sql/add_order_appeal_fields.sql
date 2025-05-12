-- 在orders表中添加申诉相关字段
ALTER TABLE orders 
ADD COLUMN `refund_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款理由' AFTER `reject_reason`,
ADD COLUMN `appeal_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申诉理由' AFTER `refund_reason`,
ADD COLUMN `teacher_response` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教师回复' AFTER `appeal_reason`,
ADD COLUMN `admin_decision` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员决定' AFTER `teacher_response`,
ADD COLUMN `appeal_time` datetime NULL DEFAULT NULL COMMENT '申诉时间' AFTER `admin_decision`;

-- 更新status字段的注释
ALTER TABLE orders 
MODIFY COLUMN `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING(待确认), ACCEPTED(已接受), REJECTED(已拒绝), CANCELED(已取消), COMPLETED(已完成), REFUND_PENDING(退款申请中), REFUND_REJECTED(退款申请被拒), APPEALING(申诉中), APPEAL_APPROVED(申诉通过), APPEAL_REJECTED(申诉驳回)'; 