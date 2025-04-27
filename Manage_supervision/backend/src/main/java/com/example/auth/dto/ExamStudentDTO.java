package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamStudentDTO {
    
    private Long id;
    
    private Long examId;
    
    private Long studentId;
    
    private String studentName;
    
    private String studentUsername;
    
    private String studentUserNumber;
    
    private String status;
    
    private Instant startTime;
    
    private Instant submitTime;
    
    private BigDecimal score;
    
    private Instant createTime;
    
    // 是否通过考试
    private Boolean isPassed;
} 