package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_class_history")
public class StudentClassHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @Column(name = "operation_type", nullable = false)
    private String operationType;  // join, leave

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @ManyToOne
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    @Column(name = "remark")
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (operationTime == null) {
            operationTime = LocalDateTime.now();
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getStudent() {
        return student;
    }

    public Class getClassEntity() {
        return classEntity;
    }

    public String getOperationType() {
        return operationType;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public User getOperator() {
        return operator;
    }

    public String getRemark() {
        return remark;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public void setClassEntity(Class classEntity) {
        this.classEntity = classEntity;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
} 