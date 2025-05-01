package com.example.auth.controller;

import com.example.auth.service.ResourceOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 资源总览控制器
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceOverviewController {

    @Autowired
    private ResourceOverviewService resourceOverviewService;
    
    /**
     * 获取资源总览数据
     * @return 资源总览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getResourceOverview() {
        return ResponseEntity.ok(resourceOverviewService.getResourceOverview());
    }
    
    /**
     * 获取资源使用趋势数据
     * @param timeRange 时间范围，可选 "hour"（1小时）或 "day"（24小时），默认为 "hour"
     * @return 资源使用趋势数据
     */
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getResourceUsageTrend(
            @RequestParam(value = "timeRange", defaultValue = "hour") String timeRange) {
        return ResponseEntity.ok(resourceOverviewService.getResourceUsageTrend(timeRange));
    }
} 