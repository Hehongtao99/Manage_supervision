package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.ReviewDTO;
import com.example.auth.model.entity.User;
import com.example.auth.service.ReviewService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员评价审核控制器
 */
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserContext userContext;
    
    /**
     * 获取待审核的评价列表
     */
    @GetMapping("/pending")
    @RequireRole("ADMIN")
    public ResponseEntity<Page<ReviewDTO>> getPendingReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<ReviewDTO> reviews = reviewService.getPendingReviews(page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * 按状态获取评价列表
     */
    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByStatus(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<ReviewDTO> reviews;
        if (status != null && !status.isEmpty()) {
            reviews = reviewService.getReviewsByStatus(status, page, size);
        } else {
            // 不指定状态时获取所有评价
            // 这里可以扩展实现全部评价的查询
            reviews = reviewService.getPendingReviews(page, size);
        }
        
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * 获取评价详情
     */
    @GetMapping("/{reviewId}")
    @RequireRole("ADMIN")
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
     * 审核评价
     */
    @PostMapping("/{reviewId}/review")
    @RequireRole("ADMIN")
    public ResponseEntity<?> reviewReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> reviewData) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        boolean approved = (boolean) reviewData.get("approved");
        String reason = (String) reviewData.getOrDefault("reason", "");
        
        boolean success;
        
        if (approved) {
            // 审核通过
            success = reviewService.approveReview(reviewId, currentUser.getId());
        } else {
            // 审核拒绝
            success = reviewService.rejectReview(reviewId, reason, currentUser.getId());
        }
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", approved ? "评价审核通过成功" : "评价审核拒绝成功");
        } else {
            response.put("success", false);
            response.put("message", "评价审核操作失败");
        }
        
        return ResponseEntity.ok(response);
    }
} 