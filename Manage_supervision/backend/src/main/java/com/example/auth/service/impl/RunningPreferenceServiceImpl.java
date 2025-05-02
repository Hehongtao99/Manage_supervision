package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.RunningPreferenceMapper;
import com.example.auth.model.dto.request.RunningPreferenceRequest;
import com.example.auth.model.dto.response.RunningPreferenceResponse;
import com.example.auth.model.entity.RunningPreference;
import com.example.auth.service.RunningPreferenceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 跑步偏好服务实现类
 */
@Service
public class RunningPreferenceServiceImpl implements RunningPreferenceService {

    @Autowired
    private RunningPreferenceMapper runningPreferenceMapper;

    @Override
    public RunningPreferenceResponse saveOrUpdatePreference(Long userId, RunningPreferenceRequest request) {
        // 检查是否已存在用户的跑步偏好
        LambdaQueryWrapper<RunningPreference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RunningPreference::getUserId, userId);
        RunningPreference preference = runningPreferenceMapper.selectOne(queryWrapper);
        
        // 如果不存在，创建新记录
        if (preference == null) {
            preference = new RunningPreference();
            preference.setUserId(userId);
            preference.setCreateTime(LocalDateTime.now());
        }
        
        // 更新属性
        if (request.getFrequency() != null) {
            preference.setFrequency(request.getFrequency());
        }
        if (request.getPreferredDistance() != null) {
            preference.setPreferredDistance(request.getPreferredDistance());
        }
        if (request.getPace() != null) {
            preference.setPace(request.getPace());
        }
        if (request.getEnvironment() != null) {
            preference.setEnvironment(request.getEnvironment());
        }
        if (request.getMotto() != null) {
            preference.setMotto(request.getMotto());
        }
        
        // 设置更新时间
        preference.setUpdateTime(LocalDateTime.now());
        
        // 保存或更新数据
        if (preference.getId() == null) {
            runningPreferenceMapper.insert(preference);
        } else {
            runningPreferenceMapper.updateById(preference);
        }
        
        // 返回更新后的数据
        return convertToResponse(preference);
    }

    @Override
    public RunningPreferenceResponse getPreferenceByUserId(Long userId) {
        LambdaQueryWrapper<RunningPreference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RunningPreference::getUserId, userId);
        
        RunningPreference preference = runningPreferenceMapper.selectOne(queryWrapper);
        if (preference == null) {
            return null;
        }
        
        return convertToResponse(preference);
    }
    
    /**
     * 将实体对象转换为响应DTO
     */
    private RunningPreferenceResponse convertToResponse(RunningPreference preference) {
        RunningPreferenceResponse response = new RunningPreferenceResponse();
        BeanUtils.copyProperties(preference, response);
        return response;
    }
} 