package com.example.auth.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程数据传输对象
 */
@Data
public class CourseDTO implements Serializable {
    private Long id;
    private String courseName;
    private String courseDescription;
    private Long teacherId;
    private String teacherName;
    private Integer courseDuration; // 课程时长（小时）
    private String courseCategory;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createdBy;
    private Long updatedBy;
    
    // 兼容旧版本字段
    private String name; // 课程名称
    private String description; // 课程描述
    private int completion; // 完成度(0-100)
    private int credits;
    private int score;

    public CourseDTO() {
    }

    public CourseDTO(String name, String status, String description, int completion) {
        this.name = name;
        this.status = status;
        this.description = description;
        this.completion = completion;
    }
} 