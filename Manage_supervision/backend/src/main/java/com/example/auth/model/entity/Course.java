package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("courses")
public class Course {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("course_name")
    private String courseName;

    @TableField("course_description")
    private String courseDescription;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("teacher_name")
    private String teacherName;

    @TableField("course_duration")
    private Integer courseDuration; // 课程时长（小时）

    @TableField("course_category")
    private String courseCategory;

    @TableField("status")
    private String status; // active, inactive

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;
} 