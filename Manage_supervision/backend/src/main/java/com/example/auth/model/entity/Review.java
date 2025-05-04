package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务评价实体类
 */
@Data
@TableName("reviews")
public class Review {
    
    // 评价ID
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 关联的订单ID
    private Long orderId;
    
    // 评价人ID（玩家ID）
    private Long reviewerId;
    
    // 被评价人ID（陪玩ID）
    private Long companionId;
    
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
    
    // 审核状态：pending-待审核，approved-已通过，rejected-已拒绝
    private String reviewStatus;
    
    // 审核时间
    private LocalDateTime reviewTime;
    
    // 审核意见
    private String reviewComment;
    
    // 审核管理员ID
    private Long reviewerAdminId;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 