package com.example.auth.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String recipientType;
    private Long classId;
    private String className;
    private LocalDateTime createTime;
    private Boolean isGlobal;

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public Boolean getIsGlobal() {
        return isGlobal;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public void setIsGlobal(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }
} 