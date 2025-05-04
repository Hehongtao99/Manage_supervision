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
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        
        reviewMapper.insert(review);
        
        return review.getId();
    }

    @Override
    @Transactional
    public boolean replyReview(ReplyReviewRequest request, Long companionId) {
        // 查询评价
        Review review = reviewMapper.selectById(request.getReviewId());
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        // 验证陪玩身份
        if (!review.getCompanionId().equals(companionId)) {
            throw new RuntimeException("您不是该评价的陪玩");
        }
        
        // 更新评价回复
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
        // 创建查询条件
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getCompanionId, companionId);
        queryWrapper.orderByDesc(Review::getCreateTime);
        
        // 执行分页查询
        Page<Review> reviewPage = new Page<>(page, size);
        Page<Review> resultPage = reviewMapper.selectPage(reviewPage, queryWrapper);
        
        // 转换结果为DTO
        List<ReviewDTO> reviewDTOList = new ArrayList<>();
        for (Review review : resultPage.getRecords()) {
            reviewDTOList.add(convertToDTO(review));
        }
        
        // 创建返回结果
        Page<ReviewDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        dtoPage.setRecords(reviewDTOList);
        
        return dtoPage;
    }

    @Override
    public Page<ReviewDTO> getPlayerReviews(Long playerId, int page, int size) {
        // 创建查询条件
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getReviewerId, playerId);
        queryWrapper.orderByDesc(Review::getCreateTime);
        
        // 执行分页查询
        Page<Review> reviewPage = new Page<>(page, size);
        Page<Review> resultPage = reviewMapper.selectPage(reviewPage, queryWrapper);
        
        // 转换结果为DTO
        List<ReviewDTO> reviewDTOList = new ArrayList<>();
        for (Review review : resultPage.getRecords()) {
            reviewDTOList.add(convertToDTO(review));
        }
        
        // 创建返回结果
        Page<ReviewDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        dtoPage.setRecords(reviewDTOList);
        
        return dtoPage;
    }

    @Override
    public boolean canReviewOrder(Long orderId, Long playerId) {
        // 获取订单信息
        Order order = orderService.getOrderEntity(orderId);
        if (order == null) {
            return false;
        }
        
        // 验证玩家身份
        if (!order.getPlayerId().equals(playerId)) {
            return false;
        }
        
        // 验证订单状态，只有已完成的订单才可以评价
        return OrderStatus.COMPLETED.name().equals(order.getStatus());
    }

    @Override
    public double getCompanionAverageRating(Long companionId) {
        // 创建查询条件
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getCompanionId, companionId);
        queryWrapper.select(Review::getRating);
        
        // 查询所有评分
        List<Review> reviews = reviewMapper.selectList(queryWrapper);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        
        // 计算平均分
        double totalRating = reviews.stream()
                .mapToInt(Review::getRating)
                .sum();
        
        return totalRating / reviews.size();
    }
    
    /**
     * 将Review实体转换为ReviewDTO
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
            
            // 设置服务标题
            dto.setServiceTitle(companionServiceService.getServiceById(order.getServiceId()).getTitle());
        }
        
        // 获取玩家信息
        User player = userService.getUserById(review.getReviewerId());
        if (player != null && (!review.getAnonymous() || Objects.equals(player.getId(), review.getReviewerId()))) {
            dto.setReviewerName(player.getUsername());
        } else {
            dto.setReviewerName("匿名用户");
        }
        
        // 获取陪玩信息
        User companion = userService.getUserById(review.getCompanionId());
        if (companion != null) {
            dto.setCompanionName(companion.getUsername());
        }
        
        return dto;
    }
} 