package com.example.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 跑步排名响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunningRankingResponse {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户真实姓名
     */
    private String realName;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 总跑步距离
     */
    private BigDecimal totalDistance;
    
    /**
     * 跑步次数
     */
    private Integer runCount;
    
    /**
     * 最佳配速
     */
    private String bestPace;
    
    /**
     * 平均配速
     */
    private String averagePace;
} 