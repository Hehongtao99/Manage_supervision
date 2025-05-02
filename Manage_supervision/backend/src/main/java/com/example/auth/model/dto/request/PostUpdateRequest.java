package com.example.auth.model.dto.request;

import lombok.Data;
import java.util.List;

/**
 * 更新朋友圈帖子请求DTO
 */
@Data
public class PostUpdateRequest {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 图片URL列表，最多支持6张图片
     */
    private List<String> imageUrls;
    
    /**
     * 可见范围：0-全部可见，1-仅好友可见
     * 默认为仅好友可见，该值不可更改
     */
    private Integer visibility = 1;
    
    /**
     * 关联的跑步记录ID，如果为null则表示不关联任何跑步记录
     * 如果更新时值为null，表示移除现有的关联
     */
    private Long runningRecordId;
} 