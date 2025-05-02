package com.example.auth.model.dto.request;

import lombok.Data;

/**
 * 创建朋友圈评论请求DTO
 */
@Data
public class CommentCreateRequest {
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论ID，如果是一级评论则为0
     */
    private Long parentId = 0L;
    
    /**
     * 被回复的用户ID，如果是一级评论则为0
     */
    private Long replyUserId = 0L;
} 