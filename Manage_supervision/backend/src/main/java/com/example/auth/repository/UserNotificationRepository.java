package com.example.auth.repository;

import com.example.auth.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    
    List<UserNotification> findByUserId(Long userId);
    
    List<UserNotification> findByUserIdAndIsRead(Long userId, Boolean isRead);
    
    Optional<UserNotification> findByUserIdAndNotificationId(Long userId, Long notificationId);
    
    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.user.id = :userId AND un.isRead = false")
    Long countUnreadNotificationsByUserId(@Param("userId") Long userId);
} 