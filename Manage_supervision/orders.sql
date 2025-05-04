-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL COMMENT '订单编号，系统生成',
    player_id BIGINT NOT NULL COMMENT '玩家ID',
    companion_id BIGINT NOT NULL COMMENT '陪玩ID',
    service_id BIGINT NOT NULL COMMENT '陪玩服务ID',
    game_type VARCHAR(50) COMMENT '游戏类型',
    hours INT NOT NULL COMMENT '购买小时数',
    price DECIMAL(10, 2) NOT NULL COMMENT '单价(元/小时)',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    appointed_time DATETIME COMMENT '预约时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待接单，ACCEPTED-已接单，COMPLETED-已完成，CANCELLED-已取消，REFUNDED-已退款',
    payment_status VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID-未支付，PAID-已支付，REFUNDED-已退款',
    payment_time DATETIME COMMENT '支付时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注信息',
    
    CONSTRAINT FK_orders_player FOREIGN KEY (player_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_companion FOREIGN KEY (companion_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_service FOREIGN KEY (service_id) REFERENCES companion_services (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建订单状态历史表
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    status VARCHAR(20) NOT NULL COMMENT '订单状态',
    operator_id BIGINT COMMENT '操作人ID',
    operator_type VARCHAR(20) COMMENT '操作人类型：PLAYER-玩家, COMPANION-陪玩, ADMIN-管理员, SYSTEM-系统',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    remark VARCHAR(500) COMMENT '备注信息',
    
    CONSTRAINT FK_status_history_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT FK_status_history_operator FOREIGN KEY (operator_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 创建索引以提高查询性能
CREATE INDEX idx_orders_player_id ON orders(player_id);
CREATE INDEX idx_orders_companion_id ON orders(companion_id);
CREATE INDEX idx_orders_service_id ON orders(service_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_create_time ON orders(create_time);
CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);
 