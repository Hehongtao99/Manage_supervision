package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异常数据上报实体类
 */
@Entity
@Table(name = "data_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 上报人ID
     */
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;
    
    /**
     * 被上报的监控数据ID
     */
    @Column(name = "monitor_data_id")
    private Long monitorDataId;

    /**
     * 上报标题
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 详细描述
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * 上报级别（1-低，2-中，3-高）
     */
    @Column(name = "severity", nullable = false)
    private Integer severity;

    /**
     * 上报状态（0-待处理，1-处理中，2-已解决，3-已关闭）
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 处理人ID
     */
    @Column(name = "handler_id")
    private Long handlerId;

    /**
     * 处理结果描述
     */
    @Column(name = "resolution", length = 1000)
    private String resolution;
    
    /**
     * 上报时间
     */
    @Column(name = "report_time", nullable = false)
    private LocalDateTime reportTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 关闭时间
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        reportTime = LocalDateTime.now();
        status = 0; // 默认为待处理状态
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 