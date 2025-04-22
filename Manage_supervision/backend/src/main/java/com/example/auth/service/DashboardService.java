package com.example.auth.service;

import com.example.auth.dto.DashboardStats;
import com.example.auth.dto.SupervisorDashboardDTO;

public interface DashboardService {
    DashboardStats getFullDashboardStats();  // 管理员使用
    DashboardStats getBasicDashboardStats(); // 普通用户使用
    
    /**
     * 获取督导员仪表盘数据
     * @param supervisorId 督导员ID
     * @return 督导员仪表盘数据
     */
    SupervisorDashboardDTO getSupervisorDashboardStats(Long supervisorId);
} 