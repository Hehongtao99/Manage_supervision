package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classes")
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "grade")
    private String grade;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "status")
    private String status;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        status = "active";
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public String getGrade() {
        return grade;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 