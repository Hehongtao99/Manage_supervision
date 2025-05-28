package com.example.auth.service;

import com.example.auth.model.dto.DashboardStats;

public interface DashboardService {
    DashboardStats getFullDashboardStats();  // 管理员使用
    DashboardStats getBasicDashboardStats(); // 普通用户使用
} 