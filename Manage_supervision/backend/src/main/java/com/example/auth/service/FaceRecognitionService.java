package com.example.auth.service;

import org.springframework.web.multipart.MultipartFile;

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
     * 通过Base64编码的图像注册用户人脸
     * @param userId 用户ID
     * @param base64Image Base64编码的图像
     * @return 是否成功
     */
    boolean registerFaceWithBase64(Long userId, String base64Image);
    
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
     * 删除用户人脸信息
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteFace(Long userId);
    
    /**
     * 检查用户是否已注册人脸
     * @param userId 用户ID
     * @return 是否已注册
     */
    boolean hasFaceRegistered(Long userId);
} 