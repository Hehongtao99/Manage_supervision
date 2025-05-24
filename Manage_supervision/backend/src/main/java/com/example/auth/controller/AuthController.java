package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.ChangePasswordRequest;
import com.example.auth.model.entity.Permission;
import com.example.auth.model.entity.User;
import com.example.auth.service.PermissionService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserContext userContext;
    
    @Autowired
    private PermissionService permissionService;

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
        
        // 加载用户权限
        currentUser = userService.loadUserPermissions(currentUser);
        
        // 获取用户所有权限编码
        Set<String> permissions = new HashSet<>();
        if (currentUser.getPermissions() != null) {
            permissions.addAll(currentUser.getPermissions().stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toSet()));
        }
        
        // 根据角色添加对应的权限
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        
        boolean isSupervisor = currentUser.getRoles().stream()
                .anyMatch(role -> "SUPERVISOR".equalsIgnoreCase(role.getName()));
        
        // 如果用户是管理员，添加所有权限
        if (isAdmin) {
            permissions.addAll(Arrays.asList(
                "USER_VIEW", "USER_EDIT", "USER_DELETE", "USER_ADD",
                "ROLE_VIEW", "ROLE_EDIT", "ROLE_DELETE", "ROLE_ADD",
                "LOG_VIEW", "SYSTEM_SETTINGS",
                "user:view", "user:edit", "user:delete", "user:add",
                "role:view", "role:edit", "role:delete", "role:add",
                "system", "dashboard",
                "STUDENT_VIEW", "STUDENT_EDIT", "STUDENT_DELETE", "STUDENT_ADD",
                "student:view", "student:edit", "student:delete", "student:add",
                "TEACHER_VIEW", "TEACHER_EDIT", "TEACHER_DELETE", "TEACHER_ADD",
                "teacher:view", "teacher:edit", "teacher:delete", "teacher:add"
            ));
        }
        
        // 如果用户是督导员，添加学生管理权限
        if (isSupervisor) {
            permissions.addAll(Arrays.asList(
                "STUDENT_VIEW", "student:view",
                "STUDENT_EDIT", "student:edit"
            ));
        }
        
        // 确保所有权限都有新旧两种格式
        Set<String> additionalPermissions = new HashSet<>();
        
        for (String permission : permissions) {
            // 如果是旧格式(大写带下划线)，添加新格式
            if (permission.contains("_")) {
                String newFormat = permission.toLowerCase().replace("_", ":");
                additionalPermissions.add(newFormat);
            } 
            // 如果是新格式(小写带冒号)，添加旧格式
            else if (permission.contains(":")) {
                String oldFormat = permission.toUpperCase().replace(":", "_");
                additionalPermissions.add(oldFormat);
            }
        }
        
        // 添加额外的权限
        permissions.addAll(additionalPermissions);
        
        // 打印调试信息
        System.out.println("用户 " + currentUser.getUsername() + " 的权限: " + String.join(", ", permissions));
        
        return ResponseEntity.ok(permissions.toArray(new String[0]));
    }
} 