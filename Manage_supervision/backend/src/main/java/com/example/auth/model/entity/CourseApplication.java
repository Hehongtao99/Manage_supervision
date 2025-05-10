package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师申请课程实体类
 */
@Data
@TableName("course_applications")
public class CourseApplication {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("title")
    private String title;

    @TableField("subject")
    private String subject;

    @TableField("hourly_price")
    private BigDecimal hourlyPrice;

    @TableField("description")
    private String description;

    @TableField("image_paths")
    private String imagePaths;

    @TableField("work_time_start")
    private Integer workTimeStart;

    @TableField("work_time_end")
    private Integer workTimeEnd;

    @TableField("status")
    private String status; // PENDING, APPROVED, REJECTED

    @TableField("rejection_reason")
    private String rejectionReason;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // 非数据库字段，用于查询展示
    @TableField(exist = false)
    private String teacherName;
    
    // 非数据库字段，用于存储解析后的图片路径列表
    @TableField(exist = false)
    private List<String> imagePathList;
} 