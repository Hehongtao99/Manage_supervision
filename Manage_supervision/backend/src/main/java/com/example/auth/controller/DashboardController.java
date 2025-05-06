package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.DashboardStats;
import com.example.auth.dto.TeacherDashboardStatsDTO;
import com.example.auth.service.DashboardService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private UserContext userContext;

    @GetMapping("/admin/stats")
    @RequireRole({"ADMIN"})
    public ResponseEntity<DashboardStats> getAdminDashboardStats() {
        return ResponseEntity.ok(dashboardService.getFullDashboardStats());
    }

    @GetMapping("/stats")
    @RequireRole({"USER"})
    public ResponseEntity<DashboardStats> getUserDashboardStats() {
        return ResponseEntity.ok(dashboardService.getBasicDashboardStats());
    }
    
    /**
     * 获取教师仪表盘统计数据
     * @return 教师仪表盘统计数据
     */
    @GetMapping("/teacher/stats")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<TeacherDashboardStatsDTO> getTeacherDashboardStats() {
        Long teacherId = userContext.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getTeacherDashboardStats(teacherId));
    }

    /**
     * 获取指定考试的学生成绩分布
     * @param examId 考试ID
     * @return 成绩分布数据 (例如：{"不及格": 2, "60-70分": 3, ...})
     */
    @GetMapping("/teacher/exam/{examId}/grades")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<Map<String, Long>> getExamGradeDistribution(@PathVariable Long examId) {
        // 权限验证：确保当前教师有权访问此考试信息 (通常在service层处理更佳)
        // Long teacherId = userContext.getCurrentUser().getId();
        // examService.validateExamCreator(examId, teacherId);
        return ResponseEntity.ok(dashboardService.getExamGradeDistribution(examId));
    }

    /**
     * 获取教师创建的所有考试的整体参与情况统计
     * @return Map<String, Long> Key为参与状态描述, Value为该状态总人数
     */
    @GetMapping("/teacher/participation-stats")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<Map<String, Long>> getExamParticipationStats() {
        Long teacherId = userContext.getCurrentUser().getId();
        return ResponseEntity.ok(dashboardService.getExamParticipationStats(teacherId));
    }

    /**
     * 获取教师考试成绩趋势
     * @param range 时间范围 ("week", "month", "semester")
     * @return 包含日期、平均分、最高分列表的数据
     */
    @GetMapping("/teacher/grade-trend")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<Map<String, Object>> getGradeTrendData(
            @RequestParam(defaultValue = "month") String range) {
        Long teacherId = userContext.getCurrentUser().getId();
        Map<String, Object> trendData = dashboardService.getGradeTrendData(teacherId, range);
        return ResponseEntity.ok(trendData);
    }

    @GetMapping("/admin/dashboard")
    @RequireRole({"ADMIN"})
    public ResponseEntity<?> getAdminDashboard() {
        // Implementation of getAdminDashboard method
        return null; // Placeholder return, actual implementation needed
    }

    @GetMapping("/user/dashboard")
    @RequireRole({"USER"})
    public ResponseEntity<?> getUserDashboard(@RequestHeader("Authorization") String auth) {
        // Implementation of getUserDashboard method
        return null; // Placeholder return, actual implementation needed
    }

    @GetMapping("/supervisor/dashboard")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getSupervisorDashboard(@RequestHeader("Authorization") String auth) {
        // Implementation of getSupervisorDashboard method
        return null; // Placeholder return, actual implementation needed
    }

    @GetMapping("/supervisor/dashboard/recent-activity")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getRecentActivity(@RequestHeader("Authorization") String auth) {
        // Implementation of getRecentActivity method
        return null; // Placeholder return, actual implementation needed
    }

    @GetMapping("/supervisor/dashboard/task-status")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getTaskStatus(@RequestHeader("Authorization") String auth) {
        // Implementation of getTaskStatus method
        return null; // Placeholder return, actual implementation needed
    }

    @GetMapping("/supervisor/dashboard/project-status")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getProjectStatus(@RequestHeader("Authorization") String auth) {
        // Implementation of getProjectStatus method
        return null; // Placeholder return, actual implementation needed
    }
} 