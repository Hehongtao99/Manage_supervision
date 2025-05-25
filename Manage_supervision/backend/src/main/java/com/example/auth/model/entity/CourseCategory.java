package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("course_categories")
public class CourseCategory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("category_name")
    private String categoryName;
    
    @TableField("description")
    private String description;
    
    @TableField("status")
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField("created_by")
    private Long createdBy;
    
    @TableField("updated_by")
    private Long updatedBy;
} 