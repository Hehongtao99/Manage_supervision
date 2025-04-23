package com.example.auth.repository;

import com.example.auth.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findBySenderId(Long senderId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'ALL' OR (n.recipientType = 'CLASS' AND n.classEntity.id = :classId)")
    List<Notification> findAllByRecipientTypeAllOrClass(@Param("classId") Long classId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'ALL' OR (n.recipientType = 'CLASS' AND n.classEntity.id IN :classIds)")
    List<Notification> findAllByRecipientTypeAllOrClassIn(@Param("classIds") List<Long> classIds);
} 