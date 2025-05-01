package com.example.auth.entity;

import java.time.LocalDateTime;

/**
 * 系统进程信息实体类
 */
public class SystemProcess {
    private Long pid;           // 进程ID
    private String name;        // 进程名称
    private String user;        // 用户
    private Double cpuUsage;    // CPU占用率
    private Long memoryUsage;   // 内存占用(KB)
    private String status;      // 进程状态
    private LocalDateTime startTime; // 启动时间
    private String command;     // 命令行

    public SystemProcess() {
    }

    public SystemProcess(Long pid, String name, String user, Double cpuUsage, Long memoryUsage, String status, LocalDateTime startTime, String command) {
        this.pid = pid;
        this.name = name;
        this.user = user;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.status = status;
        this.startTime = startTime;
        this.command = command;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
} 