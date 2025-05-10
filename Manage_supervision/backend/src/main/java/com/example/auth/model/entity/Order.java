package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Order {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("course_id")
    private Long courseId;

    @TableField("order_number")
    private String orderNumber;

    @TableField("price")
    private BigDecimal price;

    @TableField("hours")
    private Integer hours;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private String status; // PENDING, ACCEPTED, REJECTED, CANCELED, COMPLETED, REFUND_PENDING, REFUND_REJECTED, APPEALING, APPEAL_APPROVED, APPEAL_REJECTED

    @TableField("message")
    private String message;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("refund_reason")
    private String refundReason;
    
    @TableField("appeal_reason")
    private String appealReason;
    
    @TableField("teacher_response")
    private String teacherResponse;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableField("appeal_time")
    private LocalDateTime appealTime;
    
    @TableField("admin_decision")
    private String adminDecision;

    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String courseTitle;

    @TableField(exist = false)
    private String courseSubject;
} 