package com.example.auth.service;

import com.example.auth.model.dto.TeacherCourseIncomeDTO;
import com.example.auth.model.dto.TeacherIncomeStatsDTO;
import com.example.auth.model.dto.TeacherIncomeTrendDTO;

import java.util.List;

/**
 * 收入报表服务接口
 */
public interface IncomeReportService {
    
    /**
     * 获取教师收入趋势
     * @param teacherId 教师ID
     * @param period 时间周期：week - 近7天，month - 近30天，year - 近12个月
     * @return 收入趋势数据
     */
    TeacherIncomeTrendDTO getIncomeTrend(Long teacherId, String period);
    
    /**
     * 获取教师课程收入分布
     * @param teacherId 教师ID
     * @param period 时间周期：month - 当月，year - 当年，all - 所有时间
     * @return 课程收入分布数据
     */
    List<TeacherCourseIncomeDTO> getCourseIncomeDistribution(Long teacherId, String period);
    
    /**
     * 获取教师收入统计数据
     * @param teacherId 教师ID
     * @return 收入统计数据
     */
    TeacherIncomeStatsDTO getIncomeStats(Long teacherId);
} 