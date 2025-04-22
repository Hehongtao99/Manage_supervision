package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.entity.User;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

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
            
            return ResponseEntity.ok(Map.of(
                "user", userData
            ));
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
} 