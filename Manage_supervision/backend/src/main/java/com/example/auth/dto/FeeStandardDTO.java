package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeStandardDTO {
    private Long id;
    private String feeName;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 