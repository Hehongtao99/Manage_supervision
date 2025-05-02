package com.example.auth.model.dto.request;

/**
 * 跑步偏好请求DTO
 */
public class RunningPreferenceRequest {
    
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
    
    // Getters and Setters
    
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
} 