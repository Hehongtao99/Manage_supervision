package com.example.auth.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 人脸识别服务接口
 */
public interface FaceRecognitionService {
    /**
     * 注册用户人脸
     * @param userId 用户ID
     * @param faceImage 人脸图像
     * @return 是否成功
     */
    boolean registerFace(Long userId, MultipartFile faceImage);
    
    /**
     * 注册用户人脸（指定特征类型）
     * @param userId 用户ID
     * @param faceImage 人脸图像
     * @param featureType 特征类型
     * @param description 特征描述
     * @return 是否成功
     */
    boolean registerFace(Long userId, MultipartFile faceImage, String featureType, String description);
    
    /**
     * 通过Base64编码的图像注册用户人脸
     * @param userId 用户ID
     * @param base64Image Base64编码的图像
     * @return 是否成功
     */
    boolean registerFaceWithBase64(Long userId, String base64Image);
    
    /**
     * 通过Base64编码的图像注册用户人脸（指定特征类型）
     * @param userId 用户ID
     * @param base64Image Base64编码的图像
     * @param featureType 特征类型
     * @param description 特征描述
     * @return 是否成功
     */
    boolean registerFaceWithBase64(Long userId, String base64Image, String featureType, String description);
    
    /**
     * 通过人脸识别进行用户验证
     * @param faceImage 人脸图像
     * @return 成功则返回用户ID，失败则返回null
     */
    Long verifyUserByFace(MultipartFile faceImage);
    
    /**
     * 通过Base64编码的图像进行用户验证
     * @param base64Image Base64编码的图像
     * @return 成功则返回用户ID，失败则返回null
     */
    Long verifyUserByFaceWithBase64(String base64Image);
    
    /**
     * 通过Base64编码的图像查找所有匹配的用户
     * @param base64Image Base64编码的图像
     * @return 匹配的用户ID列表，如果没有匹配则返回空列表
     */
    List<Long> findMatchingUsersByFaceWithBase64(String base64Image);
    
    /**
     * 通过Base64编码的图像进行用户验证（增强多特征匹配）
     * @param base64Image Base64编码的图像
     * @param matchStrategy 匹配策略：best（最佳匹配）, vote（投票）, average（平均）
     * @return 用户ID和匹配得分的映射，如果没有匹配则返回空Map
     */
    Map<Long, Double> verifyUserByFaceEnhanced(String base64Image, String matchStrategy);
    
    /**
     * 获取用户的所有人脸特征类型
     * @param userId 用户ID
     * @return 特征类型列表
     */
    List<Map<String, Object>> getUserFaceFeatures(Long userId);
    
    /**
     * 删除用户人脸信息
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteFace(Long userId);
    
    /**
     * 删除用户指定类型的人脸信息
     * @param userId 用户ID
     * @param featureType 特征类型
     * @return 是否成功
     */
    boolean deleteFace(Long userId, String featureType);
    
    /**
     * 检查用户是否已注册人脸
     * @param userId 用户ID
     * @return 是否已注册
     */
    boolean hasFaceRegistered(Long userId);
    
    /**
     * 从Base64编码的图像中提取人脸特征
     * @param base64Image Base64编码的图像
     * @return 人脸特征字节数组，如果未检测到人脸则返回null
     * @throws Exception 如果提取过程中发生错误
     */
    byte[] extractFaceFeaturesFromBase64(String base64Image) throws Exception;
} 