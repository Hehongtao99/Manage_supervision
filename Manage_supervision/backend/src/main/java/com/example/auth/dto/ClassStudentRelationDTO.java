package com.example.auth.dto;

import com.example.auth.entity.ClassStudentRelation;
import java.time.LocalDateTime;

public class ClassStudentRelationDTO {
    private Long id;
    private Long classId;
    private String className;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private LocalDateTime assignTime;
    private String status;

    // 无参构造函数
    public ClassStudentRelationDTO() {
    }

    // 从实体类创建DTO的构造函数
    public ClassStudentRelationDTO(ClassStudentRelation relation) {
        this.id = relation.getId();
        this.classId = relation.getClassEntity().getId();
        this.className = relation.getClassEntity().getClassName();
        this.studentId = relation.getStudent().getId();
        this.studentName = relation.getStudent().getRealName() != null ? 
                relation.getStudent().getRealName() : relation.getStudent().getUsername();
        this.studentNumber = relation.getStudent().getUserNumber();
        this.assignTime = relation.getAssignTime();
        this.status = relation.getStatus();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentNumber() {
        return studentNumber;
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

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 