package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_student_relations")
public class TeacherStudentRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

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

    public User getTeacher() {
        return teacher;
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

    public void setTeacher(User teacher) {
        this.teacher = teacher;
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