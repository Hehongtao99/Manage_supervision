package com.example.auth.dto;

import java.time.LocalDateTime;

public class ApiEndpointDTO {
    private Long id;
    private String path;
    private String method;
    private String controllerName;
    private String methodName;
    private String requestParams;
    private String description;
    private String requiredRoles;
    private LocalDateTime lastAccessed;
    private Long accessCount;
    private Long averageResponseTime;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getControllerName() {
        return controllerName;
    }
    
    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getRequestParams() {
        return requestParams;
    }
    
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRequiredRoles() {
        return requiredRoles;
    }
    
    public void setRequiredRoles(String requiredRoles) {
        this.requiredRoles = requiredRoles;
    }
    
    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }
    
    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
    
    public Long getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }
    
    public Long getAverageResponseTime() {
        return averageResponseTime;
    }
    
    public void setAverageResponseTime(Long averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
} 