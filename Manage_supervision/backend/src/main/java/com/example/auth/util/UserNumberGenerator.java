package com.example.auth.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 用户编号生成工具类
 * 用于生成学号和教师号
 */
@Component
public class UserNumberGenerator {
    
    private static final String STUDENT_PREFIX = "S"; // 学生前缀
    private static final String TEACHER_PREFIX = "T"; // 教师前缀
    private static final Random random = new Random();
    
    /**
     * 生成学生学号
     * 格式：S + 年份后两位 + 随机6位数字
     * 示例：S23123456
     */
    public String generateStudentNumber() {
        String yearSuffix = getYearSuffix();
        String randomDigits = generateRandomDigits(6);
        return STUDENT_PREFIX + yearSuffix + randomDigits;
    }
    
    /**
     * 生成教师工号
     * 格式：T + 年份后两位 + 随机5位数字
     * 示例：T2312345
     */
    public String generateTeacherNumber() {
        String yearSuffix = getYearSuffix();
        String randomDigits = generateRandomDigits(5);
        return TEACHER_PREFIX + yearSuffix + randomDigits;
    }
    
    /**
     * 根据角色生成对应的用户编号
     */
    public String generateUserNumberByRole(String role) {
        if ("SUPERVISOR".equalsIgnoreCase(role)) {
            return generateTeacherNumber();
        } else {
            return generateStudentNumber();
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
        boolean isStudentNumber = currentNumber.startsWith(STUDENT_PREFIX);
        boolean isTeacherNumber = currentNumber.startsWith(TEACHER_PREFIX);
        
        // 角色与编号类型不匹配，需要更新
        if (isStudentNumber && "SUPERVISOR".equalsIgnoreCase(role)) {
            return true;
        }
        
        if (isTeacherNumber && !"SUPERVISOR".equalsIgnoreCase(role)) {
            return true;
        }
        
        return false;
    }
} 