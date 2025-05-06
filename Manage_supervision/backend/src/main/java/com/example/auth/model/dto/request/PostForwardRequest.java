package com.example.auth.model.dto.request;

import lombok.Data;
import java.util.List;

/**
 * 朋友圈转发请求DTO
 */
@Data
public class PostForwardRequest {
    
    /**
     * 原始帖子ID
     */
    private Long originalPostId;
    
    /**
     * 转发评论内容
     */
    private String forwardComment;
    
    /**
     * 可见范围：0-全部可见，1-仅好友可见
     */
    private Integer visibility = 0;
    
    /**
     * 位置信息
     */
    private String location;
} 