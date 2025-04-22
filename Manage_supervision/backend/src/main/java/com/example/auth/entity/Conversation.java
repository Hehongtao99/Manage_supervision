package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "unread_count_user1", nullable = false)
    private int unreadCountUser1 = 0;

    @Column(name = "unread_count_user2", nullable = false)
    private int unreadCountUser2 = 0;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        lastMessageTime = createdTime;
    }

    // Getters
    public Long getId() {
        return id;
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

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
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
        if (message.getRecipient().equals(user1)) {
            unreadCountUser1++;
        } else if (message.getRecipient().equals(user2)) {
            unreadCountUser2++;
        }
    }

    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setConversation(null);
    }

    public int getUnreadCountForUser(User user) {
        if (user.equals(user1)) {
            return unreadCountUser1;
        } else if (user.equals(user2)) {
            return unreadCountUser2;
        }
        return 0;
    }

    public void resetUnreadCountForUser(User user) {
        if (user.equals(user1)) {
            unreadCountUser1 = 0;
        } else if (user.equals(user2)) {
            unreadCountUser2 = 0;
        }
    }
} 