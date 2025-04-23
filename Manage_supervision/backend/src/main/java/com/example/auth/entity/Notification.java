package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "recipient_type", nullable = false)
    private String recipientType; // "ALL" or "CLASS"

    @ManyToOne
    @JoinColumn(name = "class_id")
    private Class classEntity; // 只在recipientType为"CLASS"时使用

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "status", nullable = false)
    private String status; // "active" or "inactive"
    
    @Column(name = "is_global", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isGlobal = false;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = "active";
        }
        if (isGlobal == null) {
            isGlobal = false;
        }
    }

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

    public User getSender() {
        return sender;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public Class getClassEntity() {
        return classEntity;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public String getStatus() {
        return status;
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

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public void setClassEntity(Class classEntity) {
        this.classEntity = classEntity;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setIsGlobal(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }
} 