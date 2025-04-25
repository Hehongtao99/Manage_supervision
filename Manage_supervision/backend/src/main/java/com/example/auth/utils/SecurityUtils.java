package com.example.auth.utils;

import com.example.auth.entity.User;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全工具类，用于获取当前用户
 */
@Component
public class SecurityUtils {

    private static UserService userService;
    private static JwtUtil jwtUtil;

    @Autowired
    public SecurityUtils(UserService userService, JwtUtil jwtUtil) {
        SecurityUtils.userService = userService;
        SecurityUtils.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前用户
     * @return 当前用户对象
     */
    public static User getCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.getUsernameFromToken(token);
                        return userService.findByUsername(username);
                    }
                }
            }
        } catch (Exception e) {
            // 记录错误但不抛出，返回null表示未获取到用户
            System.err.println("获取当前用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     * @return 当前用户ID，如果未获取到用户则返回null
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
} 