package com.example.auth.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单状态历史记录数据传输对象
 */
@Data
public class OrderStatusHistoryDTO {
    
    private Long id;
    private Long orderId;
    private String status;
    private Long operatorId;
    private String operatorType;  // PLAYER, COMPANION, ADMIN, SYSTEM
    private LocalDateTime createTime;
    private String remark;
    
    // 扩展字段
    private String operatorName;
} 