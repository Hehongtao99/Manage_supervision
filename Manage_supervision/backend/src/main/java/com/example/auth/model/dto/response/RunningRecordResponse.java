package com.example.auth.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 跑步记录响应DTO
 */
public class RunningRecordResponse {
    
    private Long id;
    
    /**
     * 跑步距离，单位：公里
     */
    private BigDecimal distance;
    
    /**
     * 跑步时长，单位：秒
     */
    private Integer duration;
    
    /**
     * 配速，格式：分钟:秒/公里
     */
    private String pace;
    
    /**
     * 记录日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;
    
    /**
     * 记录时间（精确到秒）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordDateTime;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getRecordDateTime() {
        return recordDateTime;
    }
    
    public void setRecordDateTime(LocalDateTime recordDateTime) {
        this.recordDateTime = recordDateTime;
    }
    
    public String getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
} 