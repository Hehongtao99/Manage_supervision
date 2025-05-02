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

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private UserContext userContext;

    @GetMapping("/admin/stats")
    @RequireRole("ADMIN")
    public ResponseEntity<DashboardStats> getAdminDashboardStats() {
        return ResponseEntity.ok(dashboardService.getFullDashboardStats());
    }

    @GetMapping("/stats")
    @RequireRole("USER")
    public ResponseEntity<DashboardStats> getUserDashboardStats() {
        return ResponseEntity.ok(dashboardService.getBasicDashboardStats());
    }
    
    /**
     * 获取督导员仪表盘数据
     * @return 督导员仪表盘数据
     */
    @GetMapping("/supervisor/stats")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<SupervisorDashboardDTO> getSupervisorDashboardStats() {
        // 获取当前登录的督导员ID
        Long supervisorId = userContext.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getSupervisorDashboardStats(supervisorId));
    }
} 