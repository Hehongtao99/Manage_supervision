package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 主机监控指标实体类
 */
@Data
@Entity
@Table(name = "host_metrics")
@NoArgsConstructor
@AllArgsConstructor
public class HostMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long hostId;
    
    @Column(nullable = false)
    private Double cpuUsage; // CPU使用率，百分比
    
    @Column(nullable = false)
    private Double memoryUsage; // 内存使用率，百分比
    
    @Column(nullable = false)
    private Long memoryUsed; // 已用内存，单位MB
    
    @Column(nullable = false)
    private Double diskUsage; // 磁盘使用率，百分比
    
    @Column(nullable = false)
    private Long diskUsed; // 已用磁盘空间，单位MB
    
    @Column(nullable = false)
    private Long networkIn; // 入站流量，单位KB/s
    
    @Column(nullable = false)
    private Long networkOut; // 出站流量，单位KB/s
    
    @Column(nullable = false)
    private LocalDateTime collectionTime; // 采集时间
} 