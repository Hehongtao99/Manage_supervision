package com.example.auth.service.impl;

import com.example.auth.dto.NotificationDTO;
import com.example.auth.dto.NotificationRequest;
import com.example.auth.entity.Notification;
import com.example.auth.entity.NotificationRecipient;
import com.example.auth.entity.User;
import com.example.auth.repository.NotificationRecipientRepository;
import com.example.auth.repository.NotificationRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationRecipientRepository recipientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    @Transactional
    public NotificationDTO createNotification(Long senderId, NotificationRequest request) {
        logger.info("创建通知: {}, 发送者ID: {}", request.getTitle(), senderId);
        
        User sender = userRepository.findById(senderId).orElse(null);
        if (sender == null) {
            logger.error("创建通知失败: 发送者(ID: {})不存在", senderId);
            throw new RuntimeException("发送者不存在");
        }
        
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSenderId(senderId);
        notification = notificationRepository.save(notification);
        
        // 为每个接收者创建记录
        List<NotificationRecipient> recipients = new ArrayList<>();
        for (Long recipientId : request.getRecipientIds()) {
            NotificationRecipient recipient = new NotificationRecipient();
            recipient.setNotificationId(notification.getId());
            recipient.setRecipientId(recipientId);
            recipient.setStatus("SENT");
            recipients.add(recipient);
        }
        recipientRepository.saveAll(recipients);
        
        // 构建DTO，处理 senderName 为空的情况
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setSenderId(notification.getSenderId());
        // 如果 realName 为空，则使用 username，如果 username 也为空，则使用 "未知"
        String senderName = sender.getRealName();
        if (senderName == null || senderName.trim().isEmpty()) {
            senderName = sender.getUsername(); // 尝试使用用户名
        }
        if (senderName == null || senderName.trim().isEmpty()) {
             senderName = "未知"; // 最后备选
        }
        dto.setSenderName(senderName); 
        dto.setCreateTime(notification.getCreateTime().format(formatter));
        dto.setRecipientIds(request.getRecipientIds());
        
        // 发送实时通知
        sendRealTimeNotification(dto);
        
        return dto;
    }
    
    @Override
    public List<NotificationDTO> getNotificationsByRecipient(Long recipientId) {
        logger.info("获取用户(ID: {})收到的通知", recipientId);
        
        List<NotificationRecipient> recipients = recipientRepository.findByRecipientIdOrderByNotificationCreateTimeDesc(recipientId);
        List<NotificationDTO> notifications = new ArrayList<>();
        
        for (NotificationRecipient recipient : recipients) {
            Notification notification = notificationRepository.findById(recipient.getNotificationId()).orElse(null);
            if (notification != null) {
                User sender = userRepository.findById(notification.getSenderId()).orElse(null);
                String senderName = sender != null ? sender.getRealName() : "未知";
                
                NotificationDTO dto = new NotificationDTO();
                dto.setId(notification.getId());
                dto.setTitle(notification.getTitle());
                dto.setContent(notification.getContent());
                dto.setSenderId(notification.getSenderId());
                dto.setSenderName(senderName);
                dto.setCreateTime(notification.getCreateTime().format(formatter));
                
                notifications.add(dto);
            }
        }
        
        return notifications;
    }
    
    @Override
    public List<NotificationDTO> getNotificationsBySender(Long senderId) {
        logger.info("获取用户(ID: {})发送的通知", senderId);
        
        List<Notification> notifications = notificationRepository.findBySenderId(senderId);
        return notifications.stream().map(notification -> {
            User sender = userRepository.findById(notification.getSenderId()).orElse(null);
            String senderName = sender != null ? sender.getRealName() : "未知";
            
            List<NotificationRecipient> recipients = recipientRepository.findByNotificationId(notification.getId());
            List<Long> recipientIds = recipients.stream()
                    .map(NotificationRecipient::getRecipientId)
                    .collect(Collectors.toList());
            
            NotificationDTO dto = new NotificationDTO();
            dto.setId(notification.getId());
            dto.setTitle(notification.getTitle());
            dto.setContent(notification.getContent());
            dto.setSenderId(notification.getSenderId());
            dto.setSenderName(senderName);
            dto.setCreateTime(notification.getCreateTime().format(formatter));
            dto.setRecipientIds(recipientIds);
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public NotificationDTO getNotificationById(Long notificationId) {
        logger.info("获取通知详情, ID: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            logger.warn("获取通知详情失败: 未找到ID为 {} 的通知", notificationId);
            return null;
        }
        
        // 获取发送者信息，并处理 senderName 为空的情况
        User sender = userRepository.findById(notification.getSenderId()).orElse(null);
        String senderName;
        if (sender != null) {
            senderName = sender.getRealName();
            if (senderName == null || senderName.trim().isEmpty()) {
                senderName = sender.getUsername(); // 尝试使用用户名
            }
            if (senderName == null || senderName.trim().isEmpty()) {
                senderName = "未知"; // 最后备选
            }
        } else {
            senderName = "未知 (ID: " + notification.getSenderId() + ")"; // 发送者用户不存在
            logger.warn("通知 (ID: {}) 的发送者 (ID: {}) 不存在", notificationId, notification.getSenderId());
        }
        
        List<NotificationRecipient> recipients = recipientRepository.findByNotificationId(notification.getId());
        List<Long> recipientIds = recipients.stream()
                .map(NotificationRecipient::getRecipientId)
                .collect(Collectors.toList());
        
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setSenderId(notification.getSenderId());
        dto.setSenderName(senderName); // 使用处理后的 senderName
        dto.setCreateTime(notification.getCreateTime().format(formatter));
        dto.setRecipientIds(recipientIds);
        
        return dto;
    }
    
    @Override
    public void sendRealTimeNotification(NotificationDTO notification) {
        logger.info("发送实时通知: {}", notification.getTitle());
        
        // 为每个接收者发送WebSocket通知
        for (Long recipientId : notification.getRecipientIds()) {
            try {
                logger.info("向用户(ID: {})发送WebSocket通知", recipientId);
                
                // 发送通知到用户特定的通知队列
                messagingTemplate.convertAndSendToUser(
                        recipientId.toString(),
                        "/queue/notifications",
                        notification
                );
            } catch (Exception e) {
                logger.error("向用户(ID: {})发送实时通知失败: {}", recipientId, e.getMessage(), e);
            }
        }
    }
} 