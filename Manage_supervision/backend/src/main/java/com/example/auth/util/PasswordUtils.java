package com.example.auth.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 密码工具类，提供密码加密和验证功能
 */
public class PasswordUtils {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);
    private static final String LEGACY_SALT = "your_custom_salt_value"; // 旧版本中使用的盐值
    
    // BCrypt工作因子，值越大加密强度越高但性能越低
    private static final int BCRYPT_WORKLOAD = 12;
    
    // 密码格式前缀
    private static final String BCRYPT_PREFIX = "$2a$";

    /**
     * 使用BCrypt加密密码
     * 
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            // 生成盐值并加密
            String salt = BCrypt.gensalt(BCRYPT_WORKLOAD);
            return BCrypt.hashpw(plainPassword, salt);
        } catch (Exception e) {
            logger.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证密码是否匹配
     * 
     * @param plainPassword 明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            // BCrypt格式密码验证
            if (hashedPassword.startsWith(BCRYPT_PREFIX)) {
                return BCrypt.checkpw(plainPassword, hashedPassword);
            } 
            // 如果是其他格式的密码，这里可以添加兼容旧密码的验证逻辑
            else {
                // 仅作示例：明文密码比较（不推荐）
                logger.warn("使用了不安全的密码格式");
                return plainPassword.equals(hashedPassword);
            }
        } catch (Exception e) {
            logger.error("密码验证失败", e);
            return false;
        }
    }
    
    /**
     * 检查密码是否需要升级到新格式
     * 
     * @param hashedPassword 加密后的密码
     * @return 是否需要升级
     */
    public static boolean needsUpgrade(String hashedPassword) {
        if (hashedPassword == null) {
            return false;
        }
        
        // 判断是否为BCrypt格式密码
        return !hashedPassword.startsWith(BCRYPT_PREFIX);
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
} 