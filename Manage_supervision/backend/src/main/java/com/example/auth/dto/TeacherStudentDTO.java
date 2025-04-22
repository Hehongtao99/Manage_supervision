package com.example.auth.dto;

import lombok.Data;

@Data
public class TeacherStudentDTO {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String teacherUserNumber;
    private Long studentId;
    private String studentName;
    private String studentUserNumber;
    private String assignTime;
    private String status;
} 