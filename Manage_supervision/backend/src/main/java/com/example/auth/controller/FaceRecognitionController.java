package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.entity.User;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸识别控制器
 */
@RestController
@RequestMapping("/api/face")
public class FaceRecognitionController {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionController.class);
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 上传并注册人脸（文件上传方式）
     */
    @PostMapping("/register")
    @RequireRole("USER")
    public ResponseEntity<?> registerFace(
            @RequestHeader("Authorization") String auth,
            @RequestParam("faceImage") MultipartFile faceImage) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            boolean success = faceRecognitionService.registerFace(user.getId(), faceImage);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "人脸注册成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸注册失败，请确保图像中有且只有一个清晰的人脸"
                ));
            }
        } catch (Exception e) {
            logger.error("人脸注册失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "人脸注册失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 上传并注册人脸（Base64方式）
     */
    @PostMapping("/register/base64")
    @RequireRole("USER")
    public ResponseEntity<?> registerFaceWithBase64(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, String> request) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            String base64Image = request.get("faceImage");
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸图像数据不能为空"
                ));
            }
            
            boolean success = faceRecognitionService.registerFaceWithBase64(user.getId(), base64Image);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "人脸注册成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸注册失败，请确保图像中有且只有一个清晰的人脸"
                ));
            }
        } catch (Exception e) {
            logger.error("人脸注册失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "人脸注册失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 人脸登录（文件上传方式）
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginWithFace(
            @RequestParam("faceImage") MultipartFile faceImage) {
        try {
            Long userId = faceRecognitionService.verifyUserByFace(faceImage);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸识别失败，未找到匹配的用户"
                ));
            }
            
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            // 检查用户状态，禁止被禁用的用户登录
            if ("inactive".equals(user.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "账号已被禁用，请联系管理员"
                ));
            }
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            
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
        } catch (Exception e) {
            logger.error("人脸登录失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "人脸登录失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 人脸登录（Base64方式）
     */
    @PostMapping("/login/base64")
    public ResponseEntity<?> loginWithFaceBase64(
            @RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("faceImage");
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸图像数据不能为空"
                ));
            }
            
            Long userId = faceRecognitionService.verifyUserByFaceWithBase64(base64Image);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸识别失败，未找到匹配的用户"
                ));
            }
            
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            // 检查用户状态，禁止被禁用的用户登录
            if ("inactive".equals(user.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "账号已被禁用，请联系管理员"
                ));
            }
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            
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
        } catch (Exception e) {
            logger.error("人脸登录失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "人脸登录失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除用户人脸数据
     */
    @DeleteMapping("/delete")
    @RequireRole("USER")
    public ResponseEntity<?> deleteFace(
            @RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            boolean success = faceRecognitionService.deleteFace(user.getId());
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "人脸数据删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸数据删除失败"
                ));
            }
        } catch (Exception e) {
            logger.error("删除人脸数据失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "删除人脸数据失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 检查用户是否已注册人脸
     */
    @GetMapping("/status")
    @RequireRole("USER")
    public ResponseEntity<?> checkFaceStatus(
            @RequestHeader("Authorization") String auth) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            boolean hasFaceRegistered = faceRecognitionService.hasFaceRegistered(user.getId());
            
            return ResponseEntity.ok(Map.of(
                "hasFaceRegistered", hasFaceRegistered
            ));
        } catch (Exception e) {
            logger.error("检查人脸注册状态失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "检查人脸注册状态失败: " + e.getMessage()
            ));
        }
    }
} 