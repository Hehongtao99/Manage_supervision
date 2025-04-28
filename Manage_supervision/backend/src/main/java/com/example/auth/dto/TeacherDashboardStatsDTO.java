package com.example.auth.dto;

import lombok.Data;

/**
 * 教师仪表盘基础统计数据 DTO
 */
@Data
public class TeacherDashboardStatsDTO {
    private long studentCount;      // 管理学生数
    private long examCount;         // 已发布考试数
    // 未读消息数将从前端 chatStore 获取，后端暂不提供
} 