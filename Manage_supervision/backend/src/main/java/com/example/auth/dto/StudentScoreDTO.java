package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 学生分数信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentScoreDTO {
    private Long studentId;
    private String studentName;       // 学生姓名
    private String studentUserNumber; // 学号/工号
    private BigDecimal score;         // 分数
} 