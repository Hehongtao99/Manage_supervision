package com.example.auth.repository;

import com.example.auth.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 根据发送者ID查找所有通知
     */
    List<Notification> findBySenderId(Long senderId);
    
    /**
     * 根据发送者ID查找所有通知，按照创建时间降序排序
     */
    @Query("SELECT n FROM Notification n WHERE n.sender.id = :senderId ORDER BY n.createTime DESC")
    List<Notification> findBySenderIdOrderByCreateTimeDesc(@Param("senderId") Long senderId);
    
    /**
     * 查找所有通知，按照创建时间降序排序
     */
    @Query("SELECT n FROM Notification n ORDER BY n.createTime DESC")
    List<Notification> findAllOrderByCreateTimeDesc();
    
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'ALL' OR (n.recipientType = 'CLASS' AND n.classEntity.id = :classId)")
    List<Notification> findAllByRecipientTypeAllOrClass(@Param("classId") Long classId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'ALL' OR (n.recipientType = 'CLASS' AND n.classEntity.id IN :classIds)")
    List<Notification> findAllByRecipientTypeAllOrClassIn(@Param("classIds") List<Long> classIds);
    
    /**
     * 获取最近的通知，按照创建时间降序排序并限制数量
     */
    @Query("SELECT n FROM Notification n ORDER BY n.createTime DESC")
    List<Notification> findRecentNotifications(Pageable pageable);
} 