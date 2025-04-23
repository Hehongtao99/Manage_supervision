package com.example.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bill")
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
    
    @Column(name = "fee_standard_id", nullable = false)
    private Long feeStandardId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_standard_id", insertable = false, updatable = false)
    private FeeStandard feeStandard;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column
    private Boolean status = false; // 0未支付，1已支付
    
    @Column
    private LocalDateTime paymentTime;
    
    @Column(length = 20)
    private String paymentMethod; // WECHAT, ALIPAY
    
    @Column(length = 255)
    private String paymentMessage;
    
    @Column(nullable = false)
    private LocalDateTime dueDate;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Boolean isDeleted;
    
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (status == null) {
            status = false;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
} 