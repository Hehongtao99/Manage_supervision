package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.OperationLogDTO;
import com.example.auth.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户操作日志控制器
 */
@RestController
@RequestMapping("/api/logs")
public class OperationLogController {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogController.class);
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 分页查询操作日志
     * @param username 用户名（可选）
     * @param operation 操作描述（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param module 模块（可选）
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/operations")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getOperationLogs(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "operation", required = false) String operation,
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        logger.info("获取用户操作日志，用户名: {}, 操作: {}, 开始日期: {}, 结束日期: {}, 模块: {}, 页码: {}, 每页大小: {}", 
                username, operation, startDate, endDate, module, page, size);
        
        try {
            // 处理日期参数
            LocalDateTime startTime = startDate != null ? 
                    LocalDateTime.of(startDate, LocalTime.MIN) : null;
            LocalDateTime endTime = endDate != null ? 
                    LocalDateTime.of(endDate, LocalTime.MAX) : null;
            
            // 创建分页请求对象，按操作时间降序排序
            PageRequest pageRequest = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "operationTime"));
            
            // 查询操作日志
            Page<OperationLogDTO> logs = operationLogService.findByConditions(
                    username, operation, startTime, endTime, module, pageRequest);
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("content", logs.getContent());
            result.put("totalElements", logs.getTotalElements());
            result.put("totalPages", logs.getTotalPages());
            result.put("size", logs.getSize());
            result.put("number", logs.getNumber());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取用户操作日志失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取用户操作日志失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取日志详情
     * @param id 日志ID
     * @return 日志详情
     */
    @GetMapping("/operations/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getOperationLogDetail(@PathVariable Long id) {
        try {
            OperationLogDTO log = operationLogService.findById(id);
            if (log != null) {
                return ResponseEntity.ok(log);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("获取操作日志详情失败，ID: " + id, e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取操作日志详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除指定日期之前的操作日志
     * @param date 日期
     * @return 删除结果
     */
    @DeleteMapping("/operations")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteOperationLogsBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDateTime time = LocalDateTime.of(date, LocalTime.MIN);
            long count = operationLogService.deleteLogsBefore(time);
            return ResponseEntity.ok(Map.of(
                    "message", "成功删除" + count + "条历史操作日志",
                    "count", count
            ));
        } catch (Exception e) {
            logger.error("删除历史操作日志失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "删除历史操作日志失败: " + e.getMessage()));
        }
    }
} 