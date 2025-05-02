package com.example.auth.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 朋友圈评论响应DTO
 */
@Data
public class CommentResponse {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 评论用户名
     */
    private String username;
    
    /**
     * 评论用户头像
     */
    private String avatar;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论ID
     */
    private Long parentId;
    
    /**
     * 被回复的用户ID
     */
    private Long replyUserId;
    
    /**
     * 被回复的用户名
     */
    private String replyUsername;
    
    /**
     * 点赞数量
     */
    private Integer likeCount;
    
    /**
     * 当前用户是否点赞
     */
    private Boolean liked;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 子评论列表
     */
    private List<CommentResponse> children;
} 