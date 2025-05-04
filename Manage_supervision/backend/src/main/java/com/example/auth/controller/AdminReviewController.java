package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.annotation.RequireRole;
import com.example.auth.mapper.ReviewMapper;
import com.example.auth.model.dto.ReviewDTO;
import com.example.auth.model.entity.Review;
import com.example.auth.model.entity.User;
import com.example.auth.service.ReviewService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    
    @Autowired
    private ReviewMapper reviewMapper;
    
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
        // 不指定状态时获取所有评价
        // 修改：调用getReviewsByStatus方法传入null来获取所有评论而不是只获取待审核的
        reviews = reviewService.getReviewsByStatus(status, page, size);
        
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

    /**
     * 添加测试评论数据（仅用于开发测试）
     */
    @GetMapping("/test-data")
    @RequireRole("ADMIN")
    public ResponseEntity<?> addTestData() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        try {
            // 创建一个待审核评论
            Review pendingReview = new Review();
            pendingReview.setOrderId(1L); // 假设存在ID为1的订单
            pendingReview.setReviewerId(2L); // 假设存在ID为2的玩家
            pendingReview.setCompanionId(3L); // 假设存在ID为3的陪玩
            pendingReview.setRating(5);
            pendingReview.setContent("这是一条待审核的测试评论");
            pendingReview.setAnonymous(false);
            pendingReview.setReplied(false);
            pendingReview.setReviewStatus("pending");
            pendingReview.setCreateTime(LocalDateTime.now());
            pendingReview.setUpdateTime(LocalDateTime.now());
            reviewMapper.insert(pendingReview);
            
            // 创建一个已通过的评论
            Review approvedReview = new Review();
            approvedReview.setOrderId(1L);
            approvedReview.setReviewerId(2L);
            approvedReview.setCompanionId(3L);
            approvedReview.setRating(4);
            approvedReview.setContent("这是一条已通过的测试评论");
            approvedReview.setAnonymous(false);
            approvedReview.setReplied(false);
            approvedReview.setReviewStatus("approved");
            approvedReview.setReviewTime(LocalDateTime.now());
            approvedReview.setReviewerAdminId(currentUser.getId());
            approvedReview.setCreateTime(LocalDateTime.now());
            approvedReview.setUpdateTime(LocalDateTime.now());
            reviewMapper.insert(approvedReview);
            
            // 创建一个已拒绝的评论
            Review rejectedReview = new Review();
            rejectedReview.setOrderId(1L);
            rejectedReview.setReviewerId(2L);
            rejectedReview.setCompanionId(3L);
            rejectedReview.setRating(3);
            rejectedReview.setContent("这是一条已拒绝的测试评论");
            rejectedReview.setAnonymous(false);
            rejectedReview.setReplied(false);
            rejectedReview.setReviewStatus("rejected");
            rejectedReview.setReviewComment("测试拒绝理由");
            rejectedReview.setReviewTime(LocalDateTime.now());
            rejectedReview.setReviewerAdminId(currentUser.getId());
            rejectedReview.setCreateTime(LocalDateTime.now());
            rejectedReview.setUpdateTime(LocalDateTime.now());
            reviewMapper.insert(rejectedReview);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已添加3条测试评论数据");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加测试数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 