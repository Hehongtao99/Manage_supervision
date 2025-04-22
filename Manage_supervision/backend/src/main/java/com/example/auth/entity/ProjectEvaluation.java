package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 课题评价实体类，存储督导对已完成课题的评价
 */
@Entity
@Table(name = "project_evaluations")
public class ProjectEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "score")
    private Integer score;  // 评分，例如1-100分

    @Column(name = "comment", length = 1000)
    private String comment; // 评语

    @Column(name = "evaluated_by", nullable = false)
    private Long evaluatedBy; // 评价人ID（督导ID）

    @Column(name = "evaluation_time")
    private LocalDateTime evaluationTime;

    @PrePersist
    protected void onCreate() {
        evaluationTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Integer getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public Long getEvaluatedBy() {
        return evaluatedBy;
    }

    public LocalDateTime getEvaluationTime() {
        return evaluationTime;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setEvaluatedBy(Long evaluatedBy) {
        this.evaluatedBy = evaluatedBy;
    }

    public void setEvaluationTime(LocalDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }
} 