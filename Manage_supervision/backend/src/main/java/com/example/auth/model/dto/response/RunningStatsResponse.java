package com.example.auth.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 跑步统计数据响应DTO
 */
public class RunningStatsResponse {
    
    /**
     * 周维度跑量统计
     * key: 周标识 (yyyy-ww 格式，例如 2023-01 表示2023年第1周)
     * value: 跑步距离总和，单位：公里
     */
    private Map<String, BigDecimal> weeklyDistanceStats;
    
    /**
     * 月维度配速统计
     * key: 日期 (yyyy-MM-dd 格式)
     * value: 当天配速，格式：分钟:秒/公里
     */
    private List<Map<String, Object>> monthlyPaceStats;
    
    /**
     * 总跑量（公里）
     */
    private BigDecimal totalDistance;
    
    /**
     * 总跑步次数
     */
    private Integer totalRunCount;
    
    /**
     * 平均配速
     */
    private String averagePace;
    
    /**
     * 最好配速
     */
    private String bestPace;
    
    /**
     * 本周总跑量
     */
    private BigDecimal currentWeekDistance;
    
    /**
     * 本月总跑量
     */
    private BigDecimal currentMonthDistance;

    // Getters and Setters
    
    public Map<String, BigDecimal> getWeeklyDistanceStats() {
        return weeklyDistanceStats;
    }

    public void setWeeklyDistanceStats(Map<String, BigDecimal> weeklyDistanceStats) {
        this.weeklyDistanceStats = weeklyDistanceStats;
    }

    public List<Map<String, Object>> getMonthlyPaceStats() {
        return monthlyPaceStats;
    }

    public void setMonthlyPaceStats(List<Map<String, Object>> monthlyPaceStats) {
        this.monthlyPaceStats = monthlyPaceStats;
    }

    public BigDecimal getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(BigDecimal totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Integer getTotalRunCount() {
        return totalRunCount;
    }

    public void setTotalRunCount(Integer totalRunCount) {
        this.totalRunCount = totalRunCount;
    }

    public String getAveragePace() {
        return averagePace;
    }

    public void setAveragePace(String averagePace) {
        this.averagePace = averagePace;
    }

    public String getBestPace() {
        return bestPace;
    }

    public void setBestPace(String bestPace) {
        this.bestPace = bestPace;
    }

    public BigDecimal getCurrentWeekDistance() {
        return currentWeekDistance;
    }

    public void setCurrentWeekDistance(BigDecimal currentWeekDistance) {
        this.currentWeekDistance = currentWeekDistance;
    }

    public BigDecimal getCurrentMonthDistance() {
        return currentMonthDistance;
    }

    public void setCurrentMonthDistance(BigDecimal currentMonthDistance) {
        this.currentMonthDistance = currentMonthDistance;
    }
} 