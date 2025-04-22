package com.example.auth.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.MessageDigest;
import java.util.Base64;

public class PasswordUtils {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);
    private static final String LEGACY_SALT = "your_custom_salt_value"; // 旧版本中使用的盐值

    /**
     * 对密码进行加密，使用BCrypt算法
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (password == null) {
            logger.warn("加密密码失败：密码为null");
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            // 使用BCrypt加密，自动生成盐值并包含在结果中
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
            logger.debug("密码加密成功");
            return hashedPassword;
        } catch (Exception e) {
            logger.error("密码加密过程中发生异常", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码是否匹配，支持新的BCrypt格式和旧的SHA-256格式
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            logger.warn("密码验证失败：原始密码或加密密码为null");
            return false;
        }
        
        try {
            // 首先尝试使用BCrypt验证
            if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                boolean matches = BCrypt.checkpw(rawPassword, encodedPassword);
                logger.debug("使用BCrypt验证密码: {}", matches ? "匹配" : "不匹配");
                return matches;
            } else {
                // 尝试使用旧的SHA-256格式验证
                logger.debug("使用旧格式验证密码");
                String legacyHash = legacyEncrypt(rawPassword);
                boolean matches = legacyHash.equals(encodedPassword);
                logger.debug("使用旧格式验证密码: {}", matches ? "匹配" : "不匹配");
                return matches;
            }
        } catch (Exception e) {
            logger.error("密码验证过程中发生异常", e);
            return false;
        }
    }
    
    /**
     * 使用旧的SHA-256算法加密密码，用于向后兼容
     * @param password 原始密码
     * @return 旧格式的加密密码
     */
    private static String legacyEncrypt(String password) {
        try {
            // 将密码和盐值组合
            String saltedPassword = password + LEGACY_SALT;
            
            // 创建SHA-256摘要器
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // 计算哈希值
            byte[] hash = digest.digest(saltedPassword.getBytes());
            
            // 将字节数组转换为Base64字符串
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            logger.error("旧格式密码加密失败", e);
            throw new RuntimeException("旧格式密码加密失败", e);
        }
    }
    
    /**
     * 检查密码是否需要升级到新格式
     * @param encodedPassword 加密后的密码
     * @return 是否需要升级
     */
    public static boolean needsUpgrade(String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        
        // 检查是否已经是BCrypt格式
        return !(encodedPassword.startsWith("$2a$") || 
                encodedPassword.startsWith("$2b$") || 
                encodedPassword.startsWith("$2y$"));
    }
} 