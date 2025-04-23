package com.example.auth.service;

import com.example.auth.dto.NotificationRequest;
import com.example.auth.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    
    // 创建通知
    NotificationResponse createNotification(Long senderId, NotificationRequest request);
    
    // 获取通知列表（针对学生和教师）
    List<NotificationResponse> getNotificationsForUser(Long userId);
    
    // 获取发送的通知（教师端）
    List<NotificationResponse> getSentNotifications(Long teacherId);
    
    // 获取所有通知（管理员端）
    List<NotificationResponse> getAllNotifications();
} 