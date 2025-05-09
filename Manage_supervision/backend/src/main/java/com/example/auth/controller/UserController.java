package com.example.auth.controller;

import com.example.auth.model.entity.User;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    // 设置最大头像大小限制为2MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7); // 去除"Bearer "前缀
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 检查文件大小限制
            if (file.getSize() > MAX_AVATAR_SIZE) {
                logger.error("头像上传失败：文件大小超过限制 - 用户: {}, 文件大小: {}", 
                            username, file.getSize());
                return ResponseEntity.badRequest().body(Map.of("message", "头像大小不能超过2MB"));
            }
            
            // 检查文件类型限制
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                logger.error("头像上传失败：文件类型不支持 - 用户: {}, 文件类型: {}", 
                            username, contentType);
                return ResponseEntity.badRequest().body(Map.of("message", "头像只能是JPG或PNG格式"));
            }

            // 确保上传目录存在
            String directory = new File(uploadDir).getAbsolutePath();
            Path uploadPath = Paths.get(directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 更新用户头像URL
            String avatarUrl = "/uploads/" + filename;
            
            // 调用UserService保存头像URL
            userService.updateAvatar(user, avatarUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("url", avatarUrl);
            response.put("message", "头像上传成功");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "头像上传失败: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData,
                                          @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }

            // 调用服务层保存个人信息
            User updatedUser = userService.updateProfile(user, profileData);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", updatedUser.getId());
            userData.put("username", updatedUser.getUsername());
            userData.put("realName", updatedUser.getRealName());
            userData.put("nickname", updatedUser.getNickname());
            userData.put("email", updatedUser.getEmail());
            userData.put("phone", updatedUser.getPhone());
            userData.put("bio", updatedUser.getBio());
            userData.put("avatar", updatedUser.getAvatar());
            userData.put("graduationSchool", updatedUser.getGraduationSchool());
            userData.put("teachingSubjects", updatedUser.getTeachingSubjects());
            userData.put("roles", updatedUser.getRoles().stream().map(role -> role.getName()).toList());
            
            response.put("user", userData);
            response.put("message", "个人信息更新成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "个人信息更新失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/supervisors")
    public ResponseEntity<?> getAllSupervisors() {
        try {
            List<User> supervisors = userService.getAllSupervisors();
            // 转换为只包含 ID 和姓名的简单对象列表
            List<Map<String, Object>> result = supervisors.stream().map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", s.getId());
                map.put("name", s.getRealName() != null ? s.getRealName() : s.getUsername());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取督导员列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取督导员列表失败: " + e.getMessage()));
        }
    }
}