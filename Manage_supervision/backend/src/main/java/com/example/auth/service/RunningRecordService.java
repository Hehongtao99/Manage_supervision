package com.example.auth.service;

import com.example.auth.model.dto.request.RunningRecordRequest;
import com.example.auth.model.dto.response.RunningRecordResponse;
import com.example.auth.model.dto.response.RunningStatsResponse;
import java.util.List;
import com.example.auth.model.dto.PageResponse;
import java.util.Map;

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
    
    /**
     * 分页获取所有用户的跑步记录（管理员功能）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名筛选条件（可选）
     * @param startDate 开始日期筛选条件（可选）
     * @param endDate 结束日期筛选条件（可选）
     * @return 分页后的跑步记录列表
     */
    PageResponse<Map<String, Object>> getAllUserRunningRecords(
            int page, int size, String username, String startDate, String endDate);
    
    /**
     * 获取用户跑步数据总览统计（管理员功能）
     * 
     * @return 统计数据，包括总跑步记录数、活跃用户数、本周新增记录数等
     */
    Map<String, Object> getRunningStatistics();
    
    /**
     * 删除跑步记录（管理员功能）
     * 
     * @param recordId 记录ID
     * @return 是否删除成功
     */
    boolean deleteRecord(Long recordId);
} 