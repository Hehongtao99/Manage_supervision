package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 监控数据查询参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorDataQueryDTO {
    
    /**
     * 开始日期
     */
    private LocalDateTime startDate;
    
    /**
     * 结束日期
     */
    private LocalDateTime endDate;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 数据类型
     */
    private String dataType;
    
    /**
     * 异常阈值
     */
    private Double threshold;
    
    /**
     * 是否只查询异常数据
     */
    private Boolean anomalyOnly;
    
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 每页数量
     */
    private Integer size;
} 