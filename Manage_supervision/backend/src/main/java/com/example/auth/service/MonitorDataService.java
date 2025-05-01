package com.example.auth.service;

import com.example.auth.dto.MonitorDataDTO;
import com.example.auth.dto.MonitorDataQueryDTO;
import com.example.auth.dto.MonitorStatsDTO;
import com.example.auth.entity.MonitorData;
import org.springframework.data.domain.Page;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MonitorDataService {

    /**
     * 查询用户监控数据
     * @param queryDTO 查询参数
     * @return 分页监控数据
     */
    Page<MonitorDataDTO> getUserMonitorData(MonitorDataQueryDTO queryDTO);

    /**
     * 查询异常数据
     * @param queryDTO 查询参数
     * @return 分页异常数据
     */
    Page<MonitorDataDTO> getAnomalyData(MonitorDataQueryDTO queryDTO);

    /**
     * 获取监控数据统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    MonitorStatsDTO getMonitorStats(Long userId);

    /**
     * 标记异常数据为已处理
     * @param id 数据ID
     * @param userId 用户ID
     * @return 操作结果
     */
    boolean markAnomalyAsResolved(Long id, Long userId);

    /**
     * 导出监控数据报告
     * @param queryDTO 查询参数
     * @param response HTTP响应
     * @throws IOException 导出异常
     */
    void exportMonitorReport(MonitorDataQueryDTO queryDTO, HttpServletResponse response) throws IOException;

    /**
     * 添加监控数据
     * @param monitorData 监控数据
     * @return 保存的数据
     */
    MonitorData addMonitorData(MonitorData monitorData);
    
    /**
     * 获取资源使用情况摘要
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 资源使用统计
     */
    Map<String, Object> getResourceUsageSummary(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 按数据类型和时间范围获取监控数据
     * @param dataType 数据类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 监控数据列表
     */
    List<MonitorData> getDataByTypeAndTimeRange(String dataType, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取异常数据数量
     * @param unresolvedOnly 是否只统计未处理的异常
     * @return 异常数量
     */
    long getAnomalyCount(boolean unresolvedOnly);
    
    /**
     * 获取最近的异常数据数量（仅统计最近24小时内的异常）
     * @return 异常数量
     */
    long getRecentAnomalyCount();
    
    /**
     * 标记异常数据为已处理（简化版本）
     * @param id 数据ID
     * @return 操作结果
     */
    boolean markAnomalyAsResolved(Long id);
    
    /**
     * 获取指定时间范围内的异常数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 最大返回数量
     * @return 异常数据列表
     */
    List<MonitorData> getAnomalyData(LocalDateTime startTime, LocalDateTime endTime, int limit);
} 