package com.example.auth.service.impl;

import com.example.auth.mapper.UserMapper;
import com.example.auth.model.entity.User;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.service.UserService;
import com.example.auth.util.FaceRecognitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别服务实现类
 */
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserService userService;
    
    @Override
    public String detectAndProcessFace(String base64Image) {
        try {
            // 去除base64前缀（如果有）
            String imageData = base64Image;
            if (base64Image.contains(",")) {
                imageData = base64Image.split(",")[1];
            }
            
            // 使用OpenCV检测并处理人脸
            String processedFace = FaceRecognitionUtil.detectFace(imageData);
            
            if (processedFace == null) {
                logger.warn("未检测到人脸或处理失败");
                return null;
            }
            
            return processedFace;
        } catch (Exception e) {
            logger.error("人脸检测处理过程中发生异常", e);
            return null;
        }
    }

    @Override
    public boolean verifyFace(Long userId, String capturedFaceData) {
        try {
            // 获取用户存储的人脸数据
            User user = userMapper.selectById(userId);
            if (user == null || user.getFaceData() == null || user.getFaceData().isEmpty()) {
                logger.warn("用户不存在或未录入人脸信息, userId: {}", userId);
                return false;
            }
            
            // 去除base64前缀（如果有）
            String capturedData = capturedFaceData;
            if (capturedFaceData.contains(",")) {
                capturedData = capturedFaceData.split(",")[1];
            }
            
            // 先检测并处理捕获的人脸
            String processedCapturedFace = FaceRecognitionUtil.detectFace(capturedData);
            if (processedCapturedFace == null) {
                logger.warn("未检测到人脸或处理失败");
                return false;
            }
            
            // 比较人脸
            boolean result = FaceRecognitionUtil.compareFaces(user.getFaceData(), processedCapturedFace);
            if (result) {
                logger.info("用户 {} 人脸识别成功", userId);
            } else {
                logger.warn("用户 {} 人脸识别失败", userId);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("人脸验证过程中发生异常", e);
            return false;
        }
    }

    @Override
    public boolean saveFaceData(Long userId, String faceData) {
        try {
            // 获取用户
            User user = userMapper.selectById(userId);
            if (user == null) {
                logger.warn("用户不存在, userId: {}", userId);
                return false;
            }
            
            // 去除base64前缀（如果有）
            String imageData = faceData;
            if (faceData.contains(",")) {
                imageData = faceData.split(",")[1];
            }
            
            // 检测并处理人脸
            String processedFace = FaceRecognitionUtil.detectFace(imageData);
            if (processedFace == null) {
                logger.warn("未检测到人脸或处理失败");
                return false;
            }
            
            // 更新用户人脸数据
            user.setFaceData(processedFace);
            int result = userMapper.updateById(user);
            
            return result > 0;
        } catch (Exception e) {
            logger.error("保存人脸数据过程中发生异常", e);
            return false;
        }
    }
    
    @Override
    public double getFaceSimilarity(Long userId, String capturedFaceData) {
        try {
            // 获取用户存储的人脸数据
            User user = userMapper.selectById(userId);
            if (user == null || user.getFaceData() == null || user.getFaceData().isEmpty()) {
                logger.warn("用户不存在或未录入人脸信息, userId: {}", userId);
                return 0.0;
            }
            
            // 去除base64前缀（如果有）
            String capturedData = capturedFaceData;
            if (capturedFaceData.contains(",")) {
                capturedData = capturedFaceData.split(",")[1];
            }
            
            // 获取相似度
            double similarity = FaceRecognitionUtil.getFaceSimilarity(user.getFaceData(), capturedData);
            logger.info("用户 {} 人脸相似度: {}", userId, similarity);
            
            return similarity;
        } catch (Exception e) {
            logger.error("获取人脸相似度过程中发生异常", e);
            return 0.0;
        }
    }

    @Override
    public Map<String, Object> findMatchingUser(String capturedFaceData) {
        logger.info("开始寻找匹配的用户");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("userId", null);
        result.put("similarity", 0.0);
        
        try {
            // 预处理输入的人脸数据
            String processedData = null;
            
            // 去除base64前缀（如果有）
            String faceData = capturedFaceData;
            if (capturedFaceData.contains(",")) {
                faceData = capturedFaceData.split(",")[1];
            }
            
            // 检测和处理人脸
            try {
                processedData = FaceRecognitionUtil.detectFace(faceData);
                if (processedData == null) {
                    logger.warn("未检测到人脸或处理失败");
                    result.put("message", "未检测到人脸，请调整光线和角度后重试");
                    return result;
                }
            } catch (Exception e) {
                logger.warn("人脸检测过程中发生异常: {}", e.getMessage());
                result.put("message", "人脸检测失败，请稍后重试");
                return result;
            }
            
            // 获取所有有人脸数据的学生
            List<User> studentsWithFaceData;
            try {
                studentsWithFaceData = userService.findAllStudents();
                if (studentsWithFaceData.isEmpty()) {
                    logger.warn("没有找到任何已注册人脸的学生");
                    result.put("message", "系统中暂未录入任何人脸信息，请联系管理员");
                    return result;
                }
            } catch (Exception e) {
                logger.warn("获取学生列表过程中发生异常: {}", e.getMessage());
                result.put("message", "获取用户列表失败，请联系管理员");
                return result;
            }
            
            // 查找最佳匹配
            User bestMatchUser = null;
            double bestSimilarity = 0.0;
            
            try {
                for (User student : studentsWithFaceData) {
                    if (student.getFaceData() != null && !student.getFaceData().isEmpty()) {
                        // 使用getFaceSimilarity方法计算相似度
                        double similarity = FaceRecognitionUtil.getFaceSimilarity(student.getFaceData(), processedData);
                        
                        // 如果当前相似度大于最佳相似度，则更新最佳匹配
                        if (similarity > bestSimilarity) {
                            bestSimilarity = similarity;
                            bestMatchUser = student;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("计算人脸相似度过程中发生异常: {}", e.getMessage());
                result.put("message", "人脸比对失败，请稍后重试");
                return result;
            }
            
            // 设置相似度阈值（例如0.5，表示50%相似）
            if (bestMatchUser != null && bestSimilarity >= 0.5) {
                logger.info("找到匹配的用户，ID：{}，相似度：{}", bestMatchUser.getId(), bestSimilarity);
                result.put("success", true);
                result.put("userId", bestMatchUser.getId());
                result.put("similarity", bestSimilarity);
                result.put("user", bestMatchUser);
            } else {
                logger.warn("未找到相似度超过阈值的用户，最佳相似度：{}", bestSimilarity);
                result.put("message", "系统未找到匹配的人脸信息，请联系管理员录入您的人脸信息后再尝试");
            }
            
            return result;
        } catch (Exception e) {
            logger.error("查找匹配用户过程中发生异常", e);
            result.put("message", "查找匹配用户失败: " + e.getMessage());
            return result;
        }
    }
} 