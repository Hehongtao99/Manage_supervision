package com.example.auth.controller;

import com.example.auth.model.entity.User;
import com.example.auth.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/chat")
public class ChatFileController {

    private static final Logger logger = LoggerFactory.getLogger(ChatFileController.class);
    
    // 设置最大文件大小限制为20MB
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    private UserContext userContext;

    /**
     * 上传聊天文件
     * @param file 要上传的文件
     * @return 包含文件URL和其他信息的响应
     */
    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadChatFile(@RequestParam("file") MultipartFile file) {
        try {
            // 获取当前用户
            User currentUser = userContext.getCurrentUser();
            if (currentUser == null) {
                logger.error("上传文件失败：用户未登录");
                return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
            }
            
            // 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                logger.error("上传文件失败：文件大小超过限制 - 用户: {}, 文件大小: {}", 
                            currentUser.getUsername(), file.getSize());
                return ResponseEntity.badRequest().body(Map.of("error", "文件大小不能超过20MB"));
            }

            // 创建聊天文件目录
            String chatFilesDir = uploadDir + "/chat_files";
            File directory = new File(chatFilesDir);
            if (!directory.exists()) {
                directory.mkdirs();
                logger.info("已创建聊天文件目录: {}", chatFilesDir);
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
            Path filePath = Paths.get(chatFilesDir, uniqueFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            logger.info("文件已保存: {}", filePath);

            // 创建相对URL路径
            String fileUrl = "/uploads/chat_files/" + uniqueFilename;

            // 返回文件信息
            Map<String, Object> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("fileName", originalFilename);
            response.put("fileSize", file.getSize());
            response.put("fileType", file.getContentType());

            logger.info("文件上传成功 - 用户: {}, 文件: {}, URL: {}", 
                        currentUser.getUsername(), originalFilename, fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("处理文件上传请求时发生异常", e);
            return ResponseEntity.badRequest().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }
} 