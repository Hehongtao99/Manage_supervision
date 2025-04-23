package com.example.auth.service.impl;

import com.example.auth.dto.LogEntryDTO;
import com.example.auth.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    private static final String LOG_DIRECTORY = "logs";
    private static final String LOG_FILE_PREFIX = "system";
    private static final Pattern LOG_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) \\[(.*?)\\] (INFO|WARN|ERROR|DEBUG|TRACE) (.*?) - (.*)");
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    public List<LogEntryDTO> getSystemLogs(String level, String timeRange, int maxCount) {
        try {
            // 获取日志文件列表
            List<File> logFiles = getLogFiles();
            if (logFiles.isEmpty()) {
                logger.warn("未找到系统日志文件");
                return Collections.emptyList();
            }
            
            // 设置时间过滤器
            LocalDateTime startTime = getStartTimeByRange(timeRange);
            
            List<LogEntryDTO> allLogs = new ArrayList<>();
            long entryId = 1;
            
            // 从最新的日志文件开始读取
            for (File logFile : logFiles) {
                if (allLogs.size() >= maxCount) {
                    break;
                }
                
                List<LogEntryDTO> fileEntries = readLogFile(logFile, level, startTime, maxCount - allLogs.size(), entryId);
                allLogs.addAll(fileEntries);
                entryId += fileEntries.size();
            }
            
            // 按时间倒序排序
            Collections.sort(allLogs, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
            
            return allLogs;
        } catch (Exception e) {
            logger.error("读取系统日志失败", e);
            return Collections.emptyList();
        }
    }
    
    private List<File> getLogFiles() throws IOException {
        Path logDir = Paths.get(LOG_DIRECTORY);
        if (!Files.exists(logDir)) {
            logger.warn("日志目录不存在: {}", logDir.toAbsolutePath());
            return Collections.emptyList();
        }
        
        // 获取所有系统日志文件，按照修改时间倒序排列（最新的排在前面）
        List<File> logFiles = Files.list(logDir)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    return fileName.startsWith(LOG_FILE_PREFIX) && fileName.endsWith(".log");
                })
                .map(Path::toFile)
                .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()))
                .collect(Collectors.toList());
        
        return logFiles;
    }
    
    private LocalDateTime getStartTimeByRange(String timeRange) {
        if (timeRange == null) {
            return LocalDateTime.MIN;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (timeRange) {
            case "1h":
                return now.minusHours(1);
            case "24h":
                return now.minusDays(1);
            case "7d":
                return now.minusDays(7);
            case "30d":
                return now.minusDays(30);
            default:
                return LocalDateTime.MIN;
        }
    }
    
    private List<LogEntryDTO> readLogFile(File logFile, String filterLevel, LocalDateTime startTime, int maxCount, long startId) {
        List<LogEntryDTO> entries = new ArrayList<>();
        long currentId = startId;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null && entries.size() < maxCount) {
                LogEntryDTO entry = parseLine(line, currentId);
                if (entry != null) {
                    // 过滤时间
                    if (entry.getTimestamp().isBefore(startTime)) {
                        continue;
                    }
                    
                    // 过滤日志级别
                    if (filterLevel != null && !filterLevel.equals("all") && !entry.getLevel().equalsIgnoreCase(filterLevel)) {
                        continue;
                    }
                    
                    entries.add(entry);
                    currentId++;
                }
            }
        } catch (IOException e) {
            logger.error("读取日志文件失败: {}", logFile.getPath(), e);
        }
        
        return entries;
    }
    
    private LogEntryDTO parseLine(String line, long id) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                String timestampStr = matcher.group(1);
                String threadName = matcher.group(2);
                String level = matcher.group(3);
                String logger = matcher.group(4);
                String message = matcher.group(5);
                
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, LOG_DATE_FORMATTER);
                
                // 确定日志来源
                String source = determineSource(logger);
                
                return new LogEntryDTO(id, timestamp, level, message, source, threadName, logger);
            } catch (DateTimeParseException e) {
                logger.warn("解析日志时间戳失败: {}", line);
            }
        }
        return null;
    }
    
    private String determineSource(String loggerName) {
        if (loggerName.startsWith("org.springframework")) {
            return "Spring框架";
        } else if (loggerName.startsWith("org.hibernate")) {
            return "Hibernate";
        } else if (loggerName.startsWith("com.example.auth")) {
            return "应用";
        } else {
            return "其他";
        }
    }
} 