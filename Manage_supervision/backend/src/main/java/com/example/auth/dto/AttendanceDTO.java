package com.example.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private LocalDateTime checkInTime;
    private boolean faceRecognized;
    private String recognitionDetails;
    private LocalDateTime createTime;
} 