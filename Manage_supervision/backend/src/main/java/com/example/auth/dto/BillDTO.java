package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long feeStandardId;
    private String feeName;
    private BigDecimal amount;
    private Boolean status;  // 0未支付，1已支付
    private LocalDateTime paymentTime;
    private String paymentMethod; // WECHAT, ALIPAY
    private String paymentMessage;
    private LocalDateTime dueDate;
    private LocalDateTime createTime;
} 