package com.example.auth.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CompanionServiceDTO {
    private Long id;
    private Long companionId;
    private String title;
    private String description;
    private String gameTypes;
    private BigDecimal price;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private String availability;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String companionName;
    private String companionAvatar;
    private String reviewStatus;
    private LocalDateTime reviewTime;
    private String reviewComment;
    private Long reviewerId;
    private String reviewerName;
    
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
    
    public String getCompanionName() {
        return companionName;
    }
    
    public String getCompanionAvatar() {
        return companionAvatar;
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
    
    public String getReviewerName() {
        return reviewerName;
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
    
    public void setCompanionName(String companionName) {
        this.companionName = companionName;
    }
    
    public void setCompanionAvatar(String companionAvatar) {
        this.companionAvatar = companionAvatar;
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
    
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }
} 