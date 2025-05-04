package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.ReviewMapper;
import com.example.auth.model.dto.CreateReviewRequest;
import com.example.auth.model.dto.ReviewDTO;
import com.example.auth.model.dto.ReplyReviewRequest;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.Review;
import com.example.auth.model.entity.User;
import com.example.auth.model.enums.OrderStatus;
import com.example.auth.service.CompanionServiceService;
import com.example.auth.service.OrderService;
import com.example.auth.service.ReviewService;
import com.example.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Autowired
    private ReviewMapper reviewMapper;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CompanionServiceService companionServiceService;

    @Override
    @Transactional
    public Long createReview(CreateReviewRequest request, Long playerId) {
        // 验证订单是否可以评价
        if (!canReviewOrder(request.getOrderId(), playerId)) {
            throw new RuntimeException("订单不存在或状态不正确，无法评价");
        }
        
        // 查询订单信息
        Order order = orderService.getOrderEntity(request.getOrderId());
        
        // 检查是否已经评价过
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getOrderId, request.getOrderId());
        if (reviewMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("该订单已评价过");
        }
        
        // 创建评价
        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setReviewerId(playerId);
        review.setCompanionId(order.getCompanionId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setAnonymous(request.getAnonymous());
        review.setReplied(false);
        // 设置审核状态为待审核
        review.setReviewStatus("pending");
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        
        reviewMapper.insert(review);
        
        return review.getId();
    }

    @Override
    @Transactional
    public boolean replyReview(ReplyReviewRequest request, Long companionId) {
        Review review = reviewMapper.selectById(request.getReviewId());
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        if (!Objects.equals(review.getCompanionId(), companionId)) {
            throw new RuntimeException("只能回复自己收到的评价");
        }
        
        if (review.getReplied()) {
            throw new RuntimeException("该评价已回复，不能重复回复");
        }
        
        // 只有审核通过的评价才能回复
        if (!"approved".equals(review.getReviewStatus())) {
            throw new RuntimeException("该评价尚未审核通过，暂时无法回复");
        }
        
        review.setReply(request.getReply());
        review.setReplied(true);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        
        return reviewMapper.updateById(review) > 0;
    }
    
    @Override
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            return null;
        }
        
        return convertToDTO(review);
    }
    
    @Override
    public Page<ReviewDTO> getCompanionReviews(Long companionId, int page, int size) {
        // 只显示已审核通过的评价
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getCompanionId, companionId)
                   .eq(Review::getReviewStatus, "approved")
                   .orderByDesc(Review::getCreateTime);
        
        Page<Review> reviewPage = new Page<>(page, size);
        reviewMapper.selectPage(reviewPage, queryWrapper);
        
        return convertToPageDTO(reviewPage);
    }
    
    @Override
    public Page<ReviewDTO> getPlayerReviews(Long playerId, int page, int size) {
        // 玩家可以查看自己发布的所有评价，包括审核中和被拒绝的
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getReviewerId, playerId)
                   .orderByDesc(Review::getCreateTime);
        
        Page<Review> reviewPage = new Page<>(page, size);
        reviewMapper.selectPage(reviewPage, queryWrapper);
        
        return convertToPageDTO(reviewPage);
    }
    
    @Override
    public boolean canReviewOrder(Long orderId, Long playerId) {
        Order order = orderService.getOrderEntity(orderId);
        
        if (order == null) {
            return false;
        }
        
        // 检查订单是否属于该玩家
        if (!Objects.equals(order.getPlayerId(), playerId)) {
            return false;
        }
        
        // 检查订单状态是否为已完成
        return OrderStatus.COMPLETED.name().equals(order.getStatus());
    }
    
    @Override
    public double getCompanionAverageRating(Long companionId) {
        // 只计算已审核通过的评价的平均分
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getCompanionId, companionId)
                   .eq(Review::getReviewStatus, "approved")
                   .select(Review::getRating);
        
        List<Review> reviews = reviewMapper.selectList(queryWrapper);
        
        if (reviews.isEmpty()) {
            return 0.0;
        }
        
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }
    
    @Override
    @Transactional
    public boolean approveReview(Long reviewId, Long adminId) {
        logger.info("开始审核通过评价，评价ID: {}, 管理员ID: {}", reviewId, adminId);
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            logger.error("评价不存在，评价ID: {}", reviewId);
            throw new RuntimeException("评价不存在");
        }
        
        if (!"pending".equals(review.getReviewStatus())) {
            logger.warn("评价已审核，不能重复审核，评价ID: {}, 当前状态: {}", reviewId, review.getReviewStatus());
            throw new RuntimeException("该评价已审核，不能重复审核");
        }
        
        review.setReviewStatus("approved");
        review.setReviewTime(LocalDateTime.now());
        review.setReviewerAdminId(adminId);
        review.setUpdateTime(LocalDateTime.now());
        
        int result = reviewMapper.updateById(review);
        logger.info("审核通过评价完成，评价ID: {}, 更新结果: {}", reviewId, result > 0 ? "成功" : "失败");
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean rejectReview(Long reviewId, String comment, Long adminId) {
        logger.info("开始拒绝评价，评价ID: {}, 管理员ID: {}, 拒绝理由: {}", reviewId, adminId, comment);
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            logger.error("评价不存在，评价ID: {}", reviewId);
            throw new RuntimeException("评价不存在");
        }
        
        if (!"pending".equals(review.getReviewStatus())) {
            logger.warn("评价已审核，不能重复审核，评价ID: {}, 当前状态: {}", reviewId, review.getReviewStatus());
            throw new RuntimeException("该评价已审核，不能重复审核");
        }
        
        review.setReviewStatus("rejected");
        review.setReviewComment(comment);
        review.setReviewTime(LocalDateTime.now());
        review.setReviewerAdminId(adminId);
        review.setUpdateTime(LocalDateTime.now());
        
        int result = reviewMapper.updateById(review);
        logger.info("拒绝评价完成，评价ID: {}, 更新结果: {}", reviewId, result > 0 ? "成功" : "失败");
        return result > 0;
    }
    
    @Override
    public Page<ReviewDTO> getPendingReviews(int page, int size) {
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getReviewStatus, "pending")
                   .orderByDesc(Review::getCreateTime);
        
        Page<Review> reviewPage = new Page<>(page, size);
        reviewMapper.selectPage(reviewPage, queryWrapper);
        
        return convertToPageDTO(reviewPage);
    }
    
    @Override
    public Page<ReviewDTO> getReviewsByStatus(String status, int page, int size) {
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Review::getReviewStatus, status);
        }
        
        queryWrapper.orderByDesc(Review::getCreateTime);
        
        Page<Review> reviewPage = new Page<>(page, size);
        reviewMapper.selectPage(reviewPage, queryWrapper);
        
        return convertToPageDTO(reviewPage);
    }
    
    /**
     * 转换评价实体为DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        BeanUtils.copyProperties(review, dto);
        
        // 获取订单信息
        Order order = orderService.getOrderEntity(review.getOrderId());
        if (order != null) {
            dto.setOrderNumber(order.getOrderNumber());
            dto.setServiceId(order.getServiceId());
            dto.setGameType(order.getGameType());
        }
        
        // 获取评价人信息（玩家）
        User reviewer = userService.getUserById(review.getReviewerId());
        if (reviewer != null) {
            // 匿名评价不显示评价人信息
            if (review.getAnonymous()) {
                dto.setReviewerName("匿名用户");
                dto.setReviewerAvatar(null);
            } else {
                dto.setReviewerName(reviewer.getRealName() != null ? reviewer.getRealName() : reviewer.getUsername());
                dto.setReviewerAvatar(reviewer.getAvatar());
            }
        }
        
        // 获取被评价人信息（陪玩）
        User companion = userService.getUserById(review.getCompanionId());
        if (companion != null) {
            dto.setCompanionName(companion.getRealName() != null ? companion.getRealName() : companion.getUsername());
        }
        
        // 获取审核管理员信息
        if (review.getReviewerAdminId() != null) {
            User admin = userService.getUserById(review.getReviewerAdminId());
            if (admin != null) {
                dto.setReviewerAdminName(admin.getRealName() != null ? admin.getRealName() : admin.getUsername());
            }
        }
        
        return dto;
    }
    
    /**
     * 转换评价分页为DTO分页
     */
    private Page<ReviewDTO> convertToPageDTO(Page<Review> reviewPage) {
        Page<ReviewDTO> dtoPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        
        List<ReviewDTO> dtoList = new ArrayList<>();
        for (Review review : reviewPage.getRecords()) {
            dtoList.add(convertToDTO(review));
        }
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
} 