package com.example.auth.dto;

import java.time.LocalDateTime;

/**
 * 系统日志条目DTO
 */
public class LogEntryDTO {
    private Long id;
    private LocalDateTime timestamp;
    private String level;
    private String message;
    private String source;
    private String threadName;
    private String logger;

    public LogEntryDTO() {
    }

    public LogEntryDTO(Long id, LocalDateTime timestamp, String level, String message, String source, String threadName, String logger) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.source = source;
        this.threadName = threadName;
        this.logger = logger;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }
} 