package com.example.auth.service.impl;

import com.example.auth.entity.UserFaceFeature;
import com.example.auth.repository.UserFaceFeatureRepository;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.util.FaceRecognitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * 人脸识别服务实现类
 */
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionServiceImpl.class);
    
    // 添加人脸匹配阈值常量
    private static final double FACE_MATCH_THRESHOLD = 0.80; // 80%的匹配度
    
    @Autowired
    private UserFaceFeatureRepository userFaceFeatureRepository;
    
    @Autowired
    private FaceRecognitionUtil faceRecognitionUtil;
    
    @Override
    @Transactional
    public boolean registerFace(Long userId, MultipartFile faceImage) {
        try {
            // 提取人脸特征
            byte[] faceFeatures = faceRecognitionUtil.extractFaceFeaturesFromFile(faceImage);
            if (faceFeatures == null) {
                logger.error("提取人脸特征失败，未检测到人脸或检测到多个人脸");
                return false;
            }
            logger.info("提取到的人脸特征大小: {} bytes", faceFeatures.length);
            
            // 提取人脸图像
            byte[] faceImageBytes = faceRecognitionUtil.extractFaceImageFromFile(faceImage);
            if (faceImageBytes == null) {
                logger.error("提取人脸图像失败，未检测到人脸或检测到多个人脸");
                return false;
            }
            logger.info("提取到的人脸图像大小: {} bytes", faceImageBytes.length);
            
            // 创建或更新用户人脸特征
            UserFaceFeature userFaceFeature = new UserFaceFeature();
            userFaceFeature.setUserId(userId);
            userFaceFeature.setFaceFeature(faceFeatures);
            userFaceFeature.setFaceImage(faceImageBytes);
            
            userFaceFeatureRepository.save(userFaceFeature);
            
            logger.info("用户 {} 的人脸注册成功", userId);
            return true;
        } catch (Exception e) {
            logger.error("用户 {} 的人脸注册失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean registerFaceWithBase64(Long userId, String base64Image) {
        try {
            // 提取人脸特征
            byte[] faceFeatures = faceRecognitionUtil.extractFaceFeaturesFromBase64(base64Image);
            if (faceFeatures == null) {
                logger.error("提取人脸特征失败，未检测到人脸或检测到多个人脸");
                return false;
            }
             logger.info("提取到的人脸特征大小: {} bytes", faceFeatures.length);
            
            // 提取人脸图像
            byte[] faceImageBytes = faceRecognitionUtil.extractFaceImageFromBase64(base64Image);
            if (faceImageBytes == null) {
                logger.error("提取人脸图像失败，未检测到人脸或检测到多个人脸");
                return false;
            }
            logger.info("提取到的人脸图像大小: {} bytes", faceImageBytes.length);
            
            // 创建或更新用户人脸特征
            UserFaceFeature userFaceFeature = new UserFaceFeature();
            userFaceFeature.setUserId(userId);
            userFaceFeature.setFaceFeature(faceFeatures);
            userFaceFeature.setFaceImage(faceImageBytes);
            
            userFaceFeatureRepository.save(userFaceFeature);
            
            logger.info("用户 {} 的人脸注册成功", userId);
            return true;
        } catch (Exception e) {
            logger.error("用户 {} 的人脸注册失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Long verifyUserByFace(MultipartFile faceImage) {
        try {
            // 提取人脸特征
            byte[] faceFeatures = faceRecognitionUtil.extractFaceFeaturesFromFile(faceImage);
            if (faceFeatures == null) {
                logger.error("提取人脸特征失败，未检测到人脸或检测到多个人脸");
                return null;
            }
            
            // 获取所有用户的人脸特征
            List<UserFaceFeature> allUserFaces = userFaceFeatureRepository.findAll();
            
            logger.info("开始人脸比对，已注册用户数量: {}", allUserFaces.size());
            
            // 循环比对，寻找匹配的用户
            for (UserFaceFeature userFace : allUserFaces) {
                byte[] storedFeatures = userFace.getFaceFeature();
                if (storedFeatures != null && faceRecognitionUtil.isFaceMatched(storedFeatures, faceFeatures)) {
                    logger.info("人脸匹配成功，用户ID: {}", userFace.getUserId());
                    return userFace.getUserId();
                }
            }
            
            logger.warn("未找到匹配的人脸");
            return null;
        } catch (Exception e) {
            logger.error("人脸验证失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public Long verifyUserByFaceWithBase64(String base64Image) {
        try {
            // 提取人脸特征
            byte[] faceFeatures = faceRecognitionUtil.extractFaceFeaturesFromBase64(base64Image);
            if (faceFeatures == null) {
                logger.error("提取人脸特征失败，未检测到人脸或检测到多个人脸");
                return null;
            }
            
            // 获取所有用户的人脸特征
            List<UserFaceFeature> allUserFaces = userFaceFeatureRepository.findAll();
            
            logger.info("开始人脸比对，已注册用户数量: {}", allUserFaces.size());
            
            // 循环比对，寻找匹配的用户
            for (UserFaceFeature userFace : allUserFaces) {
                byte[] storedFeatures = userFace.getFaceFeature();
                if (storedFeatures != null && faceRecognitionUtil.isFaceMatched(storedFeatures, faceFeatures)) {
                    logger.info("人脸匹配成功，用户ID: {}", userFace.getUserId());
                    return userFace.getUserId();
                }
            }
            
            logger.warn("未找到匹配的人脸");
            return null;
        } catch (Exception e) {
            logger.error("人脸验证失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<Long> findMatchingUsersByFaceWithBase64(String base64Image) {
        try {
            // 提取人脸特征
            byte[] faceFeatures = faceRecognitionUtil.extractFaceFeaturesFromBase64(base64Image);
            if (faceFeatures == null) {
                logger.error("提取人脸特征失败，未检测到人脸或检测到多个人脸");
                return new ArrayList<>();
            }
            
            // 获取所有用户的人脸特征
            List<UserFaceFeature> allUserFaces = userFaceFeatureRepository.findAll();
            
            logger.info("开始人脸比对，已注册用户数量: {}", allUserFaces.size());
            
            // 存储匹配的用户ID
            List<Long> matchingUserIds = new ArrayList<>();
            
            // 循环比对，寻找所有匹配的用户
            for (UserFaceFeature userFace : allUserFaces) {
                byte[] storedFeatures = userFace.getFaceFeature();
                if (storedFeatures != null) {
                    double similarity = faceRecognitionUtil.calculateSimilarity(storedFeatures, faceFeatures);
                    if (similarity >= FACE_MATCH_THRESHOLD) {
                        logger.info("人脸匹配成功，用户ID: {}, 相似度: {}", userFace.getUserId(), similarity);
                        matchingUserIds.add(userFace.getUserId());
                    }
                }
            }
            
            if (matchingUserIds.isEmpty()) {
                logger.warn("未找到匹配的人脸");
            } else {
                logger.info("找到 {} 个匹配的用户", matchingUserIds.size());
            }
            
            return matchingUserIds;
        } catch (Exception e) {
            logger.error("人脸验证失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional
    public boolean deleteFace(Long userId) {
        try {
            userFaceFeatureRepository.deleteByUserId(userId);
            logger.info("用户 {} 的人脸数据删除成功", userId);
            return true;
        } catch (Exception e) {
            logger.error("删除用户 {} 的人脸数据失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean hasFaceRegistered(Long userId) {
        try {
            Optional<UserFaceFeature> faceFeature = userFaceFeatureRepository.findTopByUserIdOrderByUpdateTimeDesc(userId);
            return faceFeature.isPresent();
        } catch (Exception e) {
            logger.error("检查用户 {} 是否注册人脸失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public byte[] extractFaceFeaturesFromBase64(String base64Image) throws Exception {
        try {
            if (base64Image == null || base64Image.isEmpty()) {
                logger.warn("提取人脸特征失败：Base64图像为空");
                return null;
            }
            
            // 使用工具类提取人脸特征
            byte[] faceFeatures = faceRecognitionUtil.extractFaceFeaturesFromBase64(base64Image);
            
            if (faceFeatures == null) {
                logger.warn("提取人脸特征失败：未检测到人脸");
                return null;
            }
            
            // 记录特征长度，作为调试信息
            logger.info("成功提取人脸特征，特征长度: {} 字节", faceFeatures.length);
            return faceFeatures;
        } catch (Exception e) {
            logger.error("提取人脸特征过程中发生错误: {}", e.getMessage(), e);
            throw new Exception("人脸特征提取失败: " + e.getMessage(), e);
        }
    }
} 