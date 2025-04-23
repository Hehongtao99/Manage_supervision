package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentDTO {
    private String paymentMethod; // WECHAT, ALIPAY
    private String paymentMessage;
} 