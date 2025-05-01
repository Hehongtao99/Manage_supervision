package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 入侵检测参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntrusionDetectionDTO {
    
    /**
     * 检测类型（full-全面检测，cpu-CPU异常，memory-内存异常，network-网络异常，disk-磁盘异常, 
     * process-进程异常，logs-系统日志，ports-端口扫描，files-文件完整性，users-用户活动，
     * services-系统服务，registry-注册表，malware-恶意软件）
     */
    private String type;
    
    /**
     * 异常阈值（百分比）
     */
    private Double threshold;
    
    /**
     * 执行检测的用户ID
     */
    private Long userId;
} 