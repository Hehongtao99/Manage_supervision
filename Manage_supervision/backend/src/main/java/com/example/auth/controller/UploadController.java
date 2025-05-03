package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    
    // 设置最大图片大小限制为2MB
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 普通图片上传接口
     * @param file 要上传的图片文件
     * @return 包含图片URL的响应
     */
    @PostMapping("/api/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件大小限制
            if (file.getSize() > MAX_IMAGE_SIZE) {
                logger.error("图片上传失败：文件大小超过限制，文件大小: {}", file.getSize());
                return ResponseEntity.badRequest().body(Map.of("message", "图片大小不能超过2MB"));
            }
            
            // 检查文件类型限制
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                logger.error("图片上传失败：文件类型不支持，文件类型: {}", contentType);
                return ResponseEntity.badRequest().body(Map.of("message", "图片只能是JPG或PNG格式"));
            }

            String saveDirectory = uploadDir;
            // 确保上传目录存在
            File directory = new File(saveDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
                logger.info("已创建图片上传目录: {}", saveDirectory);
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "unnamed.jpg";
            }

            // 获取文件扩展名
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }

            // 生成唯一文件名
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = Paths.get(saveDirectory, uniqueFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            logger.info("图片已保存: {}", filePath);

            // 创建相对URL路径
            String imageUrl = "/uploads/" + uniqueFilename;

            // 返回图片信息
            Map<String, Object> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("data", Map.of("url", imageUrl));

            logger.info("图片上传成功，文件: {}, URL: {}", originalFilename, imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("图片上传失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "图片上传失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("处理图片上传请求时发生异常", e);
            return ResponseEntity.badRequest().body(Map.of("message", "图片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 管理员图片上传接口
     * @param file 要上传的图片文件
     * @return 包含图片URL的响应
     */
    @PostMapping("/api/admin/upload")
    @RequireRole("ADMIN")  // 确保只有管理员可以使用此接口
    public ResponseEntity<?> adminUploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件大小限制
            if (file.getSize() > MAX_IMAGE_SIZE) {
                logger.error("管理员图片上传失败：文件大小超过限制，文件大小: {}", file.getSize());
                return ResponseEntity.badRequest().body(Map.of("message", "图片大小不能超过2MB"));
            }
            
            // 检查文件类型限制
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                logger.error("管理员图片上传失败：文件类型不支持，文件类型: {}", contentType);
                return ResponseEntity.badRequest().body(Map.of("message", "图片只能是JPG或PNG格式"));
            }

            String saveDirectory = uploadDir + "/admin";
            // 确保上传目录存在
            File directory = new File(saveDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
                logger.info("已创建管理员图片上传目录: {}", saveDirectory);
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "unnamed.jpg";
            }

            // 获取文件扩展名
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }

            // 生成唯一文件名
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = Paths.get(saveDirectory, uniqueFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            logger.info("管理员图片已保存: {}", filePath);

            // 创建相对URL路径
            String imageUrl = "/uploads/admin/" + uniqueFilename;

            // 返回图片信息
            Map<String, Object> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("data", Map.of("url", imageUrl));

            logger.info("管理员图片上传成功，文件: {}, URL: {}", originalFilename, imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("管理员图片上传失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "图片上传失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("处理管理员图片上传请求时发生异常", e);
            return ResponseEntity.badRequest().body(Map.of("message", "图片上传失败: " + e.getMessage()));
        }
    }
} 