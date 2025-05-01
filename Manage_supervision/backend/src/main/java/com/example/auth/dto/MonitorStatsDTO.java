package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 监控数据统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorStatsDTO {
    
    /**
     * 总数据量
     */
    private Long totalRecords;
    
    /**
     * 异常数据数量
     */
    private Long anomalyCount;
    
    /**
     * 已处理的异常数据量
     */
    private Long resolvedCount;
    
    /**
     * 最近更新时间
     */
    private LocalDateTime lastUpdated;
    
    /**
     * 按数据类型统计的异常数量
     */
    private Map<String, Long> anomaliesByType;
} 