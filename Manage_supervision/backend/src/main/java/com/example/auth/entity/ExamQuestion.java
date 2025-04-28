package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 考试题目关联实体
 */
@Data
@Entity
@Table(name = "exam_questions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    @Column(name = "question_title", nullable = false)
    private String questionTitle;
    
    @Column(name = "question_type", nullable = false)
    private String questionType;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "question_score", nullable = false)
    private BigDecimal questionScore;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", insertable = false, updatable = false)
    private Exam exam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;
    
    // 添加答案字段用于ExamService.getExamQuestionsForStudent
    @Transient
    private String answer;
    
    @PrePersist
    protected void onCreate() {
        createTime = Instant.now();
        
        if (displayOrder == null) {
            displayOrder = 0;
        }
    }
} 