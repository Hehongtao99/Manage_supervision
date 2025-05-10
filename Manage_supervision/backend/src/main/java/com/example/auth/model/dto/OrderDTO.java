package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单数据传输对象
 */
@Data
public class OrderDTO {
    private Long id;
    private Long studentId;
    private Long teacherId;
    private Long courseId;
    private String orderNumber;
    private BigDecimal price;
    private Integer hours;
    private BigDecimal totalAmount;
    private String status;
    private String message;
    private String rejectReason;
    private String refundReason;
    private String appealReason;
    private String teacherResponse;
    private String adminDecision;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime appealTime;

    // 关联信息
    private String studentName;
    private String teacherName;
    private String courseTitle;
    private String courseSubject;
} 