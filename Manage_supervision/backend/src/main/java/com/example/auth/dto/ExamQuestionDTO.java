package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDTO {
    
    private Long id;
    
    private Long examId;
    
    private Long questionId;
    
    private String questionTitle;
    
    private String questionType;
    
    private String content;
    
    private BigDecimal questionScore;
    
    private Integer displayOrder;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    // 关联的完整题目信息（可选）
    private QuestionDTO question;
    
    // 正确答案（用于结果展示）
    private String answer;
} 