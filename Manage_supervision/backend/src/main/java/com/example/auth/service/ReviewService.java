package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.CreateReviewRequest;
import com.example.auth.model.dto.ReviewDTO;
import com.example.auth.model.dto.ReplyReviewRequest;

/**
 * 评价服务接口
 */
public interface ReviewService {
    
    /**
     * 创建评价
     * @param request 评价请求
     * @param playerId 评价人ID（玩家ID）
     * @return 评价ID
     */
    Long createReview(CreateReviewRequest request, Long playerId);
    
    /**
     * 回复评价
     * @param request 回复请求
     * @param companionId 回复人ID（陪玩ID）
     * @return 是否成功
     */
    boolean replyReview(ReplyReviewRequest request, Long companionId);
    
    /**
     * 根据ID获取评价详情
     * @param reviewId 评价ID
     * @return 评价详情
     */
    ReviewDTO getReviewById(Long reviewId);
    
    /**
     * 获取陪玩的评价列表
     * @param companionId 陪玩ID
     * @param page 页码
     * @param size 每页数量
     * @return 评价分页列表
     */
    Page<ReviewDTO> getCompanionReviews(Long companionId, int page, int size);
    
    /**
     * 获取玩家发布的评价列表
     * @param playerId 玩家ID
     * @param page 页码
     * @param size 每页数量
     * @return 评价分页列表
     */
    Page<ReviewDTO> getPlayerReviews(Long playerId, int page, int size);
    
    /**
     * 检查订单是否可以评价
     * @param orderId 订单ID
     * @param playerId 玩家ID
     * @return 是否可以评价
     */
    boolean canReviewOrder(Long orderId, Long playerId);
    
    /**
     * 获取陪玩的平均评分
     * @param companionId 陪玩ID
     * @return 平均评分
     */
    double getCompanionAverageRating(Long companionId);
} 