package com.example.auth.model.dto.response;

import java.time.LocalDateTime;

/**
 * 跑步偏好响应DTO
 */
public class RunningPreferenceResponse {
    
    private Long id;
    
    private Long userId;
    
    /**
     * 跑步频率
     */
    private String frequency;
    
    /**
     * 偏好跑步距离
     */
    private String preferredDistance;
    
    /**
     * 跑步配速
     */
    private String pace;
    
    /**
     * 偏好跑步环境
     */
    private String environment;
    
    /**
     * 个人跑步宣言
     */
    private String motto;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    public String getPreferredDistance() {
        return preferredDistance;
    }
    
    public void setPreferredDistance(String preferredDistance) {
        this.preferredDistance = preferredDistance;
    }
    
    public String getPace() {
        return pace;
    }
    
    public void setPace(String pace) {
        this.pace = pace;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public String getMotto() {
        return motto;
    }
    
    public void setMotto(String motto) {
        this.motto = motto;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
} 