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
    private String checkInTime; // 改为String类型，更灵活地处理日期
    private String status;
    private String location;
    private String notes;
    private Boolean faceVerified;
    private Float similarity;
    
    // 额外字段，用于前端展示
    private String username;
    private String realName;
    private String userNumber;
    private String attendanceTitle;
    
    // 用于返回错误信息
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