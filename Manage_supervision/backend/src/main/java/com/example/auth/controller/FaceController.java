package com.example.auth.controller;

import com.example.auth.model.entity.User;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸识别控制器
 */
@RestController
@RequestMapping("/api/face")
public class FaceController {

    private static final Logger logger = LoggerFactory.getLogger(FaceController.class);
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 检测人脸
     */
    @PostMapping("/detect")
    public ResponseEntity<?> detectFace(@RequestBody Map<String, String> requestBody) {
        try {
            if (!requestBody.containsKey("image")) {
                return ResponseEntity.badRequest().body(Map.of("message", "未提供图像数据"));
            }
            
            String imageBase64 = requestBody.get("image");
            String processedFace = faceRecognitionService.detectAndProcessFace(imageBase64);
            
            if (processedFace == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "未检测到人脸或处理失败"
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "人脸检测成功");
            response.put("faceData", processedFace);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("人脸检测失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "人脸检测失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 保存用户人脸数据
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerFace(@RequestBody Map<String, String> requestBody,
                                       @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7); // 去除"Bearer "前缀
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            // 检查请求参数
            if (!requestBody.containsKey("faceData")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "人脸数据不能为空"
                ));
            }
            
            String faceData = requestBody.get("faceData");
            
            // 保存人脸数据
            boolean saved = faceRecognitionService.saveFaceData(user.getId(), faceData);
            
            if (saved) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "人脸信息注册成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "人脸信息注册失败，请确保图像中包含清晰的人脸"
                ));
            }
        } catch (Exception e) {
            logger.error("注册人脸信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "注册人脸信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证人脸
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyFace(@RequestBody Map<String, Object> requestBody) {
        try {
            // 验证请求参数
            if (!requestBody.containsKey("userId") || !requestBody.containsKey("faceData")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户ID和人脸数据不能为空"
                ));
            }
            
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            String faceData = (String) requestBody.get("faceData");
            
            // 执行人脸验证
            boolean verified = faceRecognitionService.verifyFace(userId, faceData);
            
            if (verified) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "verified", true,
                    "message", "人脸验证通过"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "verified", false,
                    "message", "人脸验证失败，请确保光线良好并正对摄像头"
                ));
            }
        } catch (Exception e) {
            logger.error("人脸验证失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "verified", false,
                "message", "人脸验证过程中发生错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 检查用户是否已注册人脸数据
     */
    @GetMapping("/status")
    public ResponseEntity<?> faceStatus(@RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7); // 去除"Bearer "前缀
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            boolean hasFaceData = userService.hasFaceData(user.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "registered", hasFaceData,
                "message", hasFaceData ? "用户已注册人脸信息" : "用户未注册人脸信息"
            ));
        } catch (Exception e) {
            logger.error("查询人脸注册状态失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询人脸注册状态失败: " + e.getMessage()
            ));
        }
    }
} 