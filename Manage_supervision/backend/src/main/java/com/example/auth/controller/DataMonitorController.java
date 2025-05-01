package com.example.auth.controller;

import com.example.auth.dto.MonitorDataDTO;
import com.example.auth.dto.MonitorDataQueryDTO;
import com.example.auth.dto.MonitorStatsDTO;
import com.example.auth.entity.MonitorData;
import com.example.auth.service.MonitorDataService;
import com.example.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据监控控制器
 */
@RestController
@RequestMapping("/api/data-monitor")
@RequiredArgsConstructor
public class DataMonitorController {

    private final MonitorDataService monitorDataService;
    private final JwtUtil jwtUtil;

    /**
     * 获取用户监控数据
     */
    @GetMapping("/user-data")
    public ResponseEntity<?> getUserMonitorData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
            @RequestParam(required = false) Double threshold,
            @RequestParam(required = false) Boolean anomalyOnly,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        MonitorDataQueryDTO queryDTO = MonitorDataQueryDTO.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .threshold(threshold)
                .anomalyOnly(anomalyOnly)
                .page(page)
                .size(size)
                .build();

        Page<MonitorDataDTO> dataPage = monitorDataService.getUserMonitorData(queryDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", dataPage.getContent());
        response.put("currentPage", dataPage.getNumber());
        response.put("totalItems", dataPage.getTotalElements());
        response.put("totalPages", dataPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取异常数据
     */
    @GetMapping("/anomaly")
    public ResponseEntity<?> getAnomalyData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "20.0") Double threshold,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        MonitorDataQueryDTO queryDTO = MonitorDataQueryDTO.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .threshold(threshold)
                .page(page)
                .size(size)
                .build();

        Page<MonitorDataDTO> dataPage = monitorDataService.getAnomalyData(queryDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", dataPage.getContent());
        response.put("currentPage", dataPage.getNumber());
        response.put("totalItems", dataPage.getTotalElements());
        response.put("totalPages", dataPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取监控数据统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getMonitorStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        MonitorStatsDTO stats = monitorDataService.getMonitorStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 标记异常为已处理
     */
    @PostMapping("/anomaly/{id}/resolve")
    public ResponseEntity<?> markAnomalyAsResolved(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        boolean success = monitorDataService.markAnomalyAsResolved(id, userId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("数据不存在或无权操作");
        }
    }

    /**
     * 导出监控数据报告
     */
    @GetMapping("/export")
    public void exportMonitorReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
            @RequestParam(required = false) Double threshold,
            @RequestParam(required = false) Boolean anomalyOnly,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }

        MonitorDataQueryDTO queryDTO = MonitorDataQueryDTO.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .threshold(threshold)
                .anomalyOnly(anomalyOnly)
                .build();

        monitorDataService.exportMonitorReport(queryDTO, response);
    }

    /**
     * 添加监控数据（仅供系统内部或其他服务调用）
     */
    @PostMapping("/add")
    public ResponseEntity<?> addMonitorData(
            @RequestBody MonitorData monitorData,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        // 设置用户ID
        monitorData.setUserId(userId);
        
        MonitorData savedData = monitorDataService.addMonitorData(monitorData);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
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