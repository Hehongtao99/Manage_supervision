package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 朋友圈帖子关联跑步记录实体类
 */
@Data
@TableName("social_post_running_records")
public class PostRunningRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;
    
    /**
     * 跑步记录ID
     */
    @TableField("running_record_id")
    private Long runningRecordId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
} 