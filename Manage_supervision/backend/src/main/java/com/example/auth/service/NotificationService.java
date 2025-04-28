package com.example.auth.service;

import com.example.auth.dto.NotificationDTO;
import com.example.auth.dto.NotificationRequest;
import com.example.auth.entity.Notification;

import java.util.List;

public interface NotificationService {
    /**
     * 创建新通知并发送给指定学生
     */
    NotificationDTO createNotification(Long senderId, NotificationRequest request);
    
    /**
     * 获取用户收到的所有通知
     */
    List<NotificationDTO> getNotificationsByRecipient(Long recipientId);
    
    /**
     * 获取用户发送的所有通知
     */
    List<NotificationDTO> getNotificationsBySender(Long senderId);
    
    /**
     * 获取通知详情
     */
    NotificationDTO getNotificationById(Long notificationId);
    
    /**
     * 发送WebSocket实时通知
     */
    void sendRealTimeNotification(NotificationDTO notification);
} 