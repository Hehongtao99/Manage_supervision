package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_student_relations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "student_id"}))
public class ClassStudentRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

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

    public User getStudent() {
        return student;
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

    public void setStudent(User student) {
        this.student = student;
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 