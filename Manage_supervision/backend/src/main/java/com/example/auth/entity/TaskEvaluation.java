package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务评价实体类，存储督导对已完成任务的评价
 */
@Entity
@Table(name = "task_evaluations")
public class TaskEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

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

    public Long getTaskId() {
        return taskId;
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

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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