package com.example.auth.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AttendanceDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private Long creatorId;
    private String creatorName;
    private String status;
    private int totalUsers;
    private int checkedInUsers;
    private List<AttendanceRecordDTO> records;
} 