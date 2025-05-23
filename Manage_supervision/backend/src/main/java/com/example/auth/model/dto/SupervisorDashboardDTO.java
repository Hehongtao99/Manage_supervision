package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 督导员仪表盘DTO
 * 用于督导员控制台展示统计数据
 */
@Data
public class SupervisorDashboardDTO {
    // 统计信息
    private Integer runnerCount;       // 跑步爱好者总数
    private Integer activeToday;       // 今日活跃用户数
    private Integer pendingTasks;      // 待处理事项数
    private Integer weeklyEvents;      // 本周活动数
    
    // 最近活动列表
    @Data
    public static class ActivityItem {
        private String title;          // 活动标题
        private String description;    // 活动描述
        private String time;           // 活动时间
        private String type;           // 活动类型：跑步记录、注册、系统通知等
    }
    
    private List<ActivityItem> recentActivities;
    
    // 为了兼容性，暂时保留原字段名的getter/setter
    public Integer getStudentCount() {
        return runnerCount;
    }
    
    public void setStudentCount(Integer studentCount) {
        this.runnerCount = studentCount;
    }
} 