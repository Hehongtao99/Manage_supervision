package com.example.auth.dto;

import java.io.Serializable;

/**
 * 学生课程的数据传输对象
 */
public class CourseDTO implements Serializable {
    private String name; // 课程名称
    private String status; // 课程状态: 进行中, 已完成, 未开始
    private String description; // 课程描述
    private int completion; // 完成度(0-100)

    public CourseDTO() {
    }

    public CourseDTO(String name, String status, String description, int completion) {
        this.name = name;
        this.status = status;
        this.description = description;
        this.completion = completion;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCompletion() {
        return completion;
    }

    public void setCompletion(int completion) {
        this.completion = completion;
    }
} 