package com.example.auth.controller;

import com.example.auth.dto.DataReportDTO;
import com.example.auth.dto.DataReportQueryDTO;
import com.example.auth.entity.DataReport;
import com.example.auth.service.DataReportService;
import com.example.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据上报控制器
 */
@RestController
@RequestMapping("/api/data-report")
@RequiredArgsConstructor
public class DataReportController {

    private final DataReportService dataReportService;
    private final JwtUtil jwtUtil;

    /**
     * 提交数据上报
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitDataReport(@RequestBody DataReport dataReport, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        // 设置上报人ID
        dataReport.setReporterId(userId);
        
        DataReport savedReport = dataReportService.createDataReport(dataReport);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
    }

    /**
     * 查询数据上报列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getDataReports(
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long monitorDataId,
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        DataReportQueryDTO queryDTO = DataReportQueryDTO.builder()
                .reporterId(reporterId)
                .handlerId(handlerId)
                .status(status)
                .monitorDataId(monitorDataId)
                .severity(severity)
                .startDate(startDate)
                .endDate(endDate)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        Page<DataReportDTO> reportPage = dataReportService.queryDataReports(queryDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", reportPage.getContent());
        response.put("currentPage", reportPage.getNumber());
        response.put("totalItems", reportPage.getTotalElements());
        response.put("totalPages", reportPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取数据上报详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDataReportDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        DataReportDTO reportDTO = dataReportService.getDataReportDetail(id);
        if (reportDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("数据上报不存在");
        }
        
        return ResponseEntity.ok(reportDTO);
    }

    /**
     * 处理数据上报
     */
    @PostMapping("/{id}/handle")
    public ResponseEntity<?> handleDataReport(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String resolution,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        DataReportDTO reportDTO = dataReportService.handleDataReport(id, userId, status, resolution);
        if (reportDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("数据上报不存在");
        }
        
        return ResponseEntity.ok(reportDTO);
    }

    /**
     * 获取用户提交的上报
     */
    @GetMapping("/my-reports")
    public ResponseEntity<?> getUserSubmittedReports(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        Page<DataReportDTO> reportPage = dataReportService.getUserSubmittedReports(userId, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", reportPage.getContent());
        response.put("currentPage", reportPage.getNumber());
        response.put("totalItems", reportPage.getTotalElements());
        response.put("totalPages", reportPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取待处理上报数量
     */
    @GetMapping("/pending-count")
    public ResponseEntity<?> getPendingReportCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        long pendingCount = dataReportService.countPendingReports();
        return ResponseEntity.ok(Map.of("count", pendingCount));
    }

    /**
     * 获取上报状态统计
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getReportStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        Map<Integer, Long> statsMap = dataReportService.getReportStatusStats();
        return ResponseEntity.ok(statsMap);
    }

    /**
     * 获取最近上报列表
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentReports(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登录");
        }

        List<DataReportDTO> reports = dataReportService.getRecentReports();
        return ResponseEntity.ok(reports);
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