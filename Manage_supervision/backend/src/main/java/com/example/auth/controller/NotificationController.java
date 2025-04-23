package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.NotificationRequest;
import com.example.auth.dto.NotificationResponse;
import com.example.auth.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 教师创建通知
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestBody NotificationRequest request) {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "用户未认证", null));
            }
            NotificationResponse notification = notificationService.createNotification(userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "通知创建成功", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "创建通知失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取当前用户收到的所有通知
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getReceivedNotifications(
            @RequestHeader(value = "userId", required = false) Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "用户未认证", null));
            }
            List<NotificationResponse> notifications = notificationService.getNotificationsForUser(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取通知成功", notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "获取通知失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取当前用户发送的所有通知（教师）
     */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getSentNotifications(
            @RequestHeader(value = "userId", required = false) Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "用户未认证", null));
            }
            List<NotificationResponse> notifications = notificationService.getSentNotifications(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取已发送通知成功", notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "获取已发送通知失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取所有通知（管理员）
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications() {
        try {
            List<NotificationResponse> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(new ApiResponse<>(true, "获取所有通知成功", notifications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "获取所有通知失败: " + e.getMessage(), null));
        }
    }
} 