package com.example.auth.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生课程信息数据传输对象
 */
@Data
public class StudentCourseDTO {
    // 课程ID
    private Long courseId;
    
    // 课程标题
    private String courseTitle;
    
    // 课程科目
    private String courseSubject;
    
    // 订单ID
    private Long orderId;
    
    // 订单编号
    private String orderNumber;
    
    // 订单状态
    private String status;
    
    // 课程单价
    private BigDecimal price;
    
    // 购买课时
    private Integer hours;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 订单创建时间
    private LocalDateTime createTime;
} 