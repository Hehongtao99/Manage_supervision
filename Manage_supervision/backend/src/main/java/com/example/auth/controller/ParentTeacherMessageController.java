package com.example.auth.controller;

import com.example.auth.dto.ParentTeacherMessageDTO;
import com.example.auth.dto.ParentTeacherMessageRequest;
import com.example.auth.entity.User;
import com.example.auth.service.ParentTeacherMessageService;
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
 * 家长-教师留言控制器
 * 提供家长向教师发送留言的接口
 */
@RestController
@RequestMapping("/api/messages")
public class ParentTeacherMessageController {

    @Autowired
    private ParentTeacherMessageService messageService;

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
     * 家长向教师发送留言
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestBody ParentTeacherMessageRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            System.out.println("收到留言请求: " + request);
            
            User parent = getCurrentUser(httpRequest);
            if (parent == null) {
                System.out.println("用户未登录或无法获取用户信息");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户未登录"));
            }
            
            System.out.println("当前用户: ID=" + parent.getId() + ", 用户名=" + parent.getUsername());

            // 验证家长角色
            if (!hasRole(parent, "PARENT")) {
                System.out.println("用户没有家长角色权限: " + parent.getRoles());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只有家长用户可以发送留言"));
            }

            try {
                // 详细记录请求参数
                System.out.println("请求参数: teacherId=" + request.getTeacherId() 
                    + ", studentId=" + request.getStudentId() 
                    + ", content=" + (request.getContent() != null ? request.getContent().substring(0, Math.min(20, request.getContent().length())) + "..." : "null"));
                
                ParentTeacherMessageDTO message = messageService.sendMessage(parent, request);
                System.out.println("留言发送成功: ID=" + message.getId());
                return ResponseEntity.ok(message);
            } catch (Exception e) {
                System.err.println("发送留言时发生异常: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "发送留言失败: " + e.getMessage()));
            }
        } catch (Exception e) {
            System.err.println("处理留言请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "处理请求失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取家长发送的所有留言列表（分页）
     */
    @GetMapping("/parent-messages")
    public ResponseEntity<?> getParentMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        try {
            System.out.println("接收到获取家长留言请求: page=" + page + ", size=" + size);
            User parent = getCurrentUser(httpRequest);
            if (parent == null) {
                System.out.println("用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户未登录"));
            }
            
            System.out.println("当前家长ID: " + parent.getId() + ", 用户名: " + parent.getUsername());
            
            // 验证家长角色
            if (!hasRole(parent, "PARENT")) {
                System.out.println("用户不是家长: " + parent.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只有家长可以查看自己的留言"));
            }
            
            Page<ParentTeacherMessageDTO> messages = messageService.getMessagesByParent(parent.getId(), page, size);
            System.out.println("查询结果: 总数=" + messages.getTotalElements() + ", 内容数量=" + messages.getContent().size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("获取家长留言异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取留言失败: " + e.getMessage()));
        }
    }
    
    /**
     * 家长标记所有回复为已读
     */
    @PutMapping("/parent-messages/mark-all-replies-read")
    public ResponseEntity<?> markAllRepliesAsRead(HttpServletRequest httpRequest) {
        try {
            User parent = getCurrentUser(httpRequest);
            if (parent == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户未登录"));
            }
            
            // 验证家长角色
            if (!hasRole(parent, "PARENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "只有家长可以标记留言"));
            }
            
            int count = messageService.markAllRepliesAsReadByParent(parent.getId());
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