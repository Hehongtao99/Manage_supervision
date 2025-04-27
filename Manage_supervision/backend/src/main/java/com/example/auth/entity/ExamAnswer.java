package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 考试答案实体
 */
@Data
@Entity
@Table(name = "exam_answers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    @Column(columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "is_correct")
    private Boolean isCorrect;
    
    @Column
    private BigDecimal score;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;
    
    @Column(name = "update_time")
    private Instant updateTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", insertable = false, updatable = false)
    private Exam exam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;
    
    @PrePersist
    protected void onCreate() {
        createTime = Instant.now();
        updateTime = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = Instant.now();
    }
} 