package com.example.auth.service;

import com.example.auth.model.dto.request.RunningPreferenceRequest;
import com.example.auth.model.dto.response.RunningPreferenceResponse;
import com.example.auth.model.entity.RunningPreference;

/**
 * 跑步偏好服务接口
 */
public interface RunningPreferenceService {
    
    /**
     * 保存或更新用户的跑步偏好
     * 
     * @param userId 用户ID
     * @param request 跑步偏好请求参数
     * @return 保存后的跑步偏好数据
     */
    RunningPreferenceResponse saveOrUpdatePreference(Long userId, RunningPreferenceRequest request);
    
    /**
     * 获取用户的跑步偏好
     * 
     * @param userId 用户ID
     * @return 用户的跑步偏好数据，如果不存在则返回null
     */
    RunningPreferenceResponse getPreferenceByUserId(Long userId);
} 