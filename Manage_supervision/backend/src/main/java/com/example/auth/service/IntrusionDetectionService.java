package com.example.auth.service;

import com.example.auth.dto.IntrusionDetectionDTO;
import com.example.auth.entity.MonitorData;

import java.util.List;
import java.util.Map;

/**
 * 入侵检测服务接口
 */
public interface IntrusionDetectionService {

    /**
     * 执行入侵检测
     * @param detectionDTO 检测参数
     * @return 检测到的异常数据列表
     */
    List<MonitorData> detectIntrusion(IntrusionDetectionDTO detectionDTO);
    
    /**
     * 获取最近的检测记录
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 检测记录列表
     */
    List<Map<String, Object>> getRecentDetections(Long userId, Integer limit);
    
    /**
     * 获取检测统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getDetectionStats(Long userId);
} 