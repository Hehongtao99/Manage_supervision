package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 教师收入统计数据传输对象
 */
@Data
public class TeacherIncomeStatsDTO {
    // 总收入
    private Double totalIncome;
    
    // 本月收入
    private Double thisMonthIncome;
    
    // 上月收入
    private Double lastMonthIncome;
    
    // 课程收入分布
    private List<TeacherCourseIncomeDTO> courseIncomeDistribution;
} 