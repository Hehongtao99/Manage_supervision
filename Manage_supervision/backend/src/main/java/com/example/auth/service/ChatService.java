package com.example.auth.service;

import com.example.auth.model.dto.ChatMessageDTO;
import com.example.auth.model.dto.ConversationDTO;
import com.example.auth.model.entity.User;

import java.util.List;

public interface ChatService {

    // 获取用户的所有会话
    List<ConversationDTO> getConversationsForUser(User currentUser);

    // 获取或创建两个用户之间的会话
    ConversationDTO getOrCreateConversation(User user1, User user2);

    // 发送纯文本消息
    ChatMessageDTO sendMessage(Long senderId, Long recipientId, String content);
    
    // 发送消息（支持文件）
    ChatMessageDTO sendMessage(Long senderId, Long recipientId, String content, 
                              String fileUrl, String fileName, String fileType, Long fileSize);

    // 获取会话的消息
    List<ChatMessageDTO> getMessagesForConversation(Long conversationId, int page, int size);

    // 将会话中的消息标记为已读
    int markConversationAsRead(Long conversationId, Long userId);

    // 获取指定消息之前的消息
    List<ChatMessageDTO> getMessagesBeforeId(Long conversationId, Long messageId, int size);

    // 获取用户的未读消息数量
    int getUnreadMessageCount(Long userId);

    // 获取用户有未读消息的会话数
    int getUnreadConversationCount(Long userId);
    
    /**
     * 向用户发送错误通知
     * @param userId 接收通知的用户ID
     * @param errorMessage 错误消息
     */
    void sendErrorNotification(Long userId, String errorMessage);
}