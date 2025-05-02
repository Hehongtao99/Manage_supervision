package com.example.auth.controller;

import com.example.auth.model.dto.ChatMessageDTO;
import com.example.auth.model.dto.ConversationDTO;
import com.example.auth.model.entity.User;
import com.example.auth.util.UserContext;
import com.example.auth.service.ChatService;
import com.example.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final UserContext userContext;

    public ChatController(ChatService chatService, UserService userService, UserContext userContext) {
        this.chatService = chatService;
        this.userService = userService;
        this.userContext = userContext;
    }

    // REST API 端点

    /**
     * 获取当前用户的所有会话
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.ok(List.of());
        }
        List<ConversationDTO> conversations = chatService.getConversationsForUser(currentUser);
        return ResponseEntity.ok(conversations);
    }

    /**
     * 获取与指定用户的会话
     */
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<ConversationDTO> getConversationWithUser(@PathVariable Long userId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.status(401).body(null);
        }
        
        User otherUser = userService.findById(userId);
        if (otherUser == null) {
            // 目标用户不存在
            return ResponseEntity.notFound().build();
        }
        
        ConversationDTO conversation = chatService.getOrCreateConversation(currentUser, otherUser);
        return ResponseEntity.ok(conversation);
    }

    /**
     * 获取指定会话的消息（分页）
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesForConversation(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 检查用户认证
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.status(401).body(List.of());
        }
        
        List<ChatMessageDTO> messages = chatService.getMessagesForConversation(conversationId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取指定消息之前的消息（加载更多）
     */
    @GetMapping("/conversations/{conversationId}/messages/before/{messageId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesBeforeId(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 检查用户认证
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.status(401).body(List.of());
        }
        
        List<ChatMessageDTO> messages = chatService.getMessagesBeforeId(conversationId, messageId, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 将会话中的所有消息标记为已读
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, Integer>> markConversationAsRead(@PathVariable Long conversationId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.status(401).body(Map.of("updatedCount", 0));
        }
        int updatedCount = chatService.markConversationAsRead(conversationId, currentUser.getId());
        return ResponseEntity.ok(Map.of("updatedCount", updatedCount));
    }

    /**
     * 获取当前用户的未读消息数量
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Integer>> getUnreadMessageCount() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.ok(Map.of("count", 0));
        }
        int count = chatService.getUnreadMessageCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 获取当前用户有未读消息的会话数
     */
    @GetMapping("/unread/conversations/count")
    public ResponseEntity<Map<String, Integer>> getUnreadConversationCount() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.ok(Map.of("count", 0));
        }
        int count = chatService.getUnreadConversationCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // WebSocket 端点

    /**
     * 发送私聊消息
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        try {
            if (principal == null) {
                System.err.println("发送消息失败: Principal为空");
                return;
            }
            
            String principalName = principal.getName();
            System.out.println("处理WebSocket消息发送请求，Principal: " + principalName);
            
            Long senderId = null;
            
            try {
                senderId = Long.valueOf(principalName);
                System.out.println("成功将Principal名称转换为senderId: " + senderId);
                
                // 验证用户ID是否存在
                User sender = userService.findById(senderId);
                if (sender == null) {
                    String errorMsg = "发送者不存在 (ID: " + senderId + ")";
                    System.err.println(errorMsg);
                    chatService.sendErrorNotification(senderId, errorMsg);
                    return;
                }
            } catch (NumberFormatException e) {
                System.err.println("发送消息失败: Principal名称不是有效的用户ID - " + principalName + ", 异常: " + e.getMessage());
                return;
            }
            
            if (payload == null) {
                System.err.println("发送消息失败: 载荷为空");
                return;
            }
            
            Object recipientIdObj = payload.get("recipientId");
            Object contentObj = payload.get("content");
            
            if (recipientIdObj == null) {
                System.err.println("发送消息失败: 缺少接收者ID");
                chatService.sendErrorNotification(senderId, "缺少接收者ID");
                return;
            }
            
            if (contentObj == null) {
                System.err.println("发送消息失败: 缺少消息内容");
                chatService.sendErrorNotification(senderId, "缺少消息内容");
                return;
            }
            
            Long recipientId;
            try {
                recipientId = Long.valueOf(recipientIdObj.toString());
                
                // 验证接收者ID是否存在
                User recipient = userService.findById(recipientId);
                if (recipient == null) {
                    String errorMsg = "接收者不存在 (ID: " + recipientId + ")";
                    System.err.println(errorMsg);
                    chatService.sendErrorNotification(senderId, errorMsg);
                    return;
                }
            } catch (NumberFormatException e) {
                String errorMsg = "接收者ID不是有效的数字 - " + recipientIdObj;
                System.err.println(errorMsg + ", 异常: " + e.getMessage());
                chatService.sendErrorNotification(senderId, errorMsg);
                return;
            }
            
            String content = contentObj.toString();
            
            // 检查是否有文件信息
            String fileUrl = payload.get("fileUrl") != null ? payload.get("fileUrl").toString() : null;
            String fileName = payload.get("fileName") != null ? payload.get("fileName").toString() : null;
            String fileType = payload.get("fileType") != null ? payload.get("fileType").toString() : null;
            Long fileSize = payload.get("fileSize") != null ? Long.valueOf(payload.get("fileSize").toString()) : null;
            
            System.out.println("准备通过WebSocket发送消息，发送者ID: " + senderId + ", 接收者ID: " + recipientId);
            
            try {
                if (fileUrl != null) {
                    // 发送文件消息
                    chatService.sendMessage(senderId, recipientId, content, fileUrl, fileName, fileType, fileSize);
                } else {
                    // 发送普通文本消息
                    chatService.sendMessage(senderId, recipientId, content);
                }
            } catch (RuntimeException e) {
                System.err.println("发送消息时服务层抛出异常: " + e.getMessage());
                chatService.sendErrorNotification(senderId, e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            System.err.println("WebSocket消息处理发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 接收Web API发送的消息
     */
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessageRest(
            @RequestBody Map<String, Object> payload
    ) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            // 用户未认证或获取用户信息失败
            return ResponseEntity.status(401).build();
        }
        
        Long recipientId = Long.valueOf(payload.get("recipientId").toString());
        String content = payload.get("content") != null ? payload.get("content").toString() : "";
        
        // 检查是否有文件信息
        String fileUrl = payload.get("fileUrl") != null ? payload.get("fileUrl").toString() : null;
        String fileName = payload.get("fileName") != null ? payload.get("fileName").toString() : null;
        String fileType = payload.get("fileType") != null ? payload.get("fileType").toString() : null;
        Long fileSize = payload.get("fileSize") != null ? Long.valueOf(payload.get("fileSize").toString()) : null;
        
        ChatMessageDTO message;
        if (fileUrl != null) {
            // 发送文件消息
            message = chatService.sendMessage(currentUser.getId(), recipientId, content, fileUrl, fileName, fileType, fileSize);
        } else {
            // 发送普通文本消息
            message = chatService.sendMessage(currentUser.getId(), recipientId, content);
        }
        
        return ResponseEntity.ok(message);
    }

    /**
     * 连接用户
     */
    @MessageMapping("/chat.connect")
    public void connect(SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        // 用户已在WebSocketConfig中通过JWT验证并设置了principal
        try {
            if (principal != null) {
                System.out.println("用户连接WebSocket，Principal: " + principal.getName());
            } else {
                System.err.println("用户连接WebSocket，但Principal为空");
            }
        } catch (Exception e) {
            System.err.println("处理WebSocket连接时发生异常: " + e.getMessage());
        }
    }
} 