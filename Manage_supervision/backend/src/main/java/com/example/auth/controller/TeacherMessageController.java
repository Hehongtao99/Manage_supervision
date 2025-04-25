package com.example.auth.controller;

import com.example.auth.dto.ParentTeacherMessageDTO;
import com.example.auth.dto.ParentTeacherMessageReplyRequest;
import com.example.auth.entity.User;
import com.example.auth.service.TeacherMessageService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * 教师留言控制器
 * 提供教师查看和回复留言的接口
 */
@RestController
@RequestMapping("/api/teacher-messages")
public class TeacherMessageController {

    @Autowired
    private TeacherMessageService teacherMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                Optional<User> userOpt = Optional.ofNullable(userService.findById(userId));
                if (userOpt.isPresent()) {
                    return userOpt.get();
                }
            }
        }
        return null;
    }

    /**
     * 检查用户是否拥有指定角色
     */
    private boolean hasRole(User user, String roleName) {
        if (user == null || user.getRoles() == null || roleName == null) {
            return false;
        }
        
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equalsIgnoreCase(role.getName()));
    }

    /**
     * 检查用户是否为教师或督导员
     */
    private boolean isTeacherOrSupervisor(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        
        return user.getRoles().stream()
                .anyMatch(role -> "TEACHER".equalsIgnoreCase(role.getName()) || 
                                 "SUPERVISOR".equalsIgnoreCase(role.getName()));
    }

    /**
     * 获取教师收到的所有留言（分页）
     */
    @GetMapping
    public ResponseEntity<?> getTeacherMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        try {
            System.out.println("接收到获取教师留言请求: page=" + page + ", size=" + size);
            User teacher = getCurrentUser(httpRequest);
            if (teacher == null) {
                System.out.println("用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户未登录"));
            }
            
            System.out.println("当前教师ID: " + teacher.getId() + ", 用户名: " + teacher.getUsername());
            
            // 验证教师角色
            if (!isTeacherOrSupervisor(teacher)) {
                System.out.println("用户不是教师或督导员: " + teacher.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只有教师或督导员可以查看留言"));
            }
            
            Page<ParentTeacherMessageDTO> messages = teacherMessageService.getMessagesByTeacher(teacher.getId(), page, size);
            System.out.println("查询结果: 总数=" + messages.getTotalElements() + ", 内容数量=" + messages.getContent().size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("获取教师留言异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取留言失败: " + e.getMessage()));
        }
    }

    /**
     * 获取教师收到的关于特定学生的留言（分页）
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getTeacherMessagesByStudent(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        try {
            User teacher = getCurrentUser(httpRequest);
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户未登录"));
            }
            
            // 验证教师角色
            if (!isTeacherOrSupervisor(teacher)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只有教师或督导员可以查看留言"));
            }
            
            Page<ParentTeacherMessageDTO> messages = teacherMessageService.getMessagesByTeacherAndStudent(
                    teacher.getId(), studentId, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取留言失败: " + e.getMessage()));
        }
    }

    /**
     * 教师回复留言
     */
    @PostMapping("/{messageId}/reply")
    public ResponseEntity<?> replyMessage(
            @PathVariable Long messageId,
            @RequestBody ParentTeacherMessageReplyRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            User teacher = getCurrentUser(httpRequest);
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户未登录"));
            }
            
            // 验证教师角色
            if (!isTeacherOrSupervisor(teacher)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只有教师或督导员可以回复留言"));
            }
            
            if (request.getReplyContent() == null || request.getReplyContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "回复内容不能为空"));
            }
            
            ParentTeacherMessageDTO replyMessage = teacherMessageService.replyMessage(
                    messageId, teacher.getId(), request.getReplyContent());
            
            return ResponseEntity.ok(replyMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "回复留言失败: " + e.getMessage()));
        }
    }

    /**
     * 将所有留言标记为已读
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest httpRequest) {
        try {
            User teacher = getCurrentUser(httpRequest);
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户未登录"));
            }
            
            // 验证教师角色
            if (!isTeacherOrSupervisor(teacher)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只有教师或督导员可以标记留言"));
            }
            
            int count = teacherMessageService.markAllAsRead(teacher.getId());
            return ResponseEntity.ok(Map.of(
                "message", "标记成功",
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "标记失败: " + e.getMessage()));
        }
    }
} 