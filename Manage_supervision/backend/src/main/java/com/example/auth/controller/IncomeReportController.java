package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.TeacherCourseIncomeDTO;
import com.example.auth.model.dto.TeacherIncomeStatsDTO;
import com.example.auth.model.dto.TeacherIncomeTrendDTO;
import com.example.auth.service.IncomeReportService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收入报表控制器
 */
@RestController
@RequestMapping("/api/supervisor/income")
public class IncomeReportController {

    private static final Logger logger = LoggerFactory.getLogger(IncomeReportController.class);

    @Autowired
    private IncomeReportService incomeReportService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取收入趋势数据
     * @param auth JWT令牌
     * @param period 时间周期：week - 近7天，month - 近30天，year - 近12个月
     * @return 收入趋势数据
     */
    @GetMapping("/trend")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<TeacherIncomeTrendDTO> getIncomeTrend(
            @RequestHeader("Authorization") String auth,
            @RequestParam(value = "period", defaultValue = "month") String period) {
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师请求收入趋势数据，ID: {}, 周期: {}", teacherId, period);
            
            // 验证周期参数
            if (!period.equals("week") && !period.equals("month") && !period.equals("year")) {
                period = "month"; // 默认为month
            }
            
            TeacherIncomeTrendDTO result = incomeReportService.getIncomeTrend(teacherId, period);
            logger.info("生成收入趋势数据成功，日期数量: {}, 金额数量: {}", 
                       result.getDates().size(), 
                       result.getAmounts().size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取收入趋势数据失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取课程收入分布数据
     * @param auth JWT令牌
     * @param period 时间周期：month - 当月，year - 当年，all - 所有时间
     * @return 课程收入分布数据
     */
    @GetMapping("/distribution")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<List<TeacherCourseIncomeDTO>> getCourseIncomeDistribution(
            @RequestHeader("Authorization") String auth,
            @RequestParam(value = "period", defaultValue = "month") String period) {
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师请求课程收入分布数据，ID: {}, 周期: {}", teacherId, period);
            
            // 验证周期参数
            if (!period.equals("month") && !period.equals("year") && !period.equals("all")) {
                period = "month"; // 默认为month
            }
            
            List<TeacherCourseIncomeDTO> result = incomeReportService.getCourseIncomeDistribution(teacherId, period);
            logger.info("生成课程收入分布数据成功，课程数量: {}", result.size());
            if (!result.isEmpty()) {
                logger.info("示例课程数据 - 名称: {}, 金额: {}", result.get(0).getCourseName(), result.get(0).getAmount());
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取课程收入分布数据失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取收入统计数据
     * @param auth JWT令牌
     * @return 收入统计数据
     */
    @GetMapping("/stats")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<TeacherIncomeStatsDTO> getIncomeStats(
            @RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师请求收入统计数据，ID: {}", teacherId);
            
            TeacherIncomeStatsDTO result = incomeReportService.getIncomeStats(teacherId);
            logger.info("生成收入统计数据成功 - 总收入: {}, 本月收入: {}, 上月收入: {}, 课程分布项数: {}", 
                       result.getTotalIncome(), 
                       result.getThisMonthIncome(), 
                       result.getLastMonthIncome(),
                       result.getCourseIncomeDistribution().size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取收入统计数据失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 