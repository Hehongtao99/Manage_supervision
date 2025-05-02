package com.example.auth.service;

import com.example.auth.model.dto.request.RunningRecordRequest;
import com.example.auth.model.dto.response.RunningRecordResponse;
import com.example.auth.model.dto.response.RunningStatsResponse;
import java.util.List;

/**
 * 跑步记录服务接口
 */
public interface RunningRecordService {
    
    /**
     * 添加跑步记录
     *
     * @param userId 用户ID
     * @param request 跑步记录请求
     * @return 添加后的跑步记录信息
     */
    RunningRecordResponse addRecord(Long userId, RunningRecordRequest request);
    
    /**
     * 获取用户的跑步记录列表
     *
     * @param userId 用户ID
     * @return 跑步记录列表
     */
    List<RunningRecordResponse> getRecordsByUserId(Long userId);
    
    /**
     * 获取指定ID的跑步记录
     *
     * @param recordId 记录ID
     * @return 跑步记录信息
     */
    RunningRecordResponse getRecordById(Long recordId);
    
    /**
     * 获取用户的跑步统计数据（包括周维度跑量、月维度配速等）
     *
     * @param userId 用户ID
     * @return 跑步统计数据
     */
    RunningStatsResponse getUserRunningStats(Long userId);
} 