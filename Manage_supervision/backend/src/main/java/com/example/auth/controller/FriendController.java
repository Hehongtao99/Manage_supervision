package com.example.auth.controller;

import com.example.auth.model.entity.User;
import com.example.auth.service.FriendService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友关系管理控制器
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    
    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);
    
    private final FriendService friendService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword, 
                                       @RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户未登录"));
            }
            
            List<Map<String, Object>> users = friendService.searchUsers(keyword, currentUser.getId());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("搜索用户失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "搜索用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取好友列表
     */
    @GetMapping
    public ResponseEntity<?> getFriends(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户未登录"));
            }
            
            List<Map<String, Object>> friends = friendService.getFriendsByUserId(currentUser.getId());
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            logger.error("获取好友列表失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取好友列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送好友请求
     */
    @PostMapping("/requests")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, Object> requestBody,
                                            @RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户未登录"));
            }
            
            Long toUserId = Long.parseLong(requestBody.get("toUserId").toString());
            String message = (String) requestBody.get("message");
            
            boolean success = friendService.sendFriendRequest(currentUser.getId(), toUserId, message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                response.put("message", "发送好友请求失败");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("发送好友请求失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送好友请求失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 处理好友请求
     */
    @PutMapping("/requests/{requestId}")
    public ResponseEntity<?> processFriendRequest(@PathVariable Long requestId,
                                               @RequestParam boolean accept,
                                               @RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户未登录"));
            }
            
            boolean success = friendService.processFriendRequest(requestId, currentUser.getId(), accept);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                response.put("message", "处理好友请求失败");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("处理好友请求失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "处理好友请求失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取收到的好友请求
     */
    @GetMapping("/requests/received")
    public ResponseEntity<?> getReceivedRequests(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户未登录"));
            }
            
            return ResponseEntity.ok(friendService.getReceivedRequests(currentUser.getId()));
        } catch (Exception e) {
            logger.error("获取收到的好友请求失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取收到的好友请求失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取发送的好友请求
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<?> getSentRequests(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户未登录"));
            }
            
            return ResponseEntity.ok(friendService.getSentRequests(currentUser.getId()));
        } catch (Exception e) {
            logger.error("获取发送的好友请求失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取发送的好友请求失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除好友
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable Long friendId,
                                        @RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户未登录"));
            }
            
            boolean success = friendService.deleteFriend(currentUser.getId(), friendId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                response.put("message", "删除好友失败");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除好友失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除好友失败: " + e.getMessage()
            ));
        }
    }
} 