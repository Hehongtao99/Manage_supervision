package com.example.auth.dto;

import com.example.auth.entity.Project;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 课题数据传输对象
 */
public class ProjectDTO implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String startTime;
    private String endTime;
    private String status;
    private Long supervisorId;
    private String supervisorName;
    private Long assigneeId;
    private String assigneeName;
    private String createTime;
    private String updateTime;
    private String requirements;
    private String resources;
    private TaskRequest taskRequest;
    private List<TaskDTO> tasks;

    public ProjectDTO() {
    }

    public ProjectDTO(Long id, String title, String description, String category,
                      String startTime, String endTime, String status,
                      Long supervisorId, String supervisorName,
                      Long assigneeId, String assigneeName,
                      String createTime, String updateTime,
                      String requirements, String resources) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.supervisorId = supervisorId;
        this.supervisorName = supervisorName;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.requirements = requirements;
        this.resources = resources;
    }

    /**
     * 将Project实体转换为ProjectDTO
     */
    public static ProjectDTO fromEntity(Project project, String supervisorName, String assigneeName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String startTimeStr = project.getStartTime() != null ? project.getStartTime().format(formatter) : null;
        String endTimeStr = project.getEndTime() != null ? project.getEndTime().format(formatter) : null;
        String createTimeStr = project.getCreateTime() != null ? project.getCreateTime().format(formatter) : null;
        String updateTimeStr = project.getUpdateTime() != null ? project.getUpdateTime().format(formatter) : null;
        
        return new ProjectDTO(
            project.getId(),
            project.getTitle(),
            project.getDescription(),
            project.getCategory(),
            startTimeStr,
            endTimeStr,
            project.getStatus(),
            project.getSupervisorId(),
            supervisorName,
            project.getAssigneeId(),
            assigneeName,
            createTimeStr,
            updateTimeStr,
            project.getRequirements(),
            project.getResources()
        );
    }

    /**
     * 将ProjectDTO转换为Project实体
     */
    public Project toEntity() {
        Project project = new Project();
        
        if (this.id != null) {
            project.setId(this.id);
        }
        
        project.setTitle(this.title);
        project.setDescription(this.description);
        project.setCategory(this.category);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (this.startTime != null && !this.startTime.isEmpty()) {
            project.setStartTime(LocalDateTime.parse(this.startTime, formatter));
        }
        
        if (this.endTime != null && !this.endTime.isEmpty()) {
            project.setEndTime(LocalDateTime.parse(this.endTime, formatter));
        }
        
        project.setStatus(this.status);
        project.setSupervisorId(this.supervisorId);
        project.setAssigneeId(this.assigneeId);
        project.setRequirements(this.requirements);
        project.setResources(this.resources);
        
        return project;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }
    
    public List<TaskDTO> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }
}