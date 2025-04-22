package com.example.auth.dto;

import com.example.auth.entity.TaskEvaluation;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 任务评价数据传输对象
 */
public class TaskEvaluationDTO implements Serializable {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Integer score;
    private String comment;
    private Long evaluatedBy;
    private String evaluatorName;
    private String evaluationTime;

    // 默认构造函数
    public TaskEvaluationDTO() {
    }

    /**
     * 从实体创建DTO
     */
    public static TaskEvaluationDTO fromEntity(TaskEvaluation evaluation, String taskTitle, String evaluatorName) {
        TaskEvaluationDTO dto = new TaskEvaluationDTO();
        dto.setId(evaluation.getId());
        dto.setTaskId(evaluation.getTaskId());
        dto.setTaskTitle(taskTitle);
        dto.setScore(evaluation.getScore());
        dto.setComment(evaluation.getComment());
        dto.setEvaluatedBy(evaluation.getEvaluatedBy());
        dto.setEvaluatorName(evaluatorName);
        
        // 格式化日期时间
        if (evaluation.getEvaluationTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setEvaluationTime(evaluation.getEvaluationTime().format(formatter));
        }
        
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getEvaluatedBy() {
        return evaluatedBy;
    }

    public void setEvaluatedBy(Long evaluatedBy) {
        this.evaluatedBy = evaluatedBy;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public String getEvaluationTime() {
        return evaluationTime;
    }

    public void setEvaluationTime(String evaluationTime) {
        this.evaluationTime = evaluationTime;
    }
} 