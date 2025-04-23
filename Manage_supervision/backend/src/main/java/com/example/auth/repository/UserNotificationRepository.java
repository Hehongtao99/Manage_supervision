package com.example.auth.repository;

import com.example.auth.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    
    List<UserNotification> findByUserId(Long userId);
    
    Optional<UserNotification> findByUserIdAndNotificationId(Long userId, Long notificationId);
} 