package com.example.auth.dto;

import com.example.auth.entity.ClassTeacherRelation;
import java.time.LocalDateTime;

public class ClassTeacherRelationDTO {
    private Long id;
    private Long classId;
    private String className;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime assignTime;
    private String status;

    // 无参构造函数
    public ClassTeacherRelationDTO() {
    }

    // 从实体类创建DTO的构造函数
    public ClassTeacherRelationDTO(ClassTeacherRelation relation) {
        this.id = relation.getId();
        this.classId = relation.getClassEntity().getId();
        this.className = relation.getClassEntity().getClassName();
        this.teacherId = relation.getTeacher().getId();
        this.teacherName = relation.getTeacher().getRealName();
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

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
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

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 