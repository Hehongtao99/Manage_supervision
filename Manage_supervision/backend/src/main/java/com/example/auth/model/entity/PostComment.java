package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 朋友圈帖子评论实体类
 */
@Data
@TableName("social_post_comments")
public class PostComment {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;
    
    /**
     * 评论用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 评论内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 父评论ID，如果是一级评论则为0
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 被回复的用户ID，如果是一级评论则为0
     */
    @TableField("reply_user_id")
    private Long replyUserId;
    
    /**
     * 点赞数量
     */
    @TableField("like_count")
    private Integer likeCount;
    
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