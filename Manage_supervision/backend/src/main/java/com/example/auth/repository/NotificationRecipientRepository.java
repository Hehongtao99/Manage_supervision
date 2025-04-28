package com.example.auth.repository;

import com.example.auth.entity.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    List<NotificationRecipient> findByRecipientId(Long recipientId);
    
    @Query("SELECT nr FROM NotificationRecipient nr JOIN Notification n ON nr.notificationId = n.id WHERE nr.recipientId = :recipientId ORDER BY n.createTime DESC")
    List<NotificationRecipient> findByRecipientIdOrderByNotificationCreateTimeDesc(@Param("recipientId") Long recipientId);
    
    List<NotificationRecipient> findByNotificationId(Long notificationId);
} 