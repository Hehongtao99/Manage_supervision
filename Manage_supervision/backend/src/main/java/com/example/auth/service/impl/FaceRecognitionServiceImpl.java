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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 人脸识别服务实现类
 */
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionServiceImpl.class);
    
    // 添加人脸匹配阈值常量
    private static final double FACE_MATCH_THRESHOLD = 0.80; // 80%的匹配度
    
    // 定义投票策略的最小匹配百分比
    private static final double VOTE_THRESHOLD_PERCENTAGE = 0.5; // 超过50%的特征匹配才认为是同一个人
    
    // 最小匹配特征数（至少要有几个特征匹配才算成功）
    private static final int MIN_MATCH_FEATURES = 1;
    
    @Autowired
    private UserFaceFeatureRepository userFaceFeatureRepository;
    
    @Autowired
    private FaceRecognitionUtil faceRecognitionUtil;
    
    @Override
    @Transactional
    public boolean registerFace(Long userId, MultipartFile faceImage) {
        return registerFace(userId, faceImage, "primary", "主要人脸特征");
    }
    
    @Override
    @Transactional
    public boolean registerFace(Long userId, MultipartFile faceImage, String featureType, String description) {
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
            
            // 查找已有的同类型特征
            Optional<UserFaceFeature> existingFeature = userFaceFeatureRepository
                    .findTopByUserIdAndFeatureTypeOrderByUpdateTimeDesc(userId, featureType);
            
            UserFaceFeature userFaceFeature;
            if (existingFeature.isPresent()) {
                // 更新现有特征
                userFaceFeature = existingFeature.get();
                userFaceFeature.setFaceFeature(faceFeatures);
                userFaceFeature.setFaceImage(faceImageBytes);
                userFaceFeature.setDescription(description);
            } else {
                // 创建新特征
                userFaceFeature = new UserFaceFeature();
                userFaceFeature.setUserId(userId);
                userFaceFeature.setFaceFeature(faceFeatures);
                userFaceFeature.setFaceImage(faceImageBytes);
                userFaceFeature.setFeatureType(featureType);
                userFaceFeature.setDescription(description);
            }
            
            userFaceFeatureRepository.save(userFaceFeature);
            
            logger.info("用户 {} 的 {} 类型人脸特征注册成功", userId, featureType);
            return true;
        } catch (Exception e) {
            logger.error("用户 {} 的人脸注册失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean registerFaceWithBase64(Long userId, String base64Image) {
        return registerFaceWithBase64(userId, base64Image, "primary", "主要人脸特征");
    }
    
    @Override
    @Transactional
    public boolean registerFaceWithBase64(Long userId, String base64Image, String featureType, String description) {
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
            
            // 查找已有的同类型特征
            Optional<UserFaceFeature> existingFeature = userFaceFeatureRepository
                    .findTopByUserIdAndFeatureTypeOrderByUpdateTimeDesc(userId, featureType);
            
            UserFaceFeature userFaceFeature;
            if (existingFeature.isPresent()) {
                // 更新现有特征
                userFaceFeature = existingFeature.get();
                userFaceFeature.setFaceFeature(faceFeatures);
                userFaceFeature.setFaceImage(faceImageBytes);
                userFaceFeature.setDescription(description);
            } else {
                // 创建新特征
                userFaceFeature = new UserFaceFeature();
                userFaceFeature.setUserId(userId);
                userFaceFeature.setFaceFeature(faceFeatures);
                userFaceFeature.setFaceImage(faceImageBytes);
                userFaceFeature.setFeatureType(featureType);
                userFaceFeature.setDescription(description);
            }
            
            userFaceFeatureRepository.save(userFaceFeature);
            
            logger.info("用户 {} 的 {} 类型人脸特征注册成功", userId, featureType);
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
            
            return matchingUserIds;
        } catch (Exception e) {
            logger.error("寻找匹配用户失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<Long, Double> verifyUserByFaceEnhanced(String base64Image, String matchStrategy) {
        try {
            // 提取人脸特征
            byte[] inputFeatures = faceRecognitionUtil.extractFaceFeaturesFromBase64(base64Image);
            if (inputFeatures == null) {
                logger.error("提取人脸特征失败，未检测到人脸或检测到多个人脸");
                return new HashMap<>();
            }
            
            // 获取所有用户的人脸特征
            List<UserFaceFeature> allUserFaces = userFaceFeatureRepository.findAll();
            logger.info("开始多特征人脸比对，已注册用户数量: {}", allUserFaces.size());
            
            // 按用户ID分组所有特征
            Map<Long, List<UserFaceFeature>> userFeaturesMap = allUserFaces.stream()
                    .collect(Collectors.groupingBy(UserFaceFeature::getUserId));
            
            // 结果Map，存储用户ID到匹配分数的映射
            Map<Long, Double> resultMap = new HashMap<>();
            
            // 对每个用户，使用所选策略计算匹配分数
            for (Map.Entry<Long, List<UserFaceFeature>> entry : userFeaturesMap.entrySet()) {
                Long userId = entry.getKey();
                List<UserFaceFeature> userFeatures = entry.getValue();
                
                // 使用所选策略计算匹配分数
                Double score = calculateMatchScore(inputFeatures, userFeatures, matchStrategy);
                
                if (score != null && score >= FACE_MATCH_THRESHOLD) {
                    resultMap.put(userId, score);
                    logger.info("多特征匹配成功，用户ID: {}, 匹配分数: {}, 策略: {}", userId, score, matchStrategy);
                }
            }
            
            // 按匹配分数降序排序
            Map<Long, Double> sortedResults = resultMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            
            return sortedResults;
        } catch (Exception e) {
            logger.error("增强人脸验证失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 根据选定的策略，计算输入特征与用户多个特征的匹配分数
     * 
     * @param inputFeatures 输入的人脸特征
     * @param userFeatures 用户的多个人脸特征
     * @param strategy 匹配策略
     * @return 匹配分数，如果不匹配则返回null
     */
    private Double calculateMatchScore(byte[] inputFeatures, List<UserFaceFeature> userFeatures, String strategy) {
        if (userFeatures == null || userFeatures.isEmpty()) {
            return null;
        }
        
        // 计算每个特征的相似度
        List<Double> similarities = new ArrayList<>();
        for (UserFaceFeature feature : userFeatures) {
            if (feature.getFaceFeature() != null) {
                double similarity = faceRecognitionUtil.calculateSimilarity(feature.getFaceFeature(), inputFeatures);
                similarities.add(similarity);
            }
        }
        
        if (similarities.isEmpty()) {
            return null;
        }
        
        // 根据不同策略计算最终分数
        switch (strategy.toLowerCase()) {
            case "best": // 最佳匹配策略：取最高相似度
                return similarities.stream().max(Double::compare).orElse(0.0);
                
            case "vote": // 投票策略：超过阈值的特征占比
                long matchCount = similarities.stream().filter(s -> s >= FACE_MATCH_THRESHOLD).count();
                if (matchCount >= MIN_MATCH_FEATURES) {
                    double voteRatio = (double) matchCount / similarities.size();
                    if (voteRatio >= VOTE_THRESHOLD_PERCENTAGE) {
                        // 返回匹配特征的平均相似度作为分数
                        double avgMatchScore = similarities.stream()
                                .filter(s -> s >= FACE_MATCH_THRESHOLD)
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0);
                        return avgMatchScore;
                    }
                }
                return null;
                
            case "average": // 加权平均策略：所有特征的加权平均值
                // 特征权重：将所有相似度加起来，按相似度分配权重
                double totalSimilarity = similarities.stream().mapToDouble(Double::doubleValue).sum();
                if (totalSimilarity <= 0) {
                    return 0.0;
                }
                
                // 计算加权平均值
                double weightedSum = 0.0;
                for (Double similarity : similarities) {
                    double weight = similarity / totalSimilarity;
                    weightedSum += similarity * weight;
                }
                
                return weightedSum;
                
            default: // 默认为平均值
                return similarities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserFaceFeatures(Long userId) {
        List<UserFaceFeature> features = userFaceFeatureRepository.findByUserId(userId);
        
        return features.stream().map(feature -> {
            Map<String, Object> featureMap = new HashMap<>();
            featureMap.put("id", feature.getId());
            featureMap.put("featureType", feature.getFeatureType());
            featureMap.put("description", feature.getDescription());
            featureMap.put("updateTime", feature.getUpdateTime());
            // 不返回特征数据和图像，因为太大
            return featureMap;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean deleteFace(Long userId) {
        try {
            userFaceFeatureRepository.deleteByUserId(userId);
            logger.info("用户 {} 的所有人脸信息已删除", userId);
            return true;
        } catch (Exception e) {
            logger.error("删除用户 {} 的人脸信息失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteFace(Long userId, String featureType) {
        try {
            userFaceFeatureRepository.deleteByUserIdAndFeatureType(userId, featureType);
            logger.info("用户 {} 的 {} 类型人脸信息已删除", userId, featureType);
            return true;
        } catch (Exception e) {
            logger.error("删除用户 {} 的 {} 类型人脸信息失败: {}", userId, featureType, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean hasFaceRegistered(Long userId) {
        return userFaceFeatureRepository.findTopByUserIdOrderByUpdateTimeDesc(userId).isPresent();
    }
    
    @Override
    public byte[] extractFaceFeaturesFromBase64(String base64Image) throws Exception {
        return faceRecognitionUtil.extractFaceFeaturesFromBase64(base64Image);
    }
} 