package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications")
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Notification getNotification() {
        return notification;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
} 