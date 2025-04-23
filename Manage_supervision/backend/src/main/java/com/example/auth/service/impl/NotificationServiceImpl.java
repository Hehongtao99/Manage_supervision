package com.example.auth.service.impl;

import com.example.auth.dto.NotificationRequest;
import com.example.auth.dto.NotificationResponse;
import com.example.auth.entity.*;
import com.example.auth.repository.*;
import com.example.auth.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassStudentRelationRepository classStudentRelationRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(Long senderId, NotificationRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSender(sender);
        notification.setRecipientType(request.getRecipientType());
        notification.setStatus("active");

        // 如果是班级通知，设置班级
        if ("CLASS".equals(request.getRecipientType())) {
            if (request.getClassId() == null) {
                throw new RuntimeException("班级通知必须指定班级ID");
            }
            com.example.auth.entity.Class classEntity = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new RuntimeException("班级不存在"));
            notification.setClassEntity(classEntity);
        }

        // 保存通知
        notification = notificationRepository.save(notification);

        // 为相关用户创建通知关系
        List<User> recipients = new ArrayList<>();
        if ("ALL".equals(request.getRecipientType())) {
            // 所有用户都收到通知
            recipients = userRepository.findAll();
        } else if ("CLASS".equals(request.getRecipientType())) {
            // 只有班级内的学生收到通知
            List<ClassStudentRelation> relations = classStudentRelationRepository.findByClassEntityId(request.getClassId());
            recipients = relations.stream()
                    .map(ClassStudentRelation::getStudent)
                    .collect(Collectors.toList());
        }

        // 创建用户通知关系
        for (User recipient : recipients) {
            UserNotification userNotification = new UserNotification();
            userNotification.setUser(recipient);
            userNotification.setNotification(notification);
            userNotification.setIsRead(false);
            userNotificationRepository.save(userNotification);
        }

        return convertToDto(notification, null);
    }

    @Override
    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        List<UserNotification> userNotifications = userNotificationRepository.findByUserId(userId);
        return userNotifications.stream()
                .map(un -> convertToDto(un.getNotification(), un))
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnreadNotificationCount(Long userId) {
        return userNotificationRepository.countUnreadNotificationsByUserId(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markNotificationAsRead(Long userId, Long notificationId) {
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在或不属于当前用户"));

        if (!userNotification.getIsRead()) {
            userNotification.setIsRead(true);
            userNotification.setReadTime(LocalDateTime.now());
            userNotification = userNotificationRepository.save(userNotification);
        }

        return convertToDto(userNotification.getNotification(), userNotification);
    }

    @Override
    public List<NotificationResponse> getSentNotifications(Long teacherId) {
        List<Notification> notifications = notificationRepository.findBySenderId(teacherId);
        return notifications.stream()
                .map(n -> convertToDto(n, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream()
                .map(n -> convertToDto(n, null))
                .collect(Collectors.toList());
    }

    private NotificationResponse convertToDto(Notification notification, UserNotification userNotification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setSenderId(notification.getSender().getId());
        
        // 设置发送者信息
        User sender = notification.getSender();
        response.setSenderName(sender.getRealName() != null ? sender.getRealName() : sender.getUsername());
        response.setSenderAvatar(sender.getAvatar());
        
        response.setRecipientType(notification.getRecipientType());
        response.setCreateTime(notification.getCreateTime());
        
        // 如果是班级通知，设置班级信息
        if ("CLASS".equals(notification.getRecipientType()) && notification.getClassEntity() != null) {
            response.setClassId(notification.getClassEntity().getId());
            response.setClassName(notification.getClassEntity().getClassName());
        }
        
        // 如果有用户通知关系信息，设置已读状态和已读时间
        if (userNotification != null) {
            response.setIsRead(userNotification.getIsRead());
            response.setReadTime(userNotification.getReadTime());
        } else {
            response.setIsRead(false);
        }
        
        return response;
    }
} 