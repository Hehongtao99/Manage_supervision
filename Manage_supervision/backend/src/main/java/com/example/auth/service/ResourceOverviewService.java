package com.example.auth.service;

import java.util.Map;

/**
 * 资源总览服务接口
 */
public interface ResourceOverviewService {
    
    /**
     * 获取资源总览数据
     * @return 包含主机、进程、接口和系统信息的综合数据
     */
    Map<String, Object> getResourceOverview();
    
    /**
     * 获取资源使用趋势数据
     * @param timeRange 时间范围，可选 "hour"（1小时）或 "day"（24小时）
     * @return 资源使用趋势数据
     */
    Map<String, Object> getResourceUsageTrend(String timeRange);
} 