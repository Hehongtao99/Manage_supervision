package com.example.auth.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("colleges")
public class College {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("college_name")
    private String collegeName;
    
    @TableField("college_code")
    private String collegeCode;
    
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