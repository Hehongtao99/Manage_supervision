package com.example.auth.service;

import com.example.auth.entity.User;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 从Token中获取用户信息
     * 
     * @param token JWT令牌
     * @return 用户实体
     */
    User getUserFromToken(String token);
} 