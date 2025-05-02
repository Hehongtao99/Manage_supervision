package com.example.auth.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生课题进度DTO
 * 用于督导员控制台展示学生课题进度信息
 */
@Data
public class StudentProjectProgressDTO {
    // 学生信息
    private Long studentId;
    private String studentName;
    private String studentNumber; // 学号
    
    // 课题信息
    private Long projectId;
    private String projectTitle;
    private String projectStatus;
    
    // 进度信息
    private Integer totalTasks;     // 总任务数
    private Integer completedTasks; // 已完成任务数
    private Double progressPercentage; // 进度百分比
    
    // 最近活动
    private LocalDateTime lastActivity;
    private String lastActivityDescription;
    
    // 状态信息
    private String status; // 活跃、非活跃
    
    // 期限信息
    private LocalDateTime deadline;
    private Boolean isOverdue;
    
    // 计算剩余任务数
    public Integer getRemainingTasks() {
        return totalTasks - completedTasks;
    }
    
    // 计算是否接近截止日期（7天内）
    public Boolean isNearDeadline() {
        if (deadline == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);
        return deadline.isBefore(weekLater) && deadline.isAfter(now);
    }
} 