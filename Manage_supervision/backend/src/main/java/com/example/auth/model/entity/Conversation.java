package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TableName("conversations")
public class Conversation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user1_id")
    private Long user1Id;

    @TableField("user2_id")
    private Long user2Id;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    @TableField("unread_count_user1")
    private int unreadCountUser1 = 0;

    @TableField("unread_count_user2")
    private int unreadCountUser2 = 0;

    @TableField(exist = false)
    private User user1;

    @TableField(exist = false)
    private User user2;

    @TableField(exist = false)
    private List<ChatMessage> messages = new ArrayList<>();

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public int getUnreadCountUser1() {
        return unreadCountUser1;
    }

    public int getUnreadCountUser2() {
        return unreadCountUser2;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
        if (user1 != null) {
            this.user1Id = user1.getId();
        }
    }

    public void setUser2(User user2) {
        this.user2 = user2;
        if (user2 != null) {
            this.user2Id = user2.getId();
        }
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setUnreadCountUser1(int unreadCountUser1) {
        this.unreadCountUser1 = unreadCountUser1;
    }

    public void setUnreadCountUser2(int unreadCountUser2) {
        this.unreadCountUser2 = unreadCountUser2;
    }

    // Helper methods
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setConversation(this);
        this.lastMessageTime = message.getSentTime();
        
        // Update unread counts
        if (message.getRecipient() != null && message.getRecipient().getId().equals(getUser1Id())) {
            unreadCountUser1++;
        } else if (message.getRecipient() != null && message.getRecipient().getId().equals(getUser2Id())) {
            unreadCountUser2++;
        }
    }

    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setConversation(null);
    }

    public int getUnreadCountForUser(User user) {
        if (user != null) {
            if (user.getId().equals(getUser1Id())) {
                return unreadCountUser1;
            } else if (user.getId().equals(getUser2Id())) {
                return unreadCountUser2;
            }
        }
        return 0;
    }

    public void resetUnreadCountForUser(User user) {
        if (user != null) {
            if (user.getId().equals(getUser1Id())) {
                unreadCountUser1 = 0;
            } else if (user.getId().equals(getUser2Id())) {
                unreadCountUser2 = 0;
            }
        }
    }
} 