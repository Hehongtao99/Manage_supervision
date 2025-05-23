package com.example.auth.service;

import java.util.Map;

/**
 * 人脸识别服务接口
 */
public interface FaceRecognitionService {
    
    /**
     * 检测并处理人脸
     * @param base64Image Base64编码的图像
     * @return 处理后的人脸Base64编码
     */
    String detectAndProcessFace(String base64Image);
    
    /**
     * 检测并处理人脸，返回人脸位置信息
     * @param base64Image Base64编码的图像
     * @return 包含处理后的人脸和位置信息的Map
     */
    Map<String, Object> detectAndProcessFaceWithPosition(String base64Image);
    
    /**
     * 验证人脸
     * @param userId 用户ID
     * @param capturedFaceData 捕获的人脸数据
     * @return 是否验证通过
     */
    boolean verifyFace(Long userId, String capturedFaceData);
    
    /**
     * 保存用户人脸数据
     * @param userId 用户ID
     * @param faceData 人脸数据
     * @return 是否保存成功
     */
    boolean saveFaceData(Long userId, String faceData);
    
    /**
     * 获取人脸相似度
     * @param userId 用户ID
     * @param capturedFaceData 捕获的人脸数据
     * @return 相似度（0-1之间的值，1表示完全匹配）
     */
    double getFaceSimilarity(Long userId, String capturedFaceData);
    
    /**
     * 根据人脸数据查找匹配的用户
     * @param capturedFaceData 捕获的人脸数据
     * @return 匹配结果，包含匹配的用户ID和相似度
     */
    Map<String, Object> findMatchingUser(String capturedFaceData);
    
    /**
     * 多次验证人脸，提高识别准确性
     * @param userId 用户ID
     * @param capturedFaceData 捕获的人脸数据
     * @param verificationTimes 验证次数（建议3-5次）
     * @return 多次验证结果，包含平均相似度等详细信息
     */
    Map<String, Object> verifyFaceMultipleTimes(Long userId, String capturedFaceData, int verificationTimes);
    
    /**
     * 根据人脸数据查找匹配用户（多次验证版本）
     * @param capturedFaceData 捕获的人脸数据
     * @param verificationTimes 验证次数
     * @return 匹配结果，包含详细验证信息
     */
    Map<String, Object> findMatchingUserWithMultipleVerification(String capturedFaceData, int verificationTimes);
} 