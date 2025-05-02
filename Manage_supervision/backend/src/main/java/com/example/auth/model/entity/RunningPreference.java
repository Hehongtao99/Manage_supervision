package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 用户跑步偏好实体类
 */
@TableName("running_preferences")
public class RunningPreference {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    /**
     * 跑步频率 (例如: "每周3-5次", "每天", "每周1-2次")
     */
    private String frequency;
    
    /**
     * 偏好跑步距离 (例如: "5公里", "10公里", "半程马拉松")
     */
    @TableField("preferred_distance")
    private String preferredDistance;
    
    /**
     * 跑步配速 (例如: "5-6分钟/公里", "7-8分钟/公里")
     */
    private String pace;
    
    /**
     * 偏好跑步环境 (例如: "城市道路", "公园", "跑步机")
     */
    private String environment;
    
    /**
     * 个人跑步宣言
     */
    private String motto;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
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