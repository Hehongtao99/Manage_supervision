package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 督导员仪表盘DTO
 * 用于督导员控制台展示统计数据和学生课题进度
 */
@Data
public class SupervisorDashboardDTO {
    // 统计信息
    private Integer studentCount;      // 学生总数
    private Integer activeToday;       // 今日活跃学生数
    private Integer pendingTasks;      // 待处理事项数
    private Integer weeklyEvents;      // 本周活动数
    
    // 学生课题进度列表
    private List<StudentProjectProgressDTO> studentProjectProgresses;
    
    // 最近活动列表
    @Data
    public static class ActivityItem {
        private String title;          // 活动标题
        private String description;    // 活动描述
        private String time;           // 活动时间
        private String type;           // 活动类型：作业提交、请假、注册、系统通知等
    }
    
    private List<ActivityItem> recentActivities;
} 