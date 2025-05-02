package com.example.auth.service.impl;

import com.example.auth.dto.ChatMessageDTO;
import com.example.auth.dto.ConversationDTO;
import com.example.auth.entity.ChatMessage;
import com.example.auth.entity.Conversation;
import com.example.auth.entity.User;
import com.example.auth.repository.ChatMessageRepository;
import com.example.auth.repository.ConversationRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ChatService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(
            ChatMessageRepository chatMessageRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // 获取用户的所有会话
    @Override
    public List<ConversationDTO> getConversationsForUser(User currentUser) {
        List<Conversation> conversations = conversationRepository.findByUserOrderByLastMessageTimeDesc(currentUser);
        
        return conversations.stream()
                .map(conversation -> new ConversationDTO(conversation, currentUser))
                .collect(Collectors.toList());
    }

    // 获取或创建两个用户之间的会话
    @Override
    @Transactional
    public ConversationDTO getOrCreateConversation(User user1, User user2) {
        Optional<Conversation> existingConversation = conversationRepository.findByUsers(user1, user2);
        
        Conversation conversation;
        if (existingConversation.isPresent()) {
            conversation = existingConversation.get();
        } else {
            conversation = new Conversation();
            conversation.setUser1(user1);
            conversation.setUser2(user2);
            conversation = conversationRepository.save(conversation);
        }
        
        // 获取最近的消息
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "sentTime"));
        Page<ChatMessage> messages = chatMessageRepository.findByConversationOrderBySentTimeDesc(conversation, pageable);
        
        List<ChatMessageDTO> messageDTOs = messages.getContent().stream()
                .sorted((m1, m2) -> m1.getSentTime().compareTo(m2.getSentTime()))
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
            
            // 查找发送者 - 增强错误处理和日志
            Optional<User> senderOpt = userRepository.findById(senderId);
            if (!senderOpt.isPresent()) {
                String errorMsg = "发送者不存在 (ID: " + senderId + ")";
                System.err.println("发送消息失败: 找不到ID为 " + senderId + " 的发送者");
                throw new RuntimeException(errorMsg);
            }
            User sender = senderOpt.get();
            
            // 检查发送者状态
            if (!"active".equals(sender.getStatus())) {
                String errorMsg = "发送者账号未激活 (ID: " + senderId + ")";
                System.err.println("发送消息失败: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("已找到发送者: " + sender.getUsername() + " (ID: " + sender.getId() + ")");
            
            // 查找接收者
            Optional<User> recipientOpt = userRepository.findById(recipientId);
            if (!recipientOpt.isPresent()) {
                String errorMsg = "接收者不存在 (ID: " + recipientId + ")";
                System.err.println("发送消息失败: 找不到ID为 " + recipientId + " 的接收者");
                throw new RuntimeException(errorMsg);
            }
            User recipient = recipientOpt.get();
            
            // 检查接收者状态
            if (!"active".equals(recipient.getStatus())) {
                String errorMsg = "接收者账号未激活 (ID: " + recipientId + ")";
                System.err.println("发送消息失败: " + errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("已找到接收者: " + recipient.getUsername() + " (ID: " + recipient.getId() + ")");
            
            // 获取或创建会话
            System.out.println("查找或创建会话");
            Optional<Conversation> optionalConversation = conversationRepository.findByUsers(sender, recipient);
            Conversation conversation;
            
            if (optionalConversation.isPresent()) {
                conversation = optionalConversation.get();
                System.out.println("使用现有会话, ID: " + conversation.getId());
            } else {
                conversation = new Conversation();
                conversation.setUser1(sender);
                conversation.setUser2(recipient);
                conversation = conversationRepository.save(conversation);
                System.out.println("已创建新会话, ID: " + conversation.getId());
            }
            
            // 创建消息
            ChatMessage message = new ChatMessage();
            message.setSender(sender);
            message.setRecipient(recipient);
            message.setContent(content);
            message.setRead(false);
            message.setConversation(conversation);
            
            // 设置文件相关信息（如果有）
            if (fileUrl != null) {
                message.setFileUrl(fileUrl);
                message.setFileName(fileName);
                message.setFileType(fileType);
                message.setFileSize(fileSize);
                System.out.println("消息包含文件: " + fileName + " (" + fileType + ")");
            }
            
            // 保存消息
            message = chatMessageRepository.save(message);
            System.out.println("已保存消息, ID: " + message.getId());
            
            // 更新会话的未读消息数和最后消息时间
            if (conversation.getUser1().equals(recipient)) {
                conversation.setUnreadCountUser1(conversation.getUnreadCountUser1() + 1);
            } else {
                conversation.setUnreadCountUser2(conversation.getUnreadCountUser2() + 1);
            }
            conversation.setLastMessageTime(message.getSentTime());
            conversationRepository.save(conversation);
            System.out.println("已更新会话的未读消息数和最后消息时间");
            
            // 创建DTO
            ChatMessageDTO messageDTO = new ChatMessageDTO(message);
            
            // 通过WebSocket向接收者和发送者发送消息
            // 向接收者发送
            messagingTemplate.convertAndSendToUser(
                    recipient.getId().toString(),
                    "/queue/messages",
                    messageDTO
            );
            
            // 向发送者也发送一份
            messagingTemplate.convertAndSendToUser(
                    sender.getId().toString(),
                    "/queue/messages",
                    messageDTO
            );
            
            System.out.println("消息已通过WebSocket推送给发送者和接收者");
            
            return messageDTO;
            
        } catch (Exception e) {
            System.err.println("发送消息时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 获取会话的消息
    @Override
    public List<ChatMessageDTO> getMessagesForConversation(Long conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentTime"));
        Page<ChatMessage> messages = chatMessageRepository.findByConversationOrderBySentTimeDesc(conversation, pageable);
        
        return messages.getContent().stream()
                .sorted((m1, m2) -> m1.getSentTime().compareTo(m2.getSentTime()))
                .map(ChatMessageDTO::new)
                .collect(Collectors.toList());
    }

    // 将会话中的消息标记为已读
    @Override
    @Transactional
    public int markConversationAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        int updatedCount = chatMessageRepository.markAllAsRead(conversation, user);
        
        // 重置会话的未读计数
        if (conversation.getUser1().getId().equals(userId)) {
            conversation.setUnreadCountUser1(0);
        } else if (conversation.getUser2().getId().equals(userId)) {
            conversation.setUnreadCountUser2(0);
        }
        
        conversationRepository.save(conversation);
        
        return updatedCount;
    }

    // 获取指定消息之前的消息
    @Override
    public List<ChatMessageDTO> getMessagesBeforeId(Long conversationId, Long messageId, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "sentTime"));
        Page<ChatMessage> messages = chatMessageRepository.findOlderMessages(conversation, messageId, pageable);
        
        return messages.getContent().stream()
                .sorted((m1, m2) -> m1.getSentTime().compareTo(m2.getSentTime()))
                .map(ChatMessageDTO::new)
                .collect(Collectors.toList());
    }

    // 获取用户的未读消息数量
    @Override
    public int getUnreadMessageCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return chatMessageRepository.countUnreadMessagesByUser(user);
    }

    // 获取用户有未读消息的会话数
    @Override
    public int getUnreadConversationCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return conversationRepository.countConversationsWithUnreadMessages(user);
    }
    
    /**
     * 向用户发送错误通知
     * @param userId 接收通知的用户ID
     * @param errorMessage 错误消息
     */
    @Override
    public void sendErrorNotification(Long userId, String errorMessage) {
        if (userId == null) {
            System.err.println("无法发送错误通知: 用户ID为空");
            return;
        }
        
        try {
            Map<String, Object> errorNotification = new HashMap<>();
            errorNotification.put("error", true);
            errorNotification.put("message", errorMessage);
            
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/errors",
                    errorNotification
            );
            
            System.out.println("已向用户 " + userId + " 发送错误通知: " + errorMessage);
        } catch (Exception e) {
            System.err.println("发送错误通知失败: " + e.getMessage());
        }
    }
} 