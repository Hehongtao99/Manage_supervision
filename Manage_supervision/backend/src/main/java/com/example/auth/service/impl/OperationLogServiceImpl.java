package com.example.auth.service.impl;

import com.example.auth.dto.OperationLogDTO;
import com.example.auth.entity.OperationLog;
import com.example.auth.repository.OperationLogRepository;
import com.example.auth.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogServiceImpl.class);
    
    @Autowired
    private OperationLogRepository operationLogRepository;
    
    @Override
    @Transactional
    public OperationLog recordLog(Long userId, String username, String operation, 
                               String method, String params, String result, 
                               String module, HttpServletRequest request) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setOperation(operation);
            log.setMethod(method);
            
            // 处理参数内容，防止过长
            if (params != null && params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
            log.setParams(params);
            
            log.setResult(result);
            log.setOperationTime(LocalDateTime.now());
            
            // 获取客户端IP
            String ip = getIpAddress(request);
            log.setIp(ip);
            
            // 获取用户代理信息
            String userAgent = request.getHeader("User-Agent");
            log.setUserAgent(userAgent);
            
            log.setModule(module);
            
            return operationLogRepository.save(log);
        } catch (Exception e) {
            logger.error("记录用户操作日志失败", e);
            return null;
        }
    }
    
    @Override
    @Async
    public void recordLogAsync(Long userId, String username, String operation, 
                            String method, String params, String result, 
                            String module, HttpServletRequest request) {
        try {
            recordLog(userId, username, operation, method, params, result, module, request);
        } catch (Exception e) {
            logger.error("异步记录用户操作日志失败", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OperationLogDTO> findByConditions(String username, String operation, 
                                             LocalDateTime startTime, LocalDateTime endTime, 
                                             String module, Pageable pageable) {
        return operationLogRepository.findByConditions(
                username, operation, startTime, endTime, module, pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OperationLogDTO findById(Long id) {
        Optional<OperationLog> optLog = operationLogRepository.findById(id);
        return optLog.map(this::convertToDTO).orElse(null);
    }
    
    @Override
    @Transactional
    public long deleteLogsBefore(LocalDateTime time) {
        try {
            // JPA没有直接提供批量删除的方法，需要自定义JPQL或原生SQL
            // 这里简单实现，实际项目中可以优化为批量删除
            Iterable<OperationLog> logs = operationLogRepository.findAll();
            long count = 0;
            
            for (OperationLog log : logs) {
                if (log.getOperationTime().isBefore(time)) {
                    operationLogRepository.delete(log);
                    count++;
                }
            }
            
            return count;
        } catch (Exception e) {
            logger.error("删除历史操作日志失败", e);
            return 0;
        }
    }
    
    /**
     * 将Entity转换为DTO
     */
    private OperationLogDTO convertToDTO(OperationLog log) {
        return new OperationLogDTO(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getOperation(),
                log.getMethod(),
                log.getParams(),
                log.getResult(),
                log.getIp(),
                log.getUserAgent(),
                log.getModule(),
                log.getOperationTime()
        );
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        
        return ip;
    }
} 