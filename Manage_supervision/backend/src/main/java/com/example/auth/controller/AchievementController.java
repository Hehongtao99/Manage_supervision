package com.example.auth.controller;

import com.example.auth.model.dto.response.AchievementResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.AchievementService;
import com.example.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成就控制器
 */
@RestController
@RequestMapping("/api/student/running")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final JwtUtil jwtUtil;

    /**
     * 获取用户成就信息
     *
     * @return 成就信息
     */
    @GetMapping("/achievements")
    public ResponseVO<AchievementResponse> getUserAchievements(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        AchievementResponse achievements = achievementService.getUserAchievements(userId);
        return ResponseVO.success("获取成就信息成功", achievements);
    }
} 