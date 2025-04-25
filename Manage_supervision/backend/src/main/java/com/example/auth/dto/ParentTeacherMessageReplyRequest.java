package com.example.auth.dto;

public class ParentTeacherMessageReplyRequest {
    private Long messageId;
    private String replyContent;
    
    public ParentTeacherMessageReplyRequest() {
    }
    
    // Getters and Setters
    
    public Long getMessageId() {
        return messageId;
    }
    
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
    
    public String getReplyContent() {
        return replyContent;
    }
    
    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }
} 