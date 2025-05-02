package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 朋友圈帖子实体类
 */
@Data
@TableName("social_posts")
public class Post {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 发布者用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 帖子内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 帖子可见范围：0-全部可见，1-仅好友可见
     */
    @TableField("visibility")
    private Integer visibility;
    
    /**
     * 位置信息
     */
    @TableField("location")
    private String location;
    
    /**
     * 点赞数量
     */
    @TableField("like_count")
    private Integer likeCount;
    
    /**
     * 评论数量
     */
    @TableField("comment_count")
    private Integer commentCount;
    
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