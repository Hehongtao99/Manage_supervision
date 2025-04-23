package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillGenerateDTO {
    private Long feeStandardId;
    private List<Long> studentIds; // 为空则对所有学生生成账单
    private Long classId; // 指定班级，为空则不按班级筛选
    private LocalDateTime dueDate; // 账单截止日期
}