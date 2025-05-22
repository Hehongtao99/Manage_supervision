package com.example.auth.service.impl;

import com.example.auth.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO对象存储服务实现类
 */
@Service
public class MinioServiceImpl implements MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioServiceImpl.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName:admin-system}")
    private String bucketName;

    @Value("${minio.endpoint:http://113.45.161.48:9000}")
    private String endpoint;

    @Override
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            // 检查存储桶是否存在
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                // 创建存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("创建存储桶: {}", bucketName);
            }

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            logger.info("文件上传成功: {}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            // 检查存储桶是否存在
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                // 创建存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("创建存储桶: {}", bucketName);
            }

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());

            logger.info("文件上传成功: {}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            // 直接返回对象路径，而不是生成预签名URL
            // 这将大大缩短URL长度，适合存储在数据库中
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            logger.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            logger.info("文件删除成功: {}", objectName);
            return true;
        } catch (Exception e) {
            logger.error("文件删除失败", e);
            return false;
        }
    }
} 