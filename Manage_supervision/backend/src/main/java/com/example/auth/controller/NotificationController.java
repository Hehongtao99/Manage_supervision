package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.NotificationDTO;
import com.example.auth.dto.NotificationRequest;
import com.example.auth.entity.User;
import com.example.auth.service.NotificationService;
import com.example.auth.service.TeacherStudentService;
import com.example.auth.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserContext userContext;
    
    @Autowired
    private TeacherStudentService teacherStudentService;
    
    /**
     * 创建新通知
     */
    @PostMapping
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> createNotification(@RequestBody NotificationRequest request) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户未认证"));
        }
        
        try {
            NotificationDTO notification = notificationService.createNotification(
                    currentUser.getId(), request);
            
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            logger.error("创建通知失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "创建通知失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取学生列表，供教师选择通知接收者
     */
    @GetMapping("/students")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getStudents() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户未认证"));
        }
        
        return ResponseEntity.ok(teacherStudentService.getStudentsByTeacher(currentUser.getId()));
    }
    
    /**
     * 获取我收到的所有通知
     */
    @GetMapping("/received")
    public ResponseEntity<?> getReceivedNotifications() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户未认证"));
        }
        
        List<NotificationDTO> notifications = notificationService.getNotificationsByRecipient(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 获取我发送的所有通知
     */
    @GetMapping("/sent")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getSentNotifications() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户未认证"));
        }
        
        List<NotificationDTO> notifications = notificationService.getNotificationsBySender(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationDetail(@PathVariable Long id) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户未认证"));
        }
        
        NotificationDTO notification = notificationService.getNotificationById(id);
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/supervisor/notifications")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getSupervisorNotifications(@RequestHeader("Authorization") String auth) {
        // Implementation of getSupervisorNotifications method
        return null; // Placeholder return, actual implementation needed
    }

    @GetMapping("/supervisor/notifications/unread")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> getUnreadSupervisorNotifications(@RequestHeader("Authorization") String auth) {
        // Implementation of getUnreadSupervisorNotifications method
        return null; // Placeholder return, actual implementation needed
    }

    @PutMapping("/supervisor/notifications/{id}/read")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        // Implementation of markNotificationAsRead method
        return null; // Placeholder return, actual implementation needed
    }
} 