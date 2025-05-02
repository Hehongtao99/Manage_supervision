package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.ChatMessageDTO;
import com.example.auth.model.dto.ConversationDTO;
import com.example.auth.model.entity.ChatMessage;
import com.example.auth.model.entity.Conversation;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.ChatMessageMapper;
import com.example.auth.mapper.ConversationMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ConversationMapper conversationMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(
            ChatMessageMapper chatMessageMapper,
            ConversationMapper conversationMapper,
            UserMapper userMapper,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatMessageMapper = chatMessageMapper;
        this.conversationMapper = conversationMapper;
        this.userMapper = userMapper;
        this.messagingTemplate = messagingTemplate;
    }

    // 获取用户的所有会话
    @Override
    public List<ConversationDTO> getConversationsForUser(User currentUser) {
        List<Conversation> conversations = conversationMapper.findByUserIdOrderByLastMessageTimeDesc(currentUser.getId());
        
        // 补充会话的用户信息
        for (Conversation conversation : conversations) {
            if (conversation.getUser1Id() != null) {
                conversation.setUser1(userMapper.selectById(conversation.getUser1Id()));
            }
            if (conversation.getUser2Id() != null) {
                conversation.setUser2(userMapper.selectById(conversation.getUser2Id()));
            }
        }
        
        return conversations.stream()
                .map(conversation -> new ConversationDTO(conversation, currentUser))
                .collect(Collectors.toList());
    }

    // 获取或创建两个用户之间的会话
    @Override
    @Transactional
    public ConversationDTO getOrCreateConversation(User user1, User user2) {
        Conversation conversation = conversationMapper.findByUserIds(user1.getId(), user2.getId());
        
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUser1(user1);
            conversation.setUser2(user2);
            conversation.setCreatedTime(LocalDateTime.now());
            conversation.setLastMessageTime(LocalDateTime.now());
            conversationMapper.insert(conversation);
        } else {
            // 补充用户信息
            conversation.setUser1(user1);
            conversation.setUser2(user2);
        }
        
        // 获取最近的消息
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ChatMessage> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getConversationId, conversation.getId())
                    .orderByDesc(ChatMessage::getSentTime)
                    .last("LIMIT 20");
        
        List<ChatMessage> messages = chatMessageMapper.selectList(queryWrapper);
        
        // 补充发送者和接收者信息
        for (ChatMessage message : messages) {
            message.setSender(userMapper.selectById(message.getSenderId()));
            message.setRecipient(userMapper.selectById(message.getRecipientId()));
        }
        
        // 按发送时间排序
        messages.sort(Comparator.comparing(ChatMessage::getSentTime));
        
        List<ChatMessageDTO> messageDTOs = messages.stream()
                .map(ChatMessageDTO::new)
                .collect(Collectors.toList());
        
        return new ConversationDTO(conversation, user1, messageDTOs);
    }

    // 发送纯文本消息
    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Long senderId, Long recipientId, String content) {
        return sendMessage(senderId, recipientId, content, null, null, null, null);
    }
    
    // 发送消息（支持文件）
    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Long senderId, Long recipientId, String content, 
                                       String fileUrl, String fileName, String fileType, Long fileSize) {
        try {
            System.out.println("ChatService.sendMessage() - senderId: " + senderId + ", recipientId: " + recipientId);
            
            if (senderId == null) {
                throw new IllegalArgumentException("发送者ID不能为空");
            }
            
            if (recipientId == null) {
                throw new IllegalArgumentException("接收者ID不能为空");
            }
            
            if (content == null || content.trim().isEmpty()) {
                // 如果是文件消息，允许内容为空
                if (fileUrl == null) {
                    throw new IllegalArgumentException("消息内容不能为空");
                } else {
                    content = ""; // 设置为空字符串避免数据库约束问题
                }
            }
            
            // 查找发送者
            User sender = userMapper.selectById(senderId);
            if (sender == null) {
                String errorMsg = "发送者不存在 (ID: " + senderId + ")";
                System.err.println("发送消息失败: 找不到ID为 " + senderId + " 的发送者");
                throw new RuntimeException(errorMsg);
            }
            
            // 检查发送者状态
            if (!"active".equals(sender.getStatus())) {
                String errorMsg = "发送者账号未激活 (ID: " + senderId + ")";
                System.err.println("发送消息失败: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("已找到发送者: " + sender.getUsername() + " (ID: " + sender.getId() + ")");
            
            // 查找接收者
            User recipient = userMapper.selectById(recipientId);
            if (recipient == null) {
                String errorMsg = "接收者不存在 (ID: " + recipientId + ")";
                System.err.println("发送消息失败: 找不到ID为 " + recipientId + " 的接收者");
                throw new RuntimeException(errorMsg);
            }
            
            // 检查接收者状态
            if (!"active".equals(recipient.getStatus())) {
                String errorMsg = "接收者账号未激活 (ID: " + recipientId + ")";
                System.err.println("发送消息失败: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("已找到接收者: " + recipient.getUsername() + " (ID: " + recipient.getId() + ")");
            
            // 获取或创建会话
            System.out.println("查找或创建会话");
            Conversation conversation = conversationMapper.findByUserIds(senderId, recipientId);
            
            if (conversation == null) {
                conversation = new Conversation();
                conversation.setUser1Id(senderId);
                conversation.setUser2Id(recipientId);
                conversation.setCreatedTime(LocalDateTime.now());
                conversation.setLastMessageTime(LocalDateTime.now());
                conversationMapper.insert(conversation);
                System.out.println("已创建新会话, ID: " + conversation.getId());
            } else {
                System.out.println("使用现有会话, ID: " + conversation.getId());
            }
            
            // 创建消息
            ChatMessage message = new ChatMessage();
            message.setSenderId(senderId);
            message.setRecipientId(recipientId);
            message.setConversationId(conversation.getId());
            message.setContent(content);
            message.setRead(false);
            message.setSentTime(LocalDateTime.now());
            
            // 设置文件相关信息（如果有）
            if (fileUrl != null) {
                message.setFileUrl(fileUrl);
                message.setFileName(fileName);
                message.setFileType(fileType);
                message.setFileSize(fileSize);
                System.out.println("消息包含文件: " + fileName + " (" + fileType + ")");
            }
            
            // 保存消息
            chatMessageMapper.insert(message);
            System.out.println("已保存消息, ID: " + message.getId());
            
            // 更新会话的未读消息数和最后消息时间
            if (conversation.getUser1Id().equals(recipientId)) {
                conversation.setUnreadCountUser1(conversation.getUnreadCountUser1() + 1);
            } else {
                conversation.setUnreadCountUser2(conversation.getUnreadCountUser2() + 1);
            }
            conversation.setLastMessageTime(message.getSentTime());
            conversationMapper.updateById(conversation);
            System.out.println("已更新会话的未读消息数和最后消息时间");
            
            // 设置发送者和接收者引用，以便DTO可以访问用户信息
            message.setSender(sender);
            message.setRecipient(recipient);
            
            // 创建DTO
            ChatMessageDTO messageDTO = new ChatMessageDTO(message);
            
            // 通过WebSocket发送消息到前端
            sendMessageNotification(recipientId, messageDTO);
            
            return messageDTO;
        } catch (Exception e) {
            System.err.println("发送消息时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("发送消息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ChatMessageDTO> getMessagesForConversation(Long conversationId, int page, int size) {
        // 使用MyBatis-Plus分页
        Page<ChatMessage> pageParam = new Page<>(page, size);
        
        // 获取会话消息（按时间降序）
        pageParam = (Page<ChatMessage>) chatMessageMapper.findByConversationIdOrderBySentTimeDesc(pageParam, conversationId);
        
        List<ChatMessage> messages = pageParam.getRecords();
        
        // 补充发送者和接收者信息
        for (ChatMessage message : messages) {
            message.setSender(userMapper.selectById(message.getSenderId()));
            message.setRecipient(userMapper.selectById(message.getRecipientId()));
        }
        
        // 按发送时间排序（升序）
        messages.sort(Comparator.comparing(ChatMessage::getSentTime));
        
        return messages.stream()
                .map(ChatMessageDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int markConversationAsRead(Long conversationId, Long userId) {
        // 获取会话
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return 0;
        }
        
        // 更新会话的未读消息计数
        if (conversation.getUser1Id().equals(userId)) {
            conversation.setUnreadCountUser1(0);
        } else if (conversation.getUser2Id().equals(userId)) {
            conversation.setUnreadCountUser2(0);
        } else {
            return 0; // 用户不属于该会话
        }
        
        conversationMapper.updateById(conversation);
        
        // 将消息标记为已读
        int updatedCount = chatMessageMapper.markAllAsRead(conversationId, userId);
        
        // 通知发送者，接收者已阅读消息
        User user = userMapper.selectById(userId);
        if (user != null) {
            Long otherUserId = conversation.getUser1Id().equals(userId) ? 
                    conversation.getUser2Id() : conversation.getUser1Id();
            
            Map<String, Object> readNotification = new HashMap<>();
            readNotification.put("type", "READ_NOTIFICATION");
            readNotification.put("conversationId", conversationId);
            readNotification.put("readByUser", new UserDTO(user));
            
            messagingTemplate.convertAndSendToUser(
                    otherUserId.toString(), 
                    "/queue/notifications",
                    readNotification
            );
        }
        
        return updatedCount;
    }

    @Override
    public List<ChatMessageDTO> getMessagesBeforeId(Long conversationId, Long messageId, int size) {
        // 使用MyBatis-Plus分页
        Page<ChatMessage> pageParam = new Page<>(1, size);
        
        // 获取指定ID之前的消息
        pageParam = (Page<ChatMessage>) chatMessageMapper.findOlderMessages(pageParam, conversationId, messageId);
        
        List<ChatMessage> messages = pageParam.getRecords();
        
        // 补充发送者和接收者信息
        for (ChatMessage message : messages) {
            message.setSender(userMapper.selectById(message.getSenderId()));
            message.setRecipient(userMapper.selectById(message.getRecipientId()));
        }
        
        // 按发送时间排序（升序）
        messages.sort(Comparator.comparing(ChatMessage::getSentTime));
        
        return messages.stream()
                .map(ChatMessageDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        return chatMessageMapper.countTotalUnreadMessagesByUser(userId);
    }

    @Override
    public int getUnreadConversationCount(Long userId) {
        return conversationMapper.countConversationsWithUnreadMessages(userId);
    }

    private void sendMessageNotification(Long userId, ChatMessageDTO message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/messages",
                    message
            );
        } catch (Exception e) {
            System.err.println("发送WebSocket通知时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendErrorNotification(Long userId, String errorMessage) {
        try {
            Map<String, Object> errorNotification = new HashMap<>();
            errorNotification.put("type", "ERROR");
            errorNotification.put("message", errorMessage);
            
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    errorNotification
            );
        } catch (Exception e) {
            System.err.println("发送错误通知时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static class UserDTO {
        private Long id;
        private String username;
        private String realName;
        private String avatar;
        
        public UserDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.realName = user.getRealName();
            this.avatar = user.getAvatar();
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getRealName() { return realName; }
        public String getAvatar() { return avatar; }
    }
} 