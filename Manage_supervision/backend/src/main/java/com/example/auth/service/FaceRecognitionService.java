package com.example.auth.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 人脸识别服务接口
 */
public interface FaceRecognitionService {
    
    /**
     * 模拟人脸识别
     * 为了简化实现，这里仅模拟识别过程
     * 
     * @param base64Image Base64编码的图像
     * @return 识别结果，包含是否成功和相关信息
     */
    boolean recognizeFace(String base64Image);
    
    /**
     * 处理上传的人脸图像
     * 
     * @param imageFile 图像文件
     * @return 识别结果
     */
    boolean processUploadedImage(MultipartFile imageFile);
} 