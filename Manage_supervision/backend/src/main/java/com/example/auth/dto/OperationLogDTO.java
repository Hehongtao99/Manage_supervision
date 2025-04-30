package com.example.auth.dto;

import java.time.LocalDateTime;

/**
 * 用户操作日志DTO
 */
public class OperationLogDTO {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String result;
    private String ip;
    private String userAgent;
    private String module;
    private LocalDateTime operationTime;

    public OperationLogDTO() {
    }

    public OperationLogDTO(Long id, Long userId, String username, String operation,
                          String method, String params, String result, String ip,
                          String userAgent, String module, LocalDateTime operationTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.operation = operation;
        this.method = method;
        this.params = params;
        this.result = result;
        this.ip = ip;
        this.userAgent = userAgent;
        this.module = module;
        this.operationTime = operationTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }
} 