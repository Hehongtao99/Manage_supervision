package com.example.auth.dto;

public class NotificationRequest {
    private String title;
    private String content;
    private String recipientType; // "ALL" or "CLASS"
    private Long classId; // 可选，仅在recipientType为"CLASS"时使用

    // Getters
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public Long getClassId() {
        return classId;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }
} 