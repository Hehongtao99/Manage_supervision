package com.example.auth.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教师申请课程DTO
 */
@Data
public class CourseApplicationDTO {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String subject;
    private BigDecimal hourlyPrice;
    private String description;
    private List<String> imagePaths;
    private Integer workTimeStart;
    private Integer workTimeEnd;
    private String status;
    private String rejectionReason;
    private String createTime;
    private String updateTime;
    // 简历相关字段
    private String teacherBio;
    private String teacherGraduationSchool;
    private String teacherTeachingSubjects;
} 