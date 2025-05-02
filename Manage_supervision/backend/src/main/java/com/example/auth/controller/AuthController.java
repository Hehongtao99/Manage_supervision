package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.ChangePasswordRequest;
import com.example.auth.model.entity.User;
import com.example.auth.model.entity.Role;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserContext userContext;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            User user = userService.register(username, password);
            return ResponseEntity.ok(Map.of(
                "message", "注册成功",
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userService.findByUsername(username);
        if (user == null || !userService.validatePassword(user, password)) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "用户名或密码错误"
            ));
        }
        
        // 检查用户状态，禁止被禁用的用户登录
        if ("inactive".equals(user.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "账号已被禁用，请联系管理员"
            ));
        }

        // 使用新的generateToken方法，包含用户ID
        String token = jwtUtil.generateToken(username, user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("avatar", user.getAvatar());
        userData.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
        
        userData.put("realName", user.getRealName());
        userData.put("nickname", user.getNickname());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("bio", user.getBio());
        userData.put("userNumber", user.getUserNumber());
        
        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            // 验证token是否有效
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body(Map.of(
                    "message", "无效的token或token已过期"
                ));
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "message", "用户不存在"
                ));
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("avatar", user.getAvatar());
            userData.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
            
            userData.put("realName", user.getRealName());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            userData.put("phone", user.getPhone());
            userData.put("bio", user.getBio());
            userData.put("userNumber", user.getUserNumber());
            
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "message", "获取用户信息失败: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/change-password")
    @RequireRole("USER")
    public ResponseEntity<?> changePassword(
        @RequestHeader("Authorization") String auth,
        @RequestBody ChangePasswordRequest request
    ) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
            
            return ResponseEntity.ok(Map.of(
                "message", "密码修改成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/admin")
    @RequireRole("ADMIN")
    public ResponseEntity<?> adminOnly() {
        return ResponseEntity.ok(Map.of(
            "message", "只有管理员才能看到这个信息"
        ));
    }

    /**
     * 获取当前用户的权限信息
     * @return 权限列表
     */
    @GetMapping("/permissions")
    public ResponseEntity<?> getUserPermissions() {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        
        // 获取用户所有角色的权限
        Set<String> permissions = new HashSet<>();
        for (Role role : currentUser.getRoles()) {
            // 角色权限是以逗号分隔的字符串，需要转换为列表
            if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
                String[] rolePermissions = role.getPermissions().split(",");
                for (String permission : rolePermissions) {
                    permissions.add(permission.trim());
                }
            }
        }
        
        // 如果用户是管理员，添加所有权限
        if (currentUser.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))) {
            permissions.addAll(Arrays.asList(
                "USER_VIEW", "USER_EDIT", "USER_DELETE",
                "ROLE_VIEW", "ROLE_EDIT", "ROLE_DELETE",
                "LOG_VIEW", "SYSTEM_SETTINGS"
            ));
        }
        
        return ResponseEntity.ok(permissions.toArray(new String[0]));
    }
} 