package com.example.auth.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("majors")
public class Major {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("major_name")
    private String majorName;
    
    @TableField("major_code")
    private String majorCode;
    
    @TableField("college_id")
    private Long collegeId;
    
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
    
    // 关联字段，不映射到数据库
    @TableField(exist = false)
    private String collegeName;
} 