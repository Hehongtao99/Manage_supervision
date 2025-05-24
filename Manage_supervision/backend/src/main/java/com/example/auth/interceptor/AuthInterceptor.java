package com.example.auth.interceptor;

import com.example.auth.annotation.RequirePermission;
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

import java.util.stream.Collectors;

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
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

        // 如果没有RequireRole或RequirePermission注解，直接通过
        if (requireRole == null && requirePermission == null) {
            return true;
        }

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
        
        // 加载用户权限
        user = userService.loadUserPermissions(user);
        
        // 先处理基于权限的授权（优先级高于角色）
        if (requirePermission != null) {
            String[] requiredPermissions = requirePermission.value();
            boolean allMatch = requirePermission.allMatch();
            
            System.out.println("请求拦截: " + request.getRequestURI() + ", 需要权限: " + String.join(",", requiredPermissions) + ", 全部匹配: " + allMatch);
            System.out.println("用户: " + username + ", 拥有权限: " + (user.getPermissions() != null ? 
                user.getPermissions().stream().map(p -> p.getCode()).collect(Collectors.joining(",")) : "无"));
            
            // 管理员拥有所有权限
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
            if (isAdmin) {
                System.out.println("用户是管理员，自动拥有所有权限");
                return true;
            }
            
            boolean hasPermission;
            if (allMatch) {
                // 需要全部权限
                hasPermission = userService.hasAllPermissions(user, requiredPermissions);
            } else {
                // 需要任意一个权限
                hasPermission = userService.hasAnyPermission(user, requiredPermissions);
            }
            
            if (!hasPermission) {
                System.out.println("认证失败: 权限不足，没有所需权限: " + String.join(",", requiredPermissions));
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            
            System.out.println("认证成功: 用户 " + username + " 拥有所需权限: " + String.join(",", requiredPermissions));
            return true;
        }
        
        // 处理基于角色的授权（兼容旧代码）
        if (requireRole != null) {
            String requiredRole = requireRole.value();
            System.out.println("请求拦截: " + request.getRequestURI() + ", 需要角色: " + requiredRole);
            
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
            
            // 如果没有所需角色，检查是否有对应的权限
            if (!hasRole) {
                // 将角色转换为对应的权限检查
                String permissionToCheck = requiredRole.toLowerCase() + ":view";
                if (userService.hasPermission(user, permissionToCheck)) {
                    System.out.println("用户没有角色 " + requiredRole + "，但拥有对应权限 " + permissionToCheck);
                    hasRole = true;
                }
            }
            
            if (!hasRole) {
                System.out.println("认证失败: 权限不足，没有所需角色 - " + requiredRole);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            
            System.out.println("认证成功: 用户 " + username + " 拥有所需角色 " + requiredRole);
        }
        
        return true;
    }
} 