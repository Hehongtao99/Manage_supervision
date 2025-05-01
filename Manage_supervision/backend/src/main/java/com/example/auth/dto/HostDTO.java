package com.example.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 主机数据传输对象
 */
@Data
public class HostDTO {
    private Long id;
    private String hostname;
    private String ip;
    private String os;
    private String cpuModel;
    private Integer cpuCores;
    private Long memoryTotal; // 单位 MB
    private Long diskTotal; // 单位 MB
    private String status; // online, offline, warning, error
    private LocalDateTime lastUpdateTime;
    private String description;
    private Long userId;
    
    // 最近的监控数据
    private List<Double> cpuUsage; // 最近的CPU使用率数据
    private List<Double> memoryUsage; // 最近的内存使用率数据
    private NetworkTrafficDTO networkTraffic; // 最近的网络流量数据
    
    @Data
    public static class NetworkTrafficDTO {
        private List<Long> input; // 入站流量数据
        private List<Long> output; // 出站流量数据
    }
} 