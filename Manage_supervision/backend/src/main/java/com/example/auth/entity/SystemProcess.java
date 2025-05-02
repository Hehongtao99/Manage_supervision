package com.example.auth.entity;

import java.time.LocalDateTime;

/**
 * 系统进程信息实体类
 */
public class SystemProcess {
    private Long pid;           // 进程ID
    private String name;        // 进程名称
    private Double cpuUsage;    // CPU占用率
    private Long memoryUsage;   // 内存占用(KB)
    private LocalDateTime startTime; // 启动时间
    private String command;     // 命令行

    public SystemProcess() {
    }

    public SystemProcess(Long pid, String name, Double cpuUsage, Long memoryUsage, LocalDateTime startTime, String command) {
        this.pid = pid;
        this.name = name;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
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