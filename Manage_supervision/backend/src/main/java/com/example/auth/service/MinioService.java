package com.example.auth.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

/**
 * MinIO对象存储服务接口
 */
public interface MinioService {

    /**
     * 上传文件到MinIO
     * @param file 文件对象
     * @param objectName 对象名称
     * @return 访问URL
     */
    String uploadFile(MultipartFile file, String objectName);
    
    /**
     * 上传文件到MinIO
     * @param inputStream 输入流
     * @param objectName 对象名称
     * @param contentType 内容类型
     * @param size 文件大小
     * @return 访问URL
     */
    String uploadFile(InputStream inputStream, String objectName, String contentType, long size);
    
    /**
     * 获取文件访问URL
     * @param objectName 对象名称
     * @return 访问URL
     */
    String getFileUrl(String objectName);
    
    /**
     * 删除文件
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    boolean deleteFile(String objectName);
} 