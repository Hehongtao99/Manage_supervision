package com.example.auth.service;

import com.example.auth.model.dto.response.AchievementResponse;

/**
 * 成就服务接口
 */
public interface AchievementService {

    /**
     * 获取用户成就信息
     *
     * @param userId 用户ID
     * @return 成就信息
     */
    AchievementResponse getUserAchievements(Long userId);

} 