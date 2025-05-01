package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据上报查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataReportQueryDTO {

    private Long reporterId;
    private Long handlerId;
    private Integer status;
    private Long monitorDataId;
    private Integer severity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer page;
    private Integer size;
    private String keyword;
} 