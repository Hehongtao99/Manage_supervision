package com.example.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 上传文件
     * @param file 要上传的文件
     * @return 包含文件URL和其他信息的响应
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 创建文件目录
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
                logger.info("已创建上传目录: {}", uploadDir);
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "未命名文件";
            }

            // 获取文件扩展名
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }

            // 生成唯一文件名
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = Paths.get(uploadDir, uniqueFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            logger.info("文件已保存: {}", filePath);

            // 创建相对URL路径
            String fileUrl = "/api/file/uploads/" + uniqueFilename;

            // 返回文件信息
            Map<String, Object> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("fileName", originalFilename);
            response.put("fileSize", file.getSize());
            response.put("fileType", file.getContentType());

            logger.info("文件上传成功 - 文件: {}, URL: {}", originalFilename, fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("处理文件上传请求时发生异常", e);
            return ResponseEntity.badRequest().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 获取上传的文件 - 修复路径问题，通过 /api/file/uploads/ 访问
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> getUploadedFile(@PathVariable String filename) {
        logger.info("请求文件: /uploads/{}", filename);
        return getFileResource(filename);
    }

    /**
     * 获取上传的文件
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/{pathVar1}/{pathVar2:.+}")
    public ResponseEntity<Resource> getFileWithTwoPathVars(
            @PathVariable String pathVar1,
            @PathVariable String pathVar2) {
        return getFileResource(pathVar1 + "/" + pathVar2);
    }

    /**
     * 获取上传的文件（单级路径）
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        return getFileResource(filename);
    }

    private ResponseEntity<Resource> getFileResource(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                // 确定文件的内容类型
                String contentType = null;
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    logger.error("无法确定文件类型", e);
                }
                
                // 如果无法确定内容类型，则默认为二进制流
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                logger.error("文件未找到: {}", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            logger.error("文件路径异常", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 