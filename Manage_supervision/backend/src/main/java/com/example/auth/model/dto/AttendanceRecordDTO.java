package com.example.auth.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AttendanceRecordDTO {
    private Long id;
    private Long attendanceId;
    private Long userId;
    private String username;
    private String realName;
    private String userNumber;
    private LocalDateTime checkInTime;
    private String status;
    private String location;
    private String notes;
    private String attendanceTitle;
    private Boolean faceVerified;
    private String error;
    private String message;
    
    /**
     * 用于返回错误信息的构造函数
     */
    public AttendanceRecordDTO(String error, String message) {
        this.error = error;
        this.message = message;
    }
} 