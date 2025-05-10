package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师查看学生及其课程信息的DTO
 */
@Data
public class TeacherStudentCourseDTO {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String email;
    private String phone;
    private String userNumber;
    private String avatar;
    private String status;
    
    // 学生订阅的该教师的课程信息
    private List<StudentCourseInfo> courses;
    
    @Data
    public static class StudentCourseInfo {
        private Long orderId;
        private Long courseId;
        private String courseTitle;
        private String courseSubject;
        private BigDecimal price;
        private Integer hours;
        private BigDecimal totalAmount;
        private String status;
        private LocalDateTime createTime;
    }
}