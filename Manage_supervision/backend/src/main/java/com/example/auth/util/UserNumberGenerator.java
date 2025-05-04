package com.example.auth.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 用户编号生成工具类
 * 用于生成玩家号和陪玩号
 */
@Component
public class UserNumberGenerator {
    
    private static final String PLAYER_PREFIX = "P"; // 玩家前缀
    private static final String COMPANION_PREFIX = "C"; // 陪玩前缀
    private static final Random random = new Random();
    
    /**
     * 生成玩家号
     * 格式：P + 年份后两位 + 随机6位数字
     * 示例：P23123456
     */
    public String generatePlayerNumber() {
        String yearSuffix = getYearSuffix();
        String randomDigits = generateRandomDigits(6);
        return PLAYER_PREFIX + yearSuffix + randomDigits;
    }
    
    /**
     * 生成陪玩工号
     * 格式：C + 年份后两位 + 随机5位数字
     * 示例：C2312345
     */
    public String generateCompanionNumber() {
        String yearSuffix = getYearSuffix();
        String randomDigits = generateRandomDigits(5);
        return COMPANION_PREFIX + yearSuffix + randomDigits;
    }
    
    /**
     * 根据角色生成对应的用户编号
     */
    public String generateUserNumberByRole(String role) {
        if ("SUPERVISOR".equalsIgnoreCase(role)) {
            return generateCompanionNumber();
        } else {
            return generatePlayerNumber();
        }
    }
    
    /**
     * 获取当前年份后两位
     */
    private String getYearSuffix() {
        int year = LocalDateTime.now().getYear();
        return String.valueOf(year).substring(2);
    }
    
    /**
     * 生成指定长度的随机数字
     */
    private String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // 0-9的随机数字
        }
        return sb.toString();
    }
    
    /**
     * 检查是否需要更新用户编号（角色改变）
     * @param currentNumber 当前编号
     * @param role 目标角色
     * @return 是否需要更新
     */
    public boolean needsNumberUpdate(String currentNumber, String role) {
        if (currentNumber == null || currentNumber.isEmpty()) {
            return true;
        }
        
        // 根据前缀检查当前编号类型
        boolean isPlayerNumber = currentNumber.startsWith(PLAYER_PREFIX);
        boolean isCompanionNumber = currentNumber.startsWith(COMPANION_PREFIX);
        
        // 角色与编号类型不匹配，需要更新
        if (isPlayerNumber && "SUPERVISOR".equalsIgnoreCase(role)) {
            return true;
        }
        
        if (isCompanionNumber && !"SUPERVISOR".equalsIgnoreCase(role)) {
            return true;
        }
        
        return false;
    }
} 