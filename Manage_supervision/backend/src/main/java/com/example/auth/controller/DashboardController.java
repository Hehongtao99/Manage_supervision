package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.DashboardStats;
import com.example.auth.model.dto.SupervisorDashboardDTO;
import com.example.auth.service.DashboardService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private UserContext userContext;

    @GetMapping("/admin/stats")
    @RequireRole("ADMIN_END")
    public ResponseEntity<DashboardStats> getAdminDashboardStats() {
        return ResponseEntity.ok(dashboardService.getFullDashboardStats());
    }

    @GetMapping("/stats")
    @RequireRole("ENTERPRISE_END")
    public ResponseEntity<DashboardStats> getUserDashboardStats() {
        return ResponseEntity.ok(dashboardService.getBasicDashboardStats());
    }
    
    /**
     * 获取督导员仪表盘数据
     * @return 督导员仪表盘数据
     */
    @GetMapping("/supervisor/stats")
    @RequireRole("CITYIN_END")
    public ResponseEntity<SupervisorDashboardDTO> getSupervisorDashboardStats() {
        // 获取当前登录的督导员ID
        Long supervisorId = userContext.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getSupervisorDashboardStats(supervisorId));
    }
    
    /**
     * 获取近7天用户创建趋势数据
     * @return 日期和用户创建数量的映射
     */
    @GetMapping("/admin/user-creation-trend")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getUserCreationTrend() {
        return ResponseEntity.ok(dashboardService.getUserCreationTrend());
    }
} 