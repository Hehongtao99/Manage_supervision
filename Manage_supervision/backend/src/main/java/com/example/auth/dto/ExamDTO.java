package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    
    private Long id;
    
    private String title;
    
    private String description;
    
    private String status;
    
    private Instant startTime;
    
    private Instant endTime;
    
    private Integer duration;
    
    private BigDecimal passingScore;
    
    private BigDecimal totalScore;
    
    private Long creatorId;
    
    private String creatorName;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    // 考试题目列表
    private List<ExamQuestionDTO> examQuestions;
    
    // 考试学生列表
    private List<ExamStudentDTO> examStudents;
    
    // 统计信息 (教师视角)
    private Long totalStudents;      // 总参与人数
    private Long submittedCount;     // 已提交待批阅数量 (SUBMITTED)
    private Long pendingPublishCount; // 待发布成绩数量 (PENDING_PUBLISH)
    private Long publishedCount;     // 已发布成绩数量 (PUBLISHED)
    
    // 学生状态信息（学生查看考试时使用）
    private String studentStatus;
    private BigDecimal studentScore;
    private Instant studentStartTime;
    private Instant studentSubmitTime;

    // 用于前端显示的精确状态
    private String displayStatus;
} 