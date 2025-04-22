package com.example.auth.dto;

import com.example.auth.entity.Task;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 任务数据传输对象
 */
public class TaskDTO implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String status;
    private String priority;
    private Long supervisorId;
    private String supervisorName;
    private Long assigneeId;
    private String assigneeName;
    private Long projectId;
    private Boolean completed;
    private String createTime;
    private String updateTime;

    public TaskDTO() {
    }

    public TaskDTO(Long id, String title, String description, String startTime, String endTime,
                   String status, String priority, Long supervisorId, String supervisorName,
                   Long assigneeId, String assigneeName, Long projectId, Boolean completed,
                   String createTime, String updateTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.priority = priority;
        this.supervisorId = supervisorId;
        this.supervisorName = supervisorName;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.projectId = projectId;
        this.completed = completed;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /**
     * 将Task实体转换为TaskDTO
     */
    public static TaskDTO fromEntity(Task task, String supervisorName, String assigneeName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().format(formatter) : null;
        String endTimeStr = task.getEndTime() != null ? task.getEndTime().format(formatter) : null;
        String createTimeStr = task.getCreateTime() != null ? task.getCreateTime().format(formatter) : null;
        String updateTimeStr = task.getUpdateTime() != null ? task.getUpdateTime().format(formatter) : null;
        
        return new TaskDTO(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            startTimeStr,
            endTimeStr,
            task.getStatus(),
            task.getPriority(),
            task.getSupervisorId(),
            supervisorName,
            task.getAssigneeId(),
            assigneeName,
            task.getProjectId(),
            task.getCompleted(),
            createTimeStr,
            updateTimeStr
        );
    }

    /**
     * 将TaskDTO转换为Task实体
     */
    public Task toEntity() {
        Task task = new Task();
        
        if (this.id != null) {
            task.setId(this.id);
        }
        
        task.setTitle(this.title);
        task.setDescription(this.description);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (this.startTime != null && !this.startTime.isEmpty()) {
            task.setStartTime(LocalDateTime.parse(this.startTime, formatter));
        }
        
        if (this.endTime != null && !this.endTime.isEmpty()) {
            task.setEndTime(LocalDateTime.parse(this.endTime, formatter));
        }
        
        task.setStatus(this.status);
        task.setPriority(this.priority);
        task.setSupervisorId(this.supervisorId);
        task.setAssigneeId(this.assigneeId);
        task.setProjectId(this.projectId);
        
        if (this.completed != null) {
            task.setCompleted(this.completed);
        } else {
            task.setCompleted(false);
        }
        
        return task;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }
    
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "TaskDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", projectId=" + projectId +
                ", status='" + status + '\'' +
                ", supervisorId=" + supervisorId +
                ", assigneeId=" + assigneeId +
                ", completed=" + completed +
                '}';
    }
} 