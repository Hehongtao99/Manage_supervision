package com.example.auth.dto;

import com.example.auth.entity.TaskSubmission;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 任务提交数据传输对象
 */
public class TaskSubmissionDTO implements Serializable {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private String filePath;
    private String originalFilename;
    private String fileType;
    private Long fileSize;
    private Long submitterId;
    private String submitterName;
    private String submissionTime;
    private String comment;

    public TaskSubmissionDTO() {
    }

    /**
     * 从实体创建DTO
     */
    public static TaskSubmissionDTO fromEntity(TaskSubmission submission, String taskTitle, String submitterName) {
        TaskSubmissionDTO dto = new TaskSubmissionDTO();
        dto.setId(submission.getId());
        dto.setTaskId(submission.getTaskId());
        dto.setTaskTitle(taskTitle);
        dto.setFilePath(submission.getFilePath());
        dto.setOriginalFilename(submission.getOriginalFilename());
        dto.setFileType(submission.getFileType());
        dto.setFileSize(submission.getFileSize());
        dto.setSubmitterId(submission.getSubmitterId());
        dto.setSubmitterName(submitterName);
        
        // 格式化日期时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (submission.getSubmissionTime() != null) {
            dto.setSubmissionTime(submission.getSubmissionTime().format(formatter));
        }
        
        dto.setComment(submission.getComment());
        return dto;
    }

    /**
     * 转换为实体
     */
    public TaskSubmission toEntity() {
        TaskSubmission submission = new TaskSubmission();
        
        if (this.id != null) {
            submission.setId(this.id);
        }
        
        submission.setTaskId(this.taskId);
        submission.setFilePath(this.filePath);
        submission.setOriginalFilename(this.originalFilename);
        submission.setFileType(this.fileType);
        submission.setFileSize(this.fileSize);
        submission.setSubmitterId(this.submitterId);
        
        // 解析日期时间
        if (this.submissionTime != null && !this.submissionTime.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            submission.setSubmissionTime(LocalDateTime.parse(this.submissionTime, formatter));
        }
        
        submission.setComment(this.comment);
        
        return submission;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(Long submitterId) {
        this.submitterId = submitterId;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
} 