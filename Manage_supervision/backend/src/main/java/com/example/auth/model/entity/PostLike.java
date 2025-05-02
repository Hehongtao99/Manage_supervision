package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 朋友圈帖子点赞实体类
 */
@Data
@TableName("social_post_likes")
public class PostLike {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;
    
    /**
     * 点赞用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
} 