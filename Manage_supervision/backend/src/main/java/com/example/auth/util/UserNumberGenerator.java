package com.example.auth.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户编号生成器
 */
@Component
public class UserNumberGenerator {
    
    private static final String USER_PREFIX = "R"; // Runner前缀
    private static final int RANDOM_LENGTH = 4; // 随机数长度
    
    // 用于缓存已生成的编号，防止重复
    private final ConcurrentHashMap<String, Boolean> generatedNumbers = new ConcurrentHashMap<>();
    
    /**
     * 生成用户编号
     * 格式：R + 年份后两位 + 4位随机数
     * 例如：R240001
     */
    public String generateUserNumber() {
        String yearSuffix = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        String randomDigits = generateRandomDigits();
        return USER_PREFIX + yearSuffix + randomDigits;
    }
    
    /**
     * 根据角色生成用户编号
     * @param role 用户角色
     * @return 用户编号
     */
    public String generateUserNumberByRole(String role) {
        // 统一使用同一种编号格式
        return generateUserNumber();
    }
    
    /**
     * 生成指定长度的随机数字串
     */
    private String generateRandomDigits() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        // 确保不以0开头
        sb.append(random.nextInt(9) + 1);
        
        // 生成剩余的随机数字
        for (int i = 1; i < RANDOM_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
    
    /**
     * 检查编号是否已存在
     */
    public boolean isNumberExists(String number) {
        return generatedNumbers.containsKey(number);
    }
    
    /**
     * 添加已使用的编号到缓存
     */
    public void addUsedNumber(String number) {
        generatedNumbers.put(number, true);
    }
    
    /**
     * 验证用户编号是否需要更改
     * @param currentNumber 当前编号
     * @param newRole 新角色
     * @return 是否需要更改
     */
    public boolean needsNumberChange(String currentNumber, String newRole) {
        // 统一使用同一种编号格式，不需要根据角色更改
        return false;
    }
} 