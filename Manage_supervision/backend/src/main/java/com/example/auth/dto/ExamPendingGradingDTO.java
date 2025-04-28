package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * 待批阅考试信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamPendingGradingDTO {
    private Long id;              // 考试 ID
    private String title;           // 考试标题
    private Instant startTime;      // 开始时间
    private Instant endTime;        // 结束时间
    private Long pendingStudentCount; // 待批阅学生数量
} 