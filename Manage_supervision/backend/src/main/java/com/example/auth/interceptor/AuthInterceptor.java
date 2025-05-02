package com.example.auth.interceptor;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.entity.User;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // 如果没有RequireRole注解，直接通过
        if (requireRole == null) {
            return true;
        }

        // 记录请求路径和所需角色
        System.out.println("请求拦截: " + request.getRequestURI() + ", 需要角色: " + requireRole.value());

        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("认证失败: Token为空或格式错误");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        token = token.substring(7);
        System.out.println("Token: " + token.substring(0, Math.min(10, token.length())) + "...");

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("认证失败: Token无效");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 获取用户信息
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println("认证失败: 用户不存在 - " + username);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // 检查用户状态
        if ("inactive".equals(user.getStatus())) {
            System.out.println("认证失败: 用户已被禁用 - " + username);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // 简化角色验证逻辑：直接字符串比较
        String requiredRole = requireRole.value();
        boolean hasRole = false;

        System.out.println("用户: " + username + ", 角色: " + user.getRoles());

        // 直接使用简单字符串比对
        for (var role : user.getRoles()) {
            String roleName = role.getName();
            System.out.println("比对角色: " + roleName + " vs 需要: " + requiredRole);
            
            // 不区分大小写比较
            if (roleName.equalsIgnoreCase(requiredRole)) {
                hasRole = true;
                break;
            }
            
            // 实现角色层级：ADMIN具有所有权限，SUPERVISOR具有USER权限
            if (roleName.equalsIgnoreCase("ADMIN")) {
                // 管理员拥有所有权限
                hasRole = true;
                break;
            } else if (roleName.equalsIgnoreCase("SUPERVISOR") && requiredRole.equalsIgnoreCase("USER")) {
                // 督导员拥有用户权限
                hasRole = true;
                break;
            }
        }

        if (!hasRole) {
            System.out.println("认证失败: 权限不足，没有所需角色 - " + requiredRole);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        System.out.println("认证成功: 用户 " + username + " 拥有所需角色 " + requiredRole);
        return true;
    }
} 