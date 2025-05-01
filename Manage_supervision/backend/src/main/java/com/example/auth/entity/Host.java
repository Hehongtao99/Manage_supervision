package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 主机信息实体类
 */
@Data
@Entity
@Table(name = "hosts")
@NoArgsConstructor
@AllArgsConstructor
public class Host {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String hostname;
    
    @Column(nullable = false)
    private String ip;
    
    @Column(nullable = false)
    private String os;
    
    @Column(nullable = false)
    private String cpuModel;
    
    @Column(nullable = false)
    private Integer cpuCores;
    
    @Column(nullable = false)
    private Long memoryTotal; // 单位 MB
    
    @Column(nullable = false)
    private Long diskTotal; // 单位 MB
    
    @Column(nullable = false)
    private String status; // online, offline, warning, error
    
    @Column(nullable = false)
    private LocalDateTime lastUpdateTime;
    
    @Column(nullable = true, length = 1000)
    private String description;
    
    @Column(nullable = true)
    private Long userId; // 关联用户ID
    
    @Column(nullable = true)
    private LocalDateTime lastOnline; // 最后在线时间
    
    @Column(nullable = true)
    private LocalDateTime addTime; // 添加时间
} 