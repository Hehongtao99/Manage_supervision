package com.example.auth.dto;

import com.example.auth.entity.ProjectEvaluation;
import java.time.LocalDateTime;

/**
 * 课题评价DTO，用于前后端数据传输
 */
public class ProjectEvaluationDTO {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Integer score;
    private String comment;
    private Long evaluatedBy;
    private String evaluatorName;
    private LocalDateTime evaluationTime;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
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

    public String getEvaluatorName() {
        return evaluatorName;
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

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
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

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public void setEvaluationTime(LocalDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }

    /**
     * 从实体转换为DTO
     */
    public static ProjectEvaluationDTO fromEntity(ProjectEvaluation entity, String projectTitle, String evaluatorName) {
        ProjectEvaluationDTO dto = new ProjectEvaluationDTO();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProjectId());
        dto.setProjectTitle(projectTitle);
        dto.setScore(entity.getScore());
        dto.setComment(entity.getComment());
        dto.setEvaluatedBy(entity.getEvaluatedBy());
        dto.setEvaluatorName(evaluatorName);
        dto.setEvaluationTime(entity.getEvaluationTime());
        return dto;
    }
} 