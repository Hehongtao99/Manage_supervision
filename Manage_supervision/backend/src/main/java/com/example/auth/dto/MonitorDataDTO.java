package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 监控数据DTO类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorDataDTO {
    private Long id;
    private Long userId;
    private String dataType;
    private Double value;
    private String expected;  // 格式化后的预期范围
    private Double expectedMin;
    private Double expectedMax;
    private Double deviation;
    private Boolean isAnomaly;
    private Boolean resolved;
    private LocalDateTime recordTime;
    private String description;
    private String source;
    private String username;  // 关联的用户名
} 