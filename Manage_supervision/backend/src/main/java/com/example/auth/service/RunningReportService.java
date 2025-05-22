package com.example.auth.service;

import com.example.auth.model.dto.response.RunningRankingResponse;
import com.example.auth.model.dto.response.RunningReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 跑步报表服务接口
 */
public interface RunningReportService {
    
    /**
     * 获取跑步距离排行榜
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 查询数量限制
     * @return 排行榜数据
     */
    List<RunningRankingResponse> getDistanceRanking(LocalDate startDate, LocalDate endDate, Integer limit);
    
    /**
     * 获取跑步次数排行榜
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 查询数量限制
     * @return 排行榜数据
     */
    List<RunningRankingResponse> getFrequencyRanking(LocalDate startDate, LocalDate endDate, Integer limit);
    
    /**
     * 获取最佳配速排行榜
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 查询数量限制
     * @return 排行榜数据
     */
    List<RunningRankingResponse> getBestPaceRanking(LocalDate startDate, LocalDate endDate, Integer limit);
    
    /**
     * 获取跑步总体报表数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表数据
     */
    RunningReportResponse getRunningReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户每日跑步情况
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日跑步数据统计
     */
    Map<String, Integer> getDailyRunningCount(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取不同距离段的跑步人数分布
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 距离分布数据
     */
    Map<String, Integer> getDistanceDistribution(LocalDate startDate, LocalDate endDate);
} 