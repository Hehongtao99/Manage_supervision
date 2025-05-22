package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.response.RunningRankingResponse;
import com.example.auth.model.dto.response.RunningReportResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.RunningReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 管理员跑步管理控制器
 */
@RestController
@RequestMapping("/api/admin/running")
@RequiredArgsConstructor
public class AdminRunningController {

    private final RunningReportService runningReportService;

    /**
     * 获取跑步距离排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     查询数量限制
     * @return 排行榜数据
     */
    @GetMapping("/ranking/distance")
    @RequireRole("ADMIN")
    public ResponseVO<List<RunningRankingResponse>> getDistanceRanking(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        // 如果没有指定日期范围，默认为当前一个月
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<RunningRankingResponse> ranking = runningReportService.getDistanceRanking(startDate, endDate, limit);
        return ResponseVO.success("获取跑步距离排行榜成功", ranking);
    }

    /**
     * 获取跑步次数排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     查询数量限制
     * @return 排行榜数据
     */
    @GetMapping("/ranking/frequency")
    @RequireRole("ADMIN")
    public ResponseVO<List<RunningRankingResponse>> getFrequencyRanking(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        // 如果没有指定日期范围，默认为当前一个月
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<RunningRankingResponse> ranking = runningReportService.getFrequencyRanking(startDate, endDate, limit);
        return ResponseVO.success("获取跑步次数排行榜成功", ranking);
    }

    /**
     * 获取最佳配速排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     查询数量限制
     * @return 排行榜数据
     */
    @GetMapping("/ranking/pace")
    @RequireRole("ADMIN")
    public ResponseVO<List<RunningRankingResponse>> getBestPaceRanking(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        // 如果没有指定日期范围，默认为当前一个月
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<RunningRankingResponse> ranking = runningReportService.getBestPaceRanking(startDate, endDate, limit);
        return ResponseVO.success("获取最佳配速排行榜成功", ranking);
    }

    /**
     * 获取跑步报表数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 报表数据
     */
    @GetMapping("/report")
    @RequireRole("ADMIN")
    public ResponseVO<RunningReportResponse> getRunningReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        // 如果没有指定日期范围，默认为当前一个月
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        RunningReportResponse report = runningReportService.getRunningReport(startDate, endDate);
        return ResponseVO.success("获取跑步报表数据成功", report);
    }

    /**
     * 获取每日跑步人数统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日跑步人数统计
     */
    @GetMapping("/daily-count")
    @RequireRole("ADMIN")
    public ResponseVO<Map<String, Integer>> getDailyRunningCount(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        // 如果没有指定日期范围，默认为当前一个月
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, Integer> dailyCount = runningReportService.getDailyRunningCount(startDate, endDate);
        return ResponseVO.success("获取每日跑步人数统计成功", dailyCount);
    }

    /**
     * 获取距离分布统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 距离分布统计
     */
    @GetMapping("/distance-distribution")
    @RequireRole("ADMIN")
    public ResponseVO<Map<String, Integer>> getDistanceDistribution(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        // 如果没有指定日期范围，默认为当前一个月
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        Map<String, Integer> distribution = runningReportService.getDistanceDistribution(startDate, endDate);
        return ResponseVO.success("获取距离分布统计成功", distribution);
    }
} 