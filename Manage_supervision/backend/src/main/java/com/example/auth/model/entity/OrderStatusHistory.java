package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_status_history")
public class OrderStatusHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    private String status;
    private Long operatorId;
    private String operatorType;  // PLAYER, COMPANION, ADMIN, SYSTEM
    private LocalDateTime createTime;
    private String remark;
} 