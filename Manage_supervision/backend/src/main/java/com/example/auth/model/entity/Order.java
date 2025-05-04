package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNumber;
    private Long playerId;
    private Long companionId;
    private Long serviceId;
    private String gameType;
    private Integer hours;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private LocalDateTime appointedTime;
    private String status;  // PENDING, ACCEPTED, COMPLETED, CANCELLED, REFUNDED
    private String paymentStatus;  // UNPAID, PAID, REFUNDED
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
} 