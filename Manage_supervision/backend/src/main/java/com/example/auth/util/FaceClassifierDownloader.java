package com.example.auth.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 用于自动下载OpenCV级联分类器文件的组件
 * 在应用启动时自动执行
 */
@Component
public class FaceClassifierDownloader implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(FaceClassifierDownloader.class);
    
    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    private static final String RESOURCE_PATH = "/haarcascade/";
    private static final String CASCADE_PATH = "./data/haarcascade/";
    
    // GitHub上OpenCV分类器文件的URL
    private static final String CLASSIFIER_URL = 
            "https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml";
    
    @Override
    public void run(String... args) {
        try {
            downloadAndSetupClassifier();
        } catch (Exception e) {
            logger.error("下载级联分类器文件失败", e);
        }
    }
    
    /**
     * 下载并设置级联分类器文件
     */
    private void downloadAndSetupClassifier() {
        logger.info("正在检查级联分类器文件...");
        
        // 检查资源目录中是否已有分类器文件
        boolean resourceExists = checkResourceExists();
        
        // 检查本地目录中是否已有分类器文件
        File localDir = new File(CASCADE_PATH);
        if (!localDir.exists()) {
            boolean created = localDir.mkdirs();
            if (!created) {
                logger.error("无法创建目录: {}", CASCADE_PATH);
                return;
            }
        }
        
        File localFile = new File(CASCADE_PATH + CASCADE_FILE);
        
        if (localFile.exists() && localFile.length() > 0) {
            logger.info("级联分类器文件已存在: {}", localFile.getAbsolutePath());
            return;
        }
        
        // 如果资源目录中有文件，复制到本地目录
        if (resourceExists) {
            try (InputStream inputStream = getClass().getResourceAsStream(RESOURCE_PATH + CASCADE_FILE)) {
                if (inputStream != null) {
                    Files.copy(inputStream, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.info("从资源复制级联分类器文件成功");
                    return;
                }
            } catch (IOException e) {
                logger.error("复制级联分类器文件失败", e);
            }
        }
        
        // 如果资源目录中没有文件或复制失败，从GitHub下载
        downloadFromGitHub(localFile);
    }
    
    /**
     * 检查资源目录中是否存在级联分类器文件
     */
    private boolean checkResourceExists() {
        try (InputStream inputStream = getClass().getResourceAsStream(RESOURCE_PATH + CASCADE_FILE)) {
            return inputStream != null;
        } catch (IOException e) {
            logger.error("检查资源文件失败", e);
            return false;
        }
    }
    
    /**
     * 从GitHub下载级联分类器文件
     */
    private void downloadFromGitHub(File targetFile) {
        logger.info("正在从GitHub下载级联分类器文件...");
        
        try {
            URL url = new URL(CLASSIFIER_URL);
            try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                 FileChannel fileChannel = fileOutputStream.getChannel()) {
                
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                logger.info("级联分类器文件下载成功: {}", targetFile.getAbsolutePath());
                
                // 备份到resources目录
                backupToResources(targetFile);
            }
        } catch (IOException e) {
            logger.error("下载级联分类器文件失败", e);
        }
    }
    
    /**
     * 将下载的文件备份到resources目录
     */
    private void backupToResources(File sourceFile) {
        try {
            // 尝试获取resources目录的路径
            String basePath = new File("").getAbsolutePath();
            Path resourcesPath = Paths.get(basePath, "src", "main", "resources", "haarcascade");
            
            // 确保目录存在
            Files.createDirectories(resourcesPath);
            
            // 复制文件
            Path targetPath = resourcesPath.resolve(CASCADE_FILE);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("已将级联分类器文件备份到resources目录: {}", targetPath);
        } catch (IOException e) {
            logger.error("备份级联分类器文件失败", e);
        }
    }
} 