package com.example.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课表数据传输对象
 */
@Data
public class TimetableDTO {
    private Long id;
    private Long classId;
    private String className;
    private Integer weekNumber;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
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

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
} 