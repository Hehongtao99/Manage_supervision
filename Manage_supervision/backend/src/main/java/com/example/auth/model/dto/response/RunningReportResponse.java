package com.example.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 跑步报表响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunningReportResponse {
    
    /**
     * 统计的开始日期
     */
    private String startDate;
    
    /**
     * 统计的结束日期
     */
    private String endDate;
    
    /**
     * 总跑步人数
     */
    private Integer totalRunners;
    
    /**
     * 总跑步次数
     */
    private Integer totalRunCount;
    
    /**
     * 总跑步距离
     */
    private BigDecimal totalDistance;
    
    /**
     * 平均跑步距离
     */
    private BigDecimal avgDistance;
    
    /**
     * 平均每人跑步次数
     */
    private BigDecimal avgRunCountPerPerson;
    
    /**
     * 日均跑步人数
     */
    private BigDecimal avgDailyRunners;
    
    /**
     * 每日跑步人数统计
     */
    private Map<String, Integer> dailyRunnerCount;
    
    /**
     * 距离分布统计
     */
    private Map<String, Integer> distanceDistribution;
    
    /**
     * 距离排行榜前10
     */
    private List<RunningRankingResponse> distanceRanking;
    
    /**
     * 次数排行榜前10
     */
    private List<RunningRankingResponse> frequencyRanking;
    
    /**
     * 配速排行榜前10
     */
    private List<RunningRankingResponse> paceRanking;
} 