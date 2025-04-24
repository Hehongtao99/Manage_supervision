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
    
    /**
     * 管理员给所有家长发送通知
     */
    @PostMapping("/send-to-all-parents")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendToAllParents(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestBody NotificationRequest request) {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "用户未认证", null));
            }
            
            // 检查当前用户是否为管理员
            if (!notificationService.isUserAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "只有管理员才能执行此操作", null));
            }
            
            NotificationResponse notification = notificationService.sendNotificationToAllParents(userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "通知已成功发送给所有家长", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "发送通知失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 管理员给所有学生发送通知
     */
    @PostMapping("/send-to-all-students")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendToAllStudents(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestBody NotificationRequest request) {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "用户未认证", null));
            }
            
            // 检查当前用户是否为管理员
            if (!notificationService.isUserAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "只有管理员才能执行此操作", null));
            }
            
            NotificationResponse notification = notificationService.sendNotificationToAllStudents(userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "通知已成功发送给所有学生", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "发送通知失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 管理员给所有教师发送通知
     */
    @PostMapping("/send-to-all-teachers")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendToAllTeachers(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestBody NotificationRequest request) {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "用户未认证", null));
            }
            
            // 检查当前用户是否为管理员
            if (!notificationService.isUserAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "只有管理员才能执行此操作", null));
            }
            
            NotificationResponse notification = notificationService.sendNotificationToAllTeachers(userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "通知已成功发送给所有教师", notification));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "发送通知失败: " + e.getMessage(), null));
        }
    }
} 