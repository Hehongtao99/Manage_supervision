package com.example.auth.model.dto;

import com.example.auth.model.entity.Conversation;
import com.example.auth.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDTO {
    private Long id;
    private UserSummaryDTO user1;
    private UserSummaryDTO user2;
    private LocalDateTime createdTime;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private List<ChatMessageDTO> recentMessages;
    private ChatMessageDTO lastMessage;

    public ConversationDTO() {
    }

    public ConversationDTO(Conversation conversation, User currentUser) {
        this.id = conversation.getId();
        
        User otherUser;
        if (conversation.getUser1().getId().equals(currentUser.getId())) {
            otherUser = conversation.getUser2();
            this.unreadCount = conversation.getUnreadCountUser1();
        } else {
            otherUser = conversation.getUser1();
            this.unreadCount = conversation.getUnreadCountUser2();
        }
        
        this.user1 = new UserSummaryDTO(currentUser);
        this.user2 = new UserSummaryDTO(otherUser);
        this.createdTime = conversation.getCreatedTime();
        this.lastMessageTime = conversation.getLastMessageTime();
        
        if (!conversation.getMessages().isEmpty()) {
            this.lastMessage = new ChatMessageDTO(
                conversation.getMessages().stream()
                    .sorted((m1, m2) -> m2.getSentTime().compareTo(m1.getSentTime()))
                    .findFirst()
                    .orElse(null)
            );
        }
    }
    
    public ConversationDTO(Conversation conversation, User currentUser, List<ChatMessageDTO> recentMessages) {
        this(conversation, currentUser);
        this.recentMessages = recentMessages;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public UserSummaryDTO getUser1() {
        return user1;
    }

    public UserSummaryDTO getUser2() {
        return user2;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public List<ChatMessageDTO> getRecentMessages() {
        return recentMessages;
    }

    public ChatMessageDTO getLastMessage() {
        return lastMessage;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser1(UserSummaryDTO user1) {
        this.user1 = user1;
    }

    public void setUser2(UserSummaryDTO user2) {
        this.user2 = user2;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setRecentMessages(List<ChatMessageDTO> recentMessages) {
        this.recentMessages = recentMessages;
    }

    public void setLastMessage(ChatMessageDTO lastMessage) {
        this.lastMessage = lastMessage;
    }

    // 内部类：用户摘要信息
    public static class UserSummaryDTO {
        private Long id;
        private String username;
        private String name;
        private String avatar;
        
        public UserSummaryDTO() {
        }
        
        public UserSummaryDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.name = user.getRealName() != null ? user.getRealName() : user.getUsername();
            this.avatar = user.getAvatar();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
} 