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
    
    // 管理员发送通知给所有家长
    NotificationResponse sendNotificationToAllParents(Long adminId, NotificationRequest request);
    
    // 管理员发送通知给所有学生
    NotificationResponse sendNotificationToAllStudents(Long adminId, NotificationRequest request);
    
    // 管理员发送通知给所有教师
    NotificationResponse sendNotificationToAllTeachers(Long adminId, NotificationRequest request);
    
    // 检查用户是否是管理员
    boolean isUserAdmin(Long userId);
} 