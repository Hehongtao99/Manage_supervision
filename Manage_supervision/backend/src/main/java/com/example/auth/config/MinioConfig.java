package com.example.auth.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint:http://113.45.161.48:9000}")
    private String endpoint;

    @Value("${minio.accessKey:hht5002342003}")
    private String accessKey;

    @Value("${minio.secretKey:hht5002342003}")
    private String secretKey;

    @Value("${minio.bucketName:admin-system}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
} 