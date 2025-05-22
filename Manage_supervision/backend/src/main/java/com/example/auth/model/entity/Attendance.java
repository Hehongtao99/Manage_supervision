package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("e981_attendance")
public class Attendance {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;
    
    private String description;
    
    @TableField("start_time")
    private LocalDateTime startTime;
    
    @TableField("end_time")
    private LocalDateTime endTime;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("creator_id")
    private Long creatorId;
    
    private String status;

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 