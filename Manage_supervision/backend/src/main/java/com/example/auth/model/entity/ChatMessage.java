package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("chat_messages")
public class ChatMessage {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("conversation_id")
    private Long conversationId;

    @TableField("sender_id")
    private Long senderId;

    @TableField("recipient_id")
    private Long recipientId;

    @TableField("content")
    private String content;

    @TableField("sent_time")
    private LocalDateTime sentTime;

    @TableField("is_read")
    private boolean isRead;
    
    @TableField("file_url")
    private String fileUrl;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("file_type")
    private String fileType;
    
    @TableField("file_size")
    private Long fileSize;

    @TableField(exist = false)
    private Conversation conversation;

    @TableField(exist = false)
    private User sender;

    @TableField(exist = false)
    private User recipient;

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

    public Long getRecipientId() {
        return recipientId;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
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

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
        if (conversation != null) {
            this.conversationId = conversation.getId();
        }
    }

    public void setSender(User sender) {
        this.sender = sender;
        if (sender != null) {
            this.senderId = sender.getId();
        }
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
        if (recipient != null) {
            this.recipientId = recipient.getId();
        }
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