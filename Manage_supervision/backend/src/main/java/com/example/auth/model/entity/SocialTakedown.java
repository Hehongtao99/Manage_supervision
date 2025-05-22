package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社交内容下架记录实体类
 */
@Data
@TableName("social_takedowns")
public class SocialTakedown {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 内容类型：1-帖子，2-评论
     */
    @TableField("content_type")
    private Integer contentType;
    
    /**
     * 内容ID（帖子ID或评论ID）
     */
    @TableField("content_id")
    private Long contentId;
    
    /**
     * 操作管理员ID
     */
    @TableField("admin_id")
    private Long adminId;
    
    /**
     * 下架原因
     */
    @TableField("reason")
    private String reason;
    
    /**
     * 下架时间
     */
    @TableField("takedown_time")
    private LocalDateTime takedownTime;
    
    /**
     * 恢复时间，为null表示未恢复
     */
    @TableField("restore_time")
    private LocalDateTime restoreTime;
    
    /**
     * 恢复操作管理员ID，为null表示未恢复
     */
    @TableField("restore_admin_id")
    private Long restoreAdminId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
} 