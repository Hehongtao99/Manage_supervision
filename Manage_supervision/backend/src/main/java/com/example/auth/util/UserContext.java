package com.example.auth.util;

import com.example.auth.entity.User;
import com.example.auth.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类，用于在请求过程中获取当前用户信息
 */
@Component
public class UserContext {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserContext(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前用户
     * @return 当前用户对象
     */
    public User getCurrentUser() {
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
        }
        return null;
    }
} 