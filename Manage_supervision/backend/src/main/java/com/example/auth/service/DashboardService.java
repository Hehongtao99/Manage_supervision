package com.example.auth.service;

import com.example.auth.model.dto.DashboardStats;
import com.example.auth.model.dto.SupervisorDashboardDTO;

import java.util.Map;

public interface DashboardService {
    DashboardStats getFullDashboardStats();  // 管理员使用
    DashboardStats getBasicDashboardStats(); // 普通用户使用
    
    /**
     * 获取督导员仪表盘数据
     * @param supervisorId 督导员ID
     * @return 督导员仪表盘数据
     */
    SupervisorDashboardDTO getSupervisorDashboardStats(Long supervisorId);
    
    /**
     * 获取近7天用户创建趋势数据
     * @return 包含日期和对应创建用户数的Map
     */
    Map<String, Object> getUserCreationTrend();
} 