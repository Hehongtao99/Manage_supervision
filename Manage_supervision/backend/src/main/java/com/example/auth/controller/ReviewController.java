package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.CreateReviewRequest;
import com.example.auth.model.dto.ReplyReviewRequest;
import com.example.auth.model.dto.ReviewDTO;
import com.example.auth.model.entity.User;
import com.example.auth.service.ReviewService;
import com.example.auth.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserContext userContext;
    
    /**
     * 创建评价
     */
    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody CreateReviewRequest request) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        try {
            // 校验是否可以评价
            if (!reviewService.canReviewOrder(request.getOrderId(), currentUser.getId())) {
                return ResponseEntity.status(400).body("订单不存在或状态不正确，无法评价");
            }
            
            Long reviewId = reviewService.createReview(request, currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviewId", reviewId);
            response.put("message", "评价成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 陪玩回复评价
     */
    @PostMapping("/reply")
    public ResponseEntity<?> replyReview(@Valid @RequestBody ReplyReviewRequest request) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 验证是否为陪玩
        boolean isCompanion = currentUser.getRoles().stream()
                .anyMatch(r -> "SUPERVISOR".equals(r.getName()));
        
        if (!isCompanion) {
            return ResponseEntity.status(403).body("您不是陪玩，无法回复评价");
        }
        
        try {
            boolean success = reviewService.replyReview(request, currentUser.getId());
            if (success) {
                return ResponseEntity.ok("回复成功");
            } else {
                return ResponseEntity.status(500).body("回复失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 获取评价详情
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> getReviewDetail(@PathVariable Long reviewId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        ReviewDTO review = reviewService.getReviewById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(review);
    }
    
    /**
     * 获取陪玩的评价列表
     */
    @GetMapping("/companion/{companionId}")
    public ResponseEntity<Page<ReviewDTO>> getCompanionReviews(
            @PathVariable Long companionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<ReviewDTO> reviews = reviewService.getCompanionReviews(companionId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * 获取我的评价列表（玩家发布的评价）
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<Page<ReviewDTO>> getMyReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<ReviewDTO> reviews = reviewService.getPlayerReviews(currentUser.getId(), page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * 获取陪玩的平均评分
     */
    @GetMapping("/rating/{companionId}")
    public ResponseEntity<Map<String, Object>> getCompanionRating(@PathVariable Long companionId) {
        double rating = reviewService.getCompanionAverageRating(companionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("companionId", companionId);
        response.put("rating", rating);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查订单是否可以评价
     */
    @GetMapping("/can-review/{orderId}")
    public ResponseEntity<Map<String, Object>> canReviewOrder(@PathVariable Long orderId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        boolean canReview = reviewService.canReviewOrder(orderId, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("canReview", canReview);
        
        return ResponseEntity.ok(response);
    }
} 