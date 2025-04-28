package com.example.auth.service;

import com.example.auth.dto.DashboardStats;
import com.example.auth.dto.TeacherDashboardStatsDTO;
// import com.example.auth.dto.SupervisorDashboardDTO; // Removed unused import

import java.util.Map; // Import Map

public interface DashboardService {
    DashboardStats getFullDashboardStats();  // 管理员使用
    DashboardStats getBasicDashboardStats(); // 普通用户使用
    
    /**
     * 获取教师仪表盘统计数据
     * @param teacherId 教师ID
     * @return 教师仪表盘统计数据
     */
    TeacherDashboardStatsDTO getTeacherDashboardStats(Long teacherId);

    /**
     * 获取指定考试的成绩分布
     * @param examId 考试ID
     * @return Map<String, Long> Key为分数段描述, Value为该分数段人数
     */
    Map<String, Long> getExamGradeDistribution(Long examId);

    /**
     * 获取教师创建的所有考试的整体参与情况统计
     * @param teacherId 教师ID
     * @return Map<String, Long> Key为参与状态描述, Value为该状态总人数
     */
    Map<String, Long> getExamParticipationStats(Long teacherId);

    /**
     * 获取教师考试成绩趋势数据
     * @param teacherId 教师ID
     * @param range 时间范围 ("week", "month", "semester")
     * @return Map 包含 "dates" (List<String>), "averageScores" (List<Double>), "highestScores" (List<Double>)
     */
    Map<String, Object> getGradeTrendData(Long teacherId, String range);

    // Remove Supervisor Dashboard service method declaration
    // /**
    //  * 获取督导员仪表盘数据
    //  * @param supervisorId 督导员ID
    //  * @return 督导员仪表盘数据
    //  */
    // SupervisorDashboardDTO getSupervisorDashboardStats(Long supervisorId);
} 