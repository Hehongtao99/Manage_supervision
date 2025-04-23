package com.example.auth.service;

import com.example.auth.dto.LogEntryDTO;
import java.util.List;

/**
 * 系统日志服务接口
 */
public interface LogService {
    
    /**
     * 获取系统日志
     * @param level 日志级别，可为null表示获取所有级别
     * @param timeRange 时间范围，如"1h", "24h", "7d", "30d"，可为null表示获取所有时间
     * @param maxCount 最大返回条数
     * @return 日志条目列表
     */
    List<LogEntryDTO> getSystemLogs(String level, String timeRange, int maxCount);
} 