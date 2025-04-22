package com.example.auth.repository;

import com.example.auth.entity.ChatMessage;
import com.example.auth.entity.Conversation;
import com.example.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByConversationOrderBySentTimeAsc(Conversation conversation);
    
    Page<ChatMessage> findByConversationOrderBySentTimeDesc(Conversation conversation, Pageable pageable);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation = :conversation AND m.id < :messageId ORDER BY m.sentTime DESC")
    Page<ChatMessage> findOlderMessages(
        @Param("conversation") Conversation conversation,
        @Param("messageId") Long messageId,
        Pageable pageable
    );
    
    @Modifying
    @Query("UPDATE ChatMessage m SET m.read = true WHERE m.conversation = :conversation AND m.recipient = :user AND m.read = false")
    int markAllAsRead(@Param("conversation") Conversation conversation, @Param("user") User user);
    
    int countByConversationAndRecipientAndReadFalse(Conversation conversation, User recipient);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.recipient = :user AND m.read = false")
    int countUnreadMessagesByUser(@Param("user") User user);
} 