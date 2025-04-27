package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 考试实体类
 */
@Data
@Entity
@Table(name = "exams")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    
    @Column(name = "end_time", nullable = false)
    private Instant endTime;
    
    @Column(nullable = false)
    private Integer duration;
    
    @Column(name = "passing_score", nullable = false)
    private BigDecimal passingScore;
    
    @Column(name = "total_score", nullable = false)
    private BigDecimal totalScore;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;
    
    @Column(name = "update_time")
    private Instant updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = Instant.now();
        updateTime = Instant.now();
        
        // 默认状态为草稿
        if (status == null) {
            status = "DRAFT";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = Instant.now();
    }
} 