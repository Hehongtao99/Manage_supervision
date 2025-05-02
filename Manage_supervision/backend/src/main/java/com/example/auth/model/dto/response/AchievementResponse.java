package com.example.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户成就响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    /**
     * 总跑步里程
     */
    private Double totalDistance;
    
    /**
     * 当前段位
     */
    private String currentLevel;
    
    /**
     * 下一个段位
     */
    private String nextLevel;
    
    /**
     * 已获得的所有段位
     */
    private List<String> achievedLevels;
} 