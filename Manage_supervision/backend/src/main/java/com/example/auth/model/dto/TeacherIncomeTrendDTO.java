package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 教师收入趋势数据传输对象
 */
@Data
public class TeacherIncomeTrendDTO {
    // 日期列表
    private List<String> dates;
    
    // 收入金额列表
    private List<Double> amounts;
} 