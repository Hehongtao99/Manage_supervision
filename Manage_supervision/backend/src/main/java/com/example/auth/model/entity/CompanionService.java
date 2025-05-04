package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@TableName("companion_services")
public class CompanionService {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("companion_id")
    private Long companionId;

    private String title;
    
    private String description;
    
    @TableField("game_types")
    private String gameTypes;
    
    private BigDecimal price;
    
    @TableField("service_start_time")
    private LocalTime serviceStartTime;
    
    @TableField("service_end_time")
    private LocalTime serviceEndTime;
    
    private String availability;
    
    private String status;
    
    @TableField("created_time")
    private LocalDateTime createdTime;
    
    @TableField("updated_time")
    private LocalDateTime updatedTime;
    
    @TableField("review_status")
    private String reviewStatus;
    
    @TableField("review_time")
    private LocalDateTime reviewTime;
    
    @TableField("review_comment")
    private String reviewComment;
    
    @TableField("reviewer_id")
    private Long reviewerId;
    
    @TableField(exist = false)
    private User companion;
    
    @TableField(exist = false)
    private User reviewer;
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getCompanionId() {
        return companionId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getGameTypes() {
        return gameTypes;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public LocalTime getServiceStartTime() {
        return serviceStartTime;
    }
    
    public LocalTime getServiceEndTime() {
        return serviceEndTime;
    }
    
    public String getAvailability() {
        return availability;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
    
    public String getReviewStatus() {
        return reviewStatus;
    }
    
    public LocalDateTime getReviewTime() {
        return reviewTime;
    }
    
    public String getReviewComment() {
        return reviewComment;
    }
    
    public Long getReviewerId() {
        return reviewerId;
    }
    
    public User getCompanion() {
        return companion;
    }
    
    public User getReviewer() {
        return reviewer;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setCompanionId(Long companionId) {
        this.companionId = companionId;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setGameTypes(String gameTypes) {
        this.gameTypes = gameTypes;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public void setServiceStartTime(LocalTime serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }
    
    public void setServiceEndTime(LocalTime serviceEndTime) {
        this.serviceEndTime = serviceEndTime;
    }
    
    public void setAvailability(String availability) {
        this.availability = availability;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
    
    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }
    
    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
    
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }
    
    public void setCompanion(User companion) {
        this.companion = companion;
        if (companion != null) {
            this.companionId = companion.getId();
        }
    }
    
    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
        if (reviewer != null) {
            this.reviewerId = reviewer.getId();
        }
    }
} 