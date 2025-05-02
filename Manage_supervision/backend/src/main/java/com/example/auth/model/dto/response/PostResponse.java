package com.example.auth.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 朋友圈帖子响应DTO
 */
@Data
public class PostResponse {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 发布者用户ID
     */
    private Long userId;
    
    /**
     * 发布者用户名
     */
    private String username;
    
    /**
     * 发布者头像URL
     */
    private String avatar;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 图片URL列表
     */
    private List<String> imageUrls;
    
    /**
     * 位置信息
     */
    private String location;
    
    /**
     * 点赞数量
     */
    private Integer likeCount;
    
    /**
     * 评论数量
     */
    private Integer commentCount;
    
    /**
     * 当前用户是否点赞
     */
    private Boolean liked;
    
    /**
     * 可见范围：0-全部可见，1-仅好友可见
     */
    private Integer visibility;
    
    /**
     * 关联的跑步记录
     */
    private RunningRecordResponse runningRecord;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 