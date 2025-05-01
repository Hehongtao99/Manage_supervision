package com.example.auth.controller;

import com.example.auth.dto.IntrusionDetectionDTO;
import com.example.auth.dto.MonitorDataDTO;
import com.example.auth.dto.MonitorDataQueryDTO;
import com.example.auth.entity.MonitorData;
import com.example.auth.service.IntrusionDetectionService;
import com.example.auth.service.MonitorDataService;
import com.example.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 入侵检测控制器
 */
@RestController
@RequestMapping("/api/intrusion-detection")
@RequiredArgsConstructor
public class IntrusionDetectionController {

    private final IntrusionDetectionService intrusionDetectionService;
    private final MonitorDataService monitorDataService;
    private final JwtUtil jwtUtil;

    /**
     * 开始入侵检测
     */
    @PostMapping("/detect")
    public ResponseEntity<?> detectIntrusion(
            @RequestBody IntrusionDetectionDTO detectionDTO,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        // 设置执行检测的用户ID
        detectionDTO.setUserId(userId);
        
        // 如果前端没有传入阈值，设置默认值0（表示使用智能检测）
        if (detectionDTO.getThreshold() == null) {
            detectionDTO.setThreshold(0.0);
        }
        
        // 执行检测
        List<MonitorData> anomalies = intrusionDetectionService.detectIntrusion(detectionDTO);
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("anomalies", anomalies);
        result.put("detectionType", detectionDTO.getType());
        result.put("detectionTime", LocalDateTime.now());
        result.put("anomalyCount", anomalies.size());
        
        // 添加详细的检测结果摘要
        Map<String, Integer> typeCounts = new HashMap<>();
        for (MonitorData data : anomalies) {
            String type = data.getDataType();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        result.put("typeSummary", typeCounts);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 将检测结果添加到监控数据
     */
    @PostMapping("/add-to-monitor")
    public ResponseEntity<?> addToMonitor(
            @RequestBody Map<String, List<Map<String, Object>>> requestBody,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        
        List<Map<String, Object>> anomalies = requestBody.get("anomalies");
        if (anomalies == null || anomalies.isEmpty()) {
            return ResponseEntity.badRequest().body("无效的请求数据");
        }
        
        // 将前端数据转换为MonitorData对象并保存
        List<MonitorData> savedData = anomalies.stream()
                .map(item -> {
                    MonitorData data = new MonitorData();
                    data.setUserId(userId);
                    data.setDataType(String.valueOf(item.get("dataType")));
                    data.setValue(Double.valueOf(String.valueOf(item.get("value"))));
                    data.setExpectedMin(Double.valueOf(String.valueOf(item.get("expectedMin"))));
                    data.setExpectedMax(Double.valueOf(String.valueOf(item.get("expectedMax"))));
                    data.setDeviation(Double.valueOf(String.valueOf(item.get("deviation"))));
                    data.setIsAnomaly(true);
                    data.setResolved(false);
                    data.setRecordTime(LocalDateTime.now());
                    data.setSource("入侵检测");
                    data.setDescription("通过入侵检测发现的异常数据");
                    
                    return monitorDataService.addMonitorData(data);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", savedData.size(),
                "message", "已成功添加到数据监控"
        ));
    }

    /**
     * 获取最近的检测记录
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentDetections(
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        
        List<Map<String, Object>> recentDetections = intrusionDetectionService.getRecentDetections(userId, limit);
        return ResponseEntity.ok(recentDetections);
    }

    /**
     * 获取检测统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDetectionStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }
        
        Map<String, Object> stats = intrusionDetectionService.getDetectionStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = jwtUtil.getTokenFromRequest(request);
        if (token != null) {
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }
} 