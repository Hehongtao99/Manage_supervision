package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 朋友圈帖子图片实体类
 */
@Data
@TableName("social_post_images")
public class PostImage {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;
    
    /**
     * 图片URL
     */
    @TableField("image_url")
    private String imageUrl;
    
    /**
     * 图片排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
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