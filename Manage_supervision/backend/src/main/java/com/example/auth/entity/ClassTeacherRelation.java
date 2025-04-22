package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_teacher_relations")
public class ClassTeacherRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "assign_time")
    private LocalDateTime assignTime;

    @Column(name = "status")
    private String status;

    @PrePersist
    protected void onCreate() {
        assignTime = LocalDateTime.now();
        if (status == null) {
            status = "active";
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Class getClassEntity() {
        return classEntity;
    }

    public User getTeacher() {
        return teacher;
    }

    public LocalDateTime getAssignTime() {
        return assignTime;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setClassEntity(Class classEntity) {
        this.classEntity = classEntity;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 