package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.LogEntryDTO;
import com.example.auth.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统日志控制器
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {
    
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    
    @Autowired
    private LogService logService;
    
    /**
     * 获取系统日志
     * @param level 日志级别 (all, info, warning, error)
     * @param timeRange 时间范围 (1h, 24h, 7d, 30d)
     * @return 日志条目列表
     */
    @GetMapping("/system")
    @RequireRole("ADMIN")
    public ResponseEntity<List<LogEntryDTO>> getSystemLogs(
            @RequestParam(value = "level", required = false, defaultValue = "all") String level,
            @RequestParam(value = "timeRange", required = false, defaultValue = "24h") String timeRange) {
        
        logger.info("获取系统日志，级别: {}, 时间范围: {}", level, timeRange);
        
        // 转换日志级别
        String logbackLevel = convertToLogbackLevel(level);
        
        // 获取最近200条符合条件的日志
        List<LogEntryDTO> logs = logService.getSystemLogs(logbackLevel, timeRange, 200);
        
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 将前端日志级别转换为Logback级别
     */
    private String convertToLogbackLevel(String level) {
        if (level == null || "all".equals(level)) {
            return null;
        }
        
        switch (level.toLowerCase()) {
            case "info":
                return "INFO";
            case "warning":
                return "WARN";
            case "error":
                return "ERROR";
            default:
                return null;
        }
    }
} 