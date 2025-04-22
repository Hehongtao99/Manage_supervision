package com.example.auth.dto;

import java.io.Serializable;

/**
 * 批量任务请求类，用于批量创建任务
 */
public class TaskRequest implements Serializable {
    private String titleTemplate; // 任务标题模板，可以包含 {学生名} 等占位符
    private String descriptionTemplate; // 任务描述模板，可以包含 {学生名}、{项目名} 等占位符
    private String priority; // 优先级
    private String status; // 状态
    private String startTime; // 开始时间
    private String endTime; // 结束时间
    private boolean assignToAllStudents; // 是否分配给项目下所有学生
    
    public TaskRequest() {
    }
    
    public TaskRequest(String titleTemplate, String descriptionTemplate, String priority, 
                       String status, String startTime, String endTime, boolean assignToAllStudents) {
        this.titleTemplate = titleTemplate;
        this.descriptionTemplate = descriptionTemplate;
        this.priority = priority;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.assignToAllStudents = assignToAllStudents;
    }
    
    // Getters and Setters
    public String getTitleTemplate() {
        return titleTemplate;
    }
    
    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }
    
    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }
    
    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public boolean isAssignToAllStudents() {
        return assignToAllStudents;
    }
    
    public void setAssignToAllStudents(boolean assignToAllStudents) {
        this.assignToAllStudents = assignToAllStudents;
    }
} 