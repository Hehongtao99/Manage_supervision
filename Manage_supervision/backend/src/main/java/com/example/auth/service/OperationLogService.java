package com.example.auth.service;

import com.example.auth.dto.OperationLogDTO;
import com.example.auth.entity.OperationLog;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 用户操作日志服务接口
 */
public interface OperationLogService {
    
    /**
     * 记录用户操作日志
     * @param userId 用户ID
     * @param username 用户名
     * @param operation 操作描述
     * @param method 请求方法
     * @param params 请求参数
     * @param result 操作结果
     * @param module 操作模块
     * @param request HTTP请求对象
     * @return 记录的日志实体
     */
    OperationLog recordLog(Long userId, String username, String operation, 
                         String method, String params, String result, 
                         String module, HttpServletRequest request);
    
    /**
     * 异步记录用户操作日志
     * @param userId 用户ID
     * @param username 用户名
     * @param operation 操作描述
     * @param method 请求方法
     * @param params 请求参数
     * @param result 操作结果
     * @param module 操作模块
     * @param request HTTP请求对象
     */
    void recordLogAsync(Long userId, String username, String operation, 
                       String method, String params, String result, 
                       String module, HttpServletRequest request);
    
    /**
     * 分页查询操作日志
     * @param username 用户名（可选）
     * @param operation 操作描述（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param module 模块（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<OperationLogDTO> findByConditions(String username, String operation, 
                                        LocalDateTime startTime, LocalDateTime endTime, 
                                        String module, Pageable pageable);
    
    /**
     * 根据ID获取操作日志
     * @param id 日志ID
     * @return 操作日志DTO
     */
    OperationLogDTO findById(Long id);
    
    /**
     * 删除指定时间之前的操作日志
     * @param time 时间点
     * @return 删除的记录数
     */
    long deleteLogsBefore(LocalDateTime time);
} 