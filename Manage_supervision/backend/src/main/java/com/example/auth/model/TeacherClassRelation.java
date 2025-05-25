package com.example.auth.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("teacher_class_relations")
public class TeacherClassRelation {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("teacher_id")
    private Long teacherId;
    
    @TableField("class_id")
    private Long classId;
    
    @TableField("assign_time")
    private LocalDateTime assignTime;
    
    @TableField("status")
    private String status;
    
    @TableField("created_by")
    private Long createdBy;
    
    // 关联字段，不映射到数据库
    @TableField(exist = false)
    private String teacherName;
    
    @TableField(exist = false)
    private String className;
    
    @TableField(exist = false)
    private String collegeName;
    
    @TableField(exist = false)
    private String majorName;
} 