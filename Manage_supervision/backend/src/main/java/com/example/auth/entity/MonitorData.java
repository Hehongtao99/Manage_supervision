package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 监控数据实体类
 */
@Entity
@Table(name = "monitor_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 数据类型（如CPU、内存、网络等）
     */
    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    /**
     * 数据值
     */
    @Column(name = "value", nullable = false)
    private Double value;

    /**
     * 预期最小值
     */
    @Column(name = "expected_min")
    private Double expectedMin;

    /**
     * 预期最大值
     */
    @Column(name = "expected_max")
    private Double expectedMax;

    /**
     * 偏差率（百分比）
     */
    @Column(name = "deviation")
    private Double deviation;

    /**
     * 是否为异常数据
     */
    @Column(name = "is_anomaly")
    private Boolean isAnomaly;

    /**
     * 是否已处理
     */
    @Column(name = "resolved")
    private Boolean resolved;

    /**
     * 记录时间
     */
    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 描述或备注
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 数据来源
     */
    @Column(name = "source", length = 100)
    private String source;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 