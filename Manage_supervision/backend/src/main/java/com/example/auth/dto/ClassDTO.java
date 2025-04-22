package com.example.auth.dto;

import com.example.auth.entity.Class;
import java.time.LocalDateTime;

public class ClassDTO {
    private Long id;
    private String className;
    private String grade;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer studentCount;

    // 无参构造函数
    public ClassDTO() {
        this.studentCount = 0;
    }

    // 从实体类创建DTO的构造函数
    public ClassDTO(Class classEntity) {
        this.id = classEntity.getId();
        this.className = classEntity.getClassName();
        this.grade = classEntity.getGrade();
        this.description = classEntity.getDescription();
        this.createTime = classEntity.getCreateTime();
        this.updateTime = classEntity.getUpdateTime();
        this.studentCount = 0;
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

    public Integer getStudentCount() {
        return studentCount;
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

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    // 转换为实体类的方法
    public Class toEntity() {
        Class classEntity = new Class();
        classEntity.setId(this.id);
        classEntity.setClassName(this.className);
        classEntity.setGrade(this.grade);
        classEntity.setDescription(this.description);
        return classEntity;
    }
} 