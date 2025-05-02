package com.example.auth.model.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 跑步记录请求DTO
 */
public class RunningRecordRequest {
    
    /**
     * 跑步距离，单位：公里
     */
    private BigDecimal distance;
    
    /**
     * 跑步时长，单位：分钟
     */
    private Integer duration;
    
    /**
     * 配速，格式：分钟:秒/公里
     */
    private String pace;
    
    /**
     * 记录日期
     */
    private LocalDate recordDate;
    
    // Getters and Setters
    
    public BigDecimal getDistance() {
        return distance;
    }
    
    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public String getPace() {
        return pace;
    }
    
    public void setPace(String pace) {
        this.pace = pace;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
} 