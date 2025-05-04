package com.example.auth.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价数据传输对象
 */
@Data
public class ReviewDTO {
    
    // 评价ID
    private Long id;
    
    // 关联的订单ID
    private Long orderId;
    
    // 订单编号
    private String orderNumber;
    
    // 评价人ID（玩家ID）
    private Long reviewerId;
    
    // 评价人名称
    private String reviewerName;
    
    // 被评价人ID（陪玩ID）
    private Long companionId;
    
    // 被评价人名称
    private String companionName;
    
    // 服务ID
    private Long serviceId;
    
    // 服务标题
    private String serviceTitle;
    
    // 游戏类型
    private String gameType;
    
    // 评分（1-5星）
    private Integer rating;
    
    // 评价内容
    private String content;
    
    // 是否匿名
    private Boolean anonymous;
    
    // 是否已回复
    private Boolean replied;
    
    // 回复内容
    private String reply;
    
    // 回复时间
    private LocalDateTime replyTime;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 