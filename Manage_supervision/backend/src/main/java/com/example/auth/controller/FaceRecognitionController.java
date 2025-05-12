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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @RequireRole({"USER", "SUPERVISOR"})
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
     * 上传并注册多特征人脸（文件上传方式）
     */
    @PostMapping("/register/multi")
    @RequireRole({"USER", "SUPERVISOR"})
    public ResponseEntity<?> registerMultiFace(
            @RequestHeader("Authorization") String auth,
            @RequestParam("faceImage") MultipartFile faceImage,
            @RequestParam("featureType") String featureType,
            @RequestParam("description") String description) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            boolean success = faceRecognitionService.registerFace(user.getId(), faceImage, featureType, description);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", featureType + "类型人脸特征注册成功"
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
    @RequireRole({"USER", "SUPERVISOR"})
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
     * 上传并注册多特征人脸（Base64方式）
     */
    @PostMapping("/register/base64/multi")
    @RequireRole({"USER", "SUPERVISOR"})
    public ResponseEntity<?> registerMultiFaceWithBase64(
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
            
            String featureType = request.get("featureType");
            if (featureType == null || featureType.isEmpty()) {
                featureType = "primary";
            }
            
            String description = request.get("description");
            if (description == null || description.isEmpty()) {
                description = featureType + "人脸特征";
            }
            
            boolean success = faceRecognitionService.registerFaceWithBase64(
                    user.getId(), base64Image, featureType, description);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", featureType + "类型人脸特征注册成功"
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
     * 获取用户已注册的人脸特征列表
     */
    @GetMapping("/features")
    @RequireRole({"USER", "SUPERVISOR"})
    public ResponseEntity<?> getUserFaceFeatures(
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
            
            List<Map<String, Object>> features = faceRecognitionService.getUserFaceFeatures(user.getId());
            
            return ResponseEntity.ok(Map.of(
                "features", features
            ));
        } catch (Exception e) {
            logger.error("获取用户人脸特征失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "获取用户人脸特征失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除用户指定类型的人脸特征
     */
    @DeleteMapping("/feature/{featureType}")
    @RequireRole({"USER", "SUPERVISOR"})
    public ResponseEntity<?> deleteUserFaceFeature(
            @RequestHeader("Authorization") String auth,
            @PathVariable("featureType") String featureType) {
        try {
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户不存在"
                ));
            }
            
            boolean success = faceRecognitionService.deleteFace(user.getId(), featureType);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", featureType + "类型人脸特征删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "删除人脸特征失败"
                ));
            }
        } catch (Exception e) {
            logger.error("删除用户人脸特征失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "删除用户人脸特征失败: " + e.getMessage()
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
            
            return loginUserById(userId);
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
            
            // 先检查是否能够从图像中提取到人脸
            byte[] faceFeatures = null;
            try {
                faceFeatures = faceRecognitionService.extractFaceFeaturesFromBase64(base64Image);
                if (faceFeatures == null) {
                    logger.warn("未检测到人脸");
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "未能检测到人脸，请确保面部在摄像头范围内并有足够光线"
                    ));
                }
            } catch (Exception e) {
                logger.error("提取人脸特征失败", e);
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸特征提取失败: " + e.getMessage()
                ));
            }
            
            // 获取匹配的用户ID列表
            List<Long> matchingUserIds = faceRecognitionService.findMatchingUsersByFaceWithBase64(base64Image);
            
            if (matchingUserIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸识别失败，未找到匹配的用户"
                ));
            }
            
            // 如果只有一个匹配的用户，直接登录
            if (matchingUserIds.size() == 1) {
                Long userId = matchingUserIds.get(0);
                return loginUserById(userId);
            } else {
                // 如果有多个匹配的用户，返回用户列表供选择
                List<Map<String, Object>> userList = new ArrayList<>();
                // 使用Set去重，避免重复的用户ID
                Set<Long> uniqueUserIds = new HashSet<>(matchingUserIds);
                
                logger.info("检测到多个匹配用户，去重前: {}个，去重后: {}个", 
                            matchingUserIds.size(), uniqueUserIds.size());
                
                for (Long userId : uniqueUserIds) {
                    User user = userService.findById(userId);
                    if (user != null && !"inactive".equals(user.getStatus())) {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("realName", user.getRealName());
                        userInfo.put("userNumber", user.getUserNumber());
                        userInfo.put("avatar", user.getAvatar());
                        userInfo.put("roles", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));
                        userList.add(userInfo);
                    }
                }
                
                if (userList.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "所有匹配的用户账号均已被禁用"
                    ));
                }
                
                return ResponseEntity.ok(Map.of(
                    "multipleUsers", true,
                    "users", userList
                ));
            }
        } catch (Exception e) {
            logger.error("人脸登录失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "人脸登录失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 增强人脸登录（多特征匹配，Base64方式）
     */
    @PostMapping("/login/enhanced")
    public ResponseEntity<?> enhancedLoginWithFace(@RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("faceImage");
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸图像数据不能为空"
                ));
            }
            
            // 获取匹配策略，默认为"best"（最佳匹配）
            String matchStrategy = request.get("matchStrategy");
            if (matchStrategy == null || matchStrategy.isEmpty()) {
                matchStrategy = "best";
            }
            
            // 验证策略是否有效
            if (!matchStrategy.equals("best") && !matchStrategy.equals("vote") && !matchStrategy.equals("average")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "无效的匹配策略，支持的策略有：best, vote, average"
                ));
            }
            
            // 先检查是否能够从图像中提取到人脸
            byte[] faceFeatures = null;
            try {
                faceFeatures = faceRecognitionService.extractFaceFeaturesFromBase64(base64Image);
                if (faceFeatures == null) {
                    logger.warn("未检测到人脸");
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "未能检测到人脸，请确保面部在摄像头范围内并有足够光线"
                    ));
                }
            } catch (Exception e) {
                logger.error("提取人脸特征失败", e);
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸特征提取失败: " + e.getMessage()
                ));
            }
            
            // 使用增强的多特征匹配方法
            Map<Long, Double> matchResults = faceRecognitionService.verifyUserByFaceEnhanced(base64Image, matchStrategy);
            
            if (matchResults.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "人脸识别失败，未找到匹配的用户"
                ));
            }
            
            // 获取匹配分数最高的用户ID
            Map.Entry<Long, Double> bestMatch = matchResults.entrySet().iterator().next();
            Long bestMatchUserId = bestMatch.getKey();
            Double bestMatchScore = bestMatch.getValue();
            
            // 如果只有一个匹配的用户，且匹配分数超过阈值，直接登录
            if (matchResults.size() == 1 && bestMatchScore >= 0.9) {
                return loginUserById(bestMatchUserId);
            } else {
                // 如果有多个匹配的用户或匹配分数不够高，返回用户列表供选择
                List<Map<String, Object>> userList = new ArrayList<>();
                
                for (Map.Entry<Long, Double> entry : matchResults.entrySet()) {
                    Long userId = entry.getKey();
                    Double score = entry.getValue();
                    
                    User user = userService.findById(userId);
                    if (user != null && !"inactive".equals(user.getStatus())) {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("realName", user.getRealName());
                        userInfo.put("userNumber", user.getUserNumber());
                        userInfo.put("avatar", user.getAvatar());
                        userInfo.put("matchScore", String.format("%.2f%%", score * 100));
                        userInfo.put("roles", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));
                        userList.add(userInfo);
                    }
                }
                
                if (userList.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "所有匹配的用户账号均已被禁用"
                    ));
                }
                
                return ResponseEntity.ok(Map.of(
                    "multipleUsers", true,
                    "matchStrategy", matchStrategy,
                    "users", userList
                ));
            }
        } catch (Exception e) {
            logger.error("增强人脸登录失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "人脸登录失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 根据用户ID登录（人脸识别后选择用户）
     */
    @PostMapping("/login/select-user")
    public ResponseEntity<?> loginSelectedUser(@RequestBody Map<String, Long> request) {
        try {
            Long userId = request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "用户ID不能为空"
                ));
            }
            
            return loginUserById(userId);
        } catch (Exception e) {
            logger.error("用户选择登录失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "登录失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 根据用户ID执行登录流程
     */
    private ResponseEntity<?> loginUserById(Long userId) {
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
    }
    
    /**
     * 检查用户是否已注册人脸
     */
    @GetMapping("/check")
    @RequireRole({"USER", "SUPERVISOR"})
    public ResponseEntity<?> checkFaceRegistered(
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