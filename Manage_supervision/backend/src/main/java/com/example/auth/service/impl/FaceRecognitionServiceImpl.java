package com.example.auth.service.impl;

import com.example.auth.mapper.UserMapper;
import com.example.auth.model.entity.User;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.util.FaceRecognitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 人脸识别服务实现类
 */
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
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
} 