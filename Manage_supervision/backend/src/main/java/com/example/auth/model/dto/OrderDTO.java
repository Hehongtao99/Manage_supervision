package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long playerId;
    private String playerName;
    private Long companionId;
    private String companionName;
    private Long serviceId;
    private String serviceTitle;
    private String gameType;
    private Integer hours;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private LocalDateTime appointedTime;
    private String status;
    private String paymentStatus;
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
} 