package com.example.auth.service.impl;

import com.example.auth.dto.ApiEndpointDTO;
import com.example.auth.dto.HostDTO;
import com.example.auth.entity.SystemProcess;
import com.example.auth.service.ApiEndpointService;
import com.example.auth.service.HostService;
import com.example.auth.service.ResourceOverviewService;
import com.example.auth.service.SystemProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 资源总览服务实现类
 */
@Service
public class ResourceOverviewServiceImpl implements ResourceOverviewService {

    @Autowired
    private HostService hostService;
    
    @Autowired
    private SystemProcessService systemProcessService;
    
    @Autowired
    private ApiEndpointService apiEndpointService;
    
    /**
     * 获取资源总览数据
     */
    @Override
    public Map<String, Object> getResourceOverview() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取主机统计信息
        Map<String, Integer> hostStats = hostService.getHostStatusStats();
        result.put("hostStats", hostStats);
        
        // 获取最近的主机列表
        List<HostDTO> hosts = hostService.getHostsByStatus("online");
        List<HostDTO> recentHosts = hosts.stream()
                .limit(5)
                .collect(Collectors.toList());
        result.put("recentHosts", recentHosts);
        
        // 获取进程统计信息
        Map<String, Integer> processStats = new HashMap<>();
        processStats.put("running", systemProcessService.getProcessListWithoutRefresh().size());
        processStats.put("total", processStats.get("running"));
        result.put("processStats", processStats);
        
        // 获取CPU占用最高的Top 5进程
        List<SystemProcess> topProcesses = systemProcessService.getTopProcesses(5, true);
        result.put("topProcesses", topProcesses);
        
        // 获取系统信息
        Map<String, Object> systemInfo = new HashMap<>();
        // 获取当前主机信息作为系统状态
        HostDTO currentHost = hostService.getCurrentHostInfo();
        // 如果主机有CPU使用率数组，取最后一个值
        double cpuUsage = 0;
        if (currentHost.getCpuUsage() != null && !currentHost.getCpuUsage().isEmpty()) {
            cpuUsage = currentHost.getCpuUsage().get(currentHost.getCpuUsage().size() - 1);
        }
        systemInfo.put("cpuUsage", cpuUsage);
        
        // 获取内存使用率
        double memoryUsage = 0;
        if (currentHost.getMemoryUsage() != null && !currentHost.getMemoryUsage().isEmpty()) {
            memoryUsage = currentHost.getMemoryUsage().get(currentHost.getMemoryUsage().size() - 1);
        }
        systemInfo.put("memoryUsage", memoryUsage);
        
        // 磁盘使用率（模拟数据）
        systemInfo.put("diskUsage", 65.0); // 模拟值
        
        result.put("systemInfo", systemInfo);
        
        // 获取接口统计信息
        Map<String, Long> apiStats = apiEndpointService.getApiAccessStats();
        
        // 添加平均响应时间（模拟数据）
        Map<String, Object> enhancedApiStats = new HashMap<>(apiStats);
        enhancedApiStats.put("responseTime", 135L); // 模拟值，平均响应时间135ms
        
        result.put("apiStats", enhancedApiStats);
        
        // 获取最近访问的接口
        List<ApiEndpointDTO> allApis = apiEndpointService.getAllApiEndpoints();
        List<ApiEndpointDTO> recentApis = allApis.stream()
                .filter(api -> api.getLastAccessed() != null)
                .sorted((a, b) -> b.getLastAccessed().compareTo(a.getLastAccessed()))
                .limit(5)
                .collect(Collectors.toList());
        
        result.put("recentApis", recentApis);
        
        return result;
    }
    
    /**
     * 获取资源使用趋势数据
     */
    @Override
    public Map<String, Object> getResourceUsageTrend(String timeRange) {
        Map<String, Object> result = new HashMap<>();
        
        // 确定时间范围
        int dataPoints = "hour".equals(timeRange) ? 60 : 24; // 一小时60个点，一天24个点
        int interval = "hour".equals(timeRange) ? 1 : 60; // 一小时每分钟一个点，一天每小时一个点
        
        // 生成时间戳
        List<String> timestamps = new ArrayList<>();
        List<Double> cpuData = new ArrayList<>();
        List<Double> memoryData = new ArrayList<>();
        List<Double> networkInputData = new ArrayList<>();
        List<Double> networkOutputData = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        
        // 获取当前主机信息，用于基准值
        HostDTO currentHost = hostService.getCurrentHostInfo();
        double baseCpuUsage = 30.0; // 基准CPU使用率
        double baseMemoryUsage = 40.0; // 基准内存使用率
        
        // 如果主机有实际数据，使用实际数据作为基准
        if (currentHost.getCpuUsage() != null && !currentHost.getCpuUsage().isEmpty()) {
            baseCpuUsage = currentHost.getCpuUsage().get(currentHost.getCpuUsage().size() - 1);
        }
        if (currentHost.getMemoryUsage() != null && !currentHost.getMemoryUsage().isEmpty()) {
            baseMemoryUsage = currentHost.getMemoryUsage().get(currentHost.getMemoryUsage().size() - 1);
        }
        
        // 根据时间范围生成数据点
        for (int i = dataPoints - 1; i >= 0; i--) {
            LocalDateTime pointTime = "hour".equals(timeRange) 
                ? now.minusMinutes(i * interval) 
                : now.minusHours(i * interval);
            
            timestamps.add(pointTime.format(DateTimeFormatter.ISO_DATE_TIME));
            
            // 生成CPU使用率数据，基于基准值上下浮动
            double cpuUsage = Math.min(99.0, Math.max(1.0, baseCpuUsage + (random.nextDouble() - 0.5) * 20));
            cpuData.add(Math.round(cpuUsage * 10) / 10.0); // 保留一位小数
            
            // 生成内存使用率数据
            double memoryUsage = Math.min(99.0, Math.max(1.0, baseMemoryUsage + (random.nextDouble() - 0.5) * 15));
            memoryData.add(Math.round(memoryUsage * 10) / 10.0); // 保留一位小数
            
            // 生成网络流量数据（KB/s）
            double networkInput = 1000 + random.nextDouble() * 5000;
            double networkOutput = 500 + random.nextDouble() * 3000;
            networkInputData.add((double) Math.round(networkInput));
            networkOutputData.add((double) Math.round(networkOutput));
        }
        
        // 组装结果
        result.put("timestamps", timestamps);
        result.put("cpu", cpuData);
        result.put("memory", memoryData);
        Map<String, List<Double>> network = new HashMap<>();
        network.put("input", networkInputData);
        network.put("output", networkOutputData);
        result.put("network", network);
        
        return result;
    }
} 