package com.example.auth.model.dto;

import lombok.Data;

/**
 * 教师课程收入分布数据传输对象
 */
@Data
public class TeacherCourseIncomeDTO {
    // 课程名称
    private String courseName;
    
    // 收入金额
    private Double amount;
} 