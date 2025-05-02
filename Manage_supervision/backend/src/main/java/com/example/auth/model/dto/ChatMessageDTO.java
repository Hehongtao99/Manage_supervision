package com.example.auth.model.dto;

import com.example.auth.model.entity.ChatMessage;
import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long recipientId;
    private String recipientName;
    private String recipientAvatar;
    private String content;
    private LocalDateTime sentTime;
    private boolean isRead;
    // 文件相关字段
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.conversationId = message.getConversationId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getRealName() != null ? 
                message.getSender().getRealName() : message.getSender().getUsername();
        this.senderAvatar = message.getSender().getAvatar();
        this.recipientId = message.getRecipient().getId();
        this.recipientName = message.getRecipient().getRealName() != null ? 
                message.getRecipient().getRealName() : message.getRecipient().getUsername();
        this.recipientAvatar = message.getRecipient().getAvatar();
        this.content = message.getContent();
        this.sentTime = message.getSentTime();
        this.isRead = message.isRead();
        this.fileUrl = message.getFileUrl();
        this.fileName = message.getFileName();
        this.fileType = message.getFileType();
        this.fileSize = message.getFileSize();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getConversationId() {
        return conversationId;
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

    public Long getRecipientId() {
        return recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientAvatar() {
        return recipientAvatar;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }

    public boolean isRead() {
        return isRead;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
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

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setRecipientAvatar(String recipientAvatar) {
        this.recipientAvatar = recipientAvatar;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
} 