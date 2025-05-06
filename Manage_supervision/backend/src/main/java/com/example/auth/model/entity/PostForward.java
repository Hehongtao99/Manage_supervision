package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 朋友圈帖子转发实体类
 */
@Data
@TableName("social_post_forwards")
public class PostForward {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 转发者用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 原始帖子ID
     */
    @TableField("original_post_id")
    private Long originalPostId;
    
    /**
     * 新帖子ID
     */
    @TableField("new_post_id")
    private Long newPostId;
    
    /**
     * 转发评论内容
     */
    @TableField("forward_comment")
    private String forwardComment;
    
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
    
    /**
     * 是否删除，0-未删除，1-已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;
} 