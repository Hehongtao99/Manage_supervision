package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 考试学生关联实体
 * 状态说明：
 * NOT_STARTED: 未开始
 * IN_PROGRESS: 进行中
 * SUBMITTED: 已提交
 * GRADED: 已评分（教师已批阅但未发布）
 * PENDING_PUBLISH: 待发布（教师已批阅但未发布）
 * PUBLISHED: 已发布（学生可查看成绩）
 */
@Data
@Entity
@Table(name = "exam_students")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamStudent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "start_time")
    private Instant startTime;
    
    @Column(name = "submit_time")
    private Instant submitTime;
    
    @Column
    private BigDecimal score;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", insertable = false, updatable = false)
    private Exam exam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
    
    @PrePersist
    protected void onCreate() {
        createTime = Instant.now();
        
        if (status == null) {
            status = "NOT_STARTED";
        }
    }
} 