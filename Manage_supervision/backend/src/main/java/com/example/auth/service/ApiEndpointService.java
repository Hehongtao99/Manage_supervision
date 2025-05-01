package com.example.auth.service;

import com.example.auth.dto.ApiEndpointDTO;
import com.example.auth.entity.ApiEndpoint;
import com.example.auth.repository.ApiEndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiEndpointService {
    
    @Autowired
    private ApiEndpointRepository apiEndpointRepository;
    
    /**
     * 获取所有API接口
     */
    public List<ApiEndpointDTO> getAllApiEndpoints() {
        return apiEndpointRepository.findAllByOrderByPathAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 按控制器分组获取API接口
     */
    public Map<String, List<ApiEndpointDTO>> getApiEndpointsByController() {
        List<ApiEndpointDTO> allEndpoints = getAllApiEndpoints();
        
        return allEndpoints.stream()
                .collect(Collectors.groupingBy(
                        ApiEndpointDTO::getControllerName,
                        Collectors.toList()
                ));
    }
    
    /**
     * 获取所有控制器名称
     */
    public List<String> getAllControllerNames() {
        return apiEndpointRepository.findAllControllerNames();
    }
    
    /**
     * 根据控制器名称获取API接口
     */
    public List<ApiEndpointDTO> getApiEndpointsByControllerName(String controllerName) {
        return apiEndpointRepository.findByControllerNameOrderByPathAsc(controllerName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取API访问统计信息
     */
    public Map<String, Long> getApiAccessStats() {
        List<ApiEndpointDTO> allEndpoints = getAllApiEndpoints();
        
        // 计算总访问次数
        long totalAccess = allEndpoints.stream()
                .mapToLong(ApiEndpointDTO::getAccessCount)
                .sum();
        
        // 获取访问最多的前10个接口
        Map<String, Long> topAccessed = allEndpoints.stream()
                .sorted((a, b) -> b.getAccessCount().compareTo(a.getAccessCount()))
                .limit(10)
                .collect(Collectors.toMap(
                        e -> e.getMethod() + " " + e.getPath(),
                        ApiEndpointDTO::getAccessCount
                ));
        
        // 获取平均响应时间最长的前10个接口
        Map<String, Long> slowestApis = allEndpoints.stream()
                .filter(e -> e.getAccessCount() > 0) // 只考虑有访问记录的接口
                .sorted((a, b) -> b.getAverageResponseTime().compareTo(a.getAverageResponseTime()))
                .limit(10)
                .collect(Collectors.toMap(
                        e -> e.getMethod() + " " + e.getPath(),
                        ApiEndpointDTO::getAverageResponseTime
                ));
        
        // 合并结果
        Map<String, Long> result = Map.of(
                "totalEndpoints", (long) allEndpoints.size(),
                "totalAccess", totalAccess
        );
        
        return result;
    }
    
    /**
     * 实体转DTO
     */
    private ApiEndpointDTO convertToDTO(ApiEndpoint entity) {
        ApiEndpointDTO dto = new ApiEndpointDTO();
        dto.setId(entity.getId());
        dto.setPath(entity.getPath());
        dto.setMethod(entity.getMethod());
        dto.setControllerName(entity.getControllerName());
        dto.setMethodName(entity.getMethodName());
        dto.setRequestParams(entity.getRequestParams());
        dto.setDescription(entity.getDescription());
        dto.setRequiredRoles(entity.getRequiredRoles());
        dto.setLastAccessed(entity.getLastAccessed());
        dto.setAccessCount(entity.getAccessCount());
        dto.setAverageResponseTime(entity.getAverageResponseTime());
        return dto;
    }
} 