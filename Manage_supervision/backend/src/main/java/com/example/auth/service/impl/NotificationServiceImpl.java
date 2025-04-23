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
        
        // 设置是否为全局通知
        if ("ALL".equals(request.getRecipientType())) {
            notification.setIsGlobal(true);
        } else {
            notification.setIsGlobal(false);
        }

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
            List<ClassStudentRelation> relations = classStudentRelationRepository.findByClassEntityAndStatus(
                    notification.getClassEntity(), "active");
            
            if (relations.isEmpty()) {
                System.out.println("警告: 班级 " + request.getClassId() + " 没有活跃学生");
            } else {
                System.out.println("为班级 " + request.getClassId() + " 的 " + relations.size() + " 名学生创建通知关系");
            }
            
            recipients = relations.stream()
                    .map(ClassStudentRelation::getStudent)
                    .collect(Collectors.toList());
        }

        // 创建用户通知关系
        for (User recipient : recipients) {
            UserNotification userNotification = new UserNotification();
            userNotification.setUser(recipient);
            userNotification.setNotification(notification);
            userNotificationRepository.save(userNotification);
        }

        return convertToDto(notification, null);
    }

    @Override
    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        // 获取用户对象
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 获取用户当前所在的班级(只获取active状态的班级)
        List<ClassStudentRelation> activeClassRelations = classStudentRelationRepository.findByStudentAndStatus(user, "active");
        List<Long> activeClassIds = activeClassRelations.stream()
                .map(relation -> relation.getClassEntity().getId())
                .collect(Collectors.toList());
        
        List<UserNotification> userNotifications = userNotificationRepository.findByUserId(userId);
        List<NotificationResponse> result = new ArrayList<>();
        
        for (UserNotification un : userNotifications) {
            Notification notification = un.getNotification();
            
            // 过滤通知：
            // 1. 如果是全局通知(ALL)，则显示
            // 2. 如果是班级通知(CLASS)且用户当前在该班级，则显示
            // 3. 其他情况不显示
            boolean shouldShow = "ALL".equals(notification.getRecipientType()) || 
                    ("CLASS".equals(notification.getRecipientType()) && 
                     notification.getClassEntity() != null && 
                     activeClassIds.contains(notification.getClassEntity().getId()));
            
            if (shouldShow) {
                result.add(convertToDto(notification, un));
            }
        }
        
        return result;
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
        response.setIsGlobal(notification.getIsGlobal());
        
        // 如果是班级通知，设置班级信息
        if ("CLASS".equals(notification.getRecipientType()) && notification.getClassEntity() != null) {
            response.setClassId(notification.getClassEntity().getId());
            response.setClassName(notification.getClassEntity().getClassName());
        }
        
        return response;
    }
} 