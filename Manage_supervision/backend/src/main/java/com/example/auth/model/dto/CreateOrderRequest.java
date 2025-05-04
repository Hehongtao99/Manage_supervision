package com.example.auth.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {
    
    private Long serviceId;
    
    private Integer hours;
    
    private LocalDateTime appointedTime;
    
    private String remark;
} 