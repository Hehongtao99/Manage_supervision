package com.example.auth.service.impl;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import com.example.auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 认证服务实现类
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public User getUserFromToken(String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            log.error("根据Token中的用户ID找不到用户: {}", userId);
            throw new IllegalArgumentException("无效的用户令牌");
        }
        
        return userOpt.get();
    }
} 