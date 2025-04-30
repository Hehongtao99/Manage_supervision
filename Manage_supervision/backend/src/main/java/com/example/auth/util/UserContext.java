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
                
                System.out.println("当前请求头部: Authorization=" + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    System.out.println("提取到Token: " + token.substring(0, Math.min(20, token.length())) + "...");
                    
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.getUsernameFromToken(token);
                        System.out.println("Token有效，提取到用户名: " + username);
                        User user = userService.findByUsername(username);
                        if (user != null) {
                            System.out.println("成功获取到用户: ID=" + user.getId() + ", 用户名=" + user.getUsername());
                        } else {
                            System.out.println("找不到用户: " + username);
                        }
                        return user;
                    } else {
                        System.out.println("Token无效");
                    }
                } else {
                    System.out.println("Authorization头部格式不正确或不存在");
                }
            } else {
                System.out.println("无法获取请求上下文");
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
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户对象，如果未找到则返回null
     */
    public User getUserByUsername(String username) {
        try {
            if (username != null && !username.isEmpty()) {
                return userService.findByUsername(username);
            }
        } catch (Exception e) {
            System.err.println("根据用户名获取用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
} 