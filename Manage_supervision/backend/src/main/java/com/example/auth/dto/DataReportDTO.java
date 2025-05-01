package com.example.auth.dto;

import com.example.auth.entity.DataReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据上报DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataReportDTO {

    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long monitorDataId;
    private String title;
    private String description;
    private Integer severity;
    private Integer status;
    private Long handlerId;
    private String handlerName;
    private String resolution;
    private LocalDateTime reportTime;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    // 附加信息
    private MonitorDataDTO monitorData;
    
    /**
     * 将实体转换为DTO
     */
    public static DataReportDTO fromEntity(DataReport entity) {
        if (entity == null) {
            return null;
        }

        return DataReportDTO.builder()
                .id(entity.getId())
                .reporterId(entity.getReporterId())
                .monitorDataId(entity.getMonitorDataId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .handlerId(entity.getHandlerId())
                .resolution(entity.getResolution())
                .reportTime(entity.getReportTime())
                .updatedAt(entity.getUpdatedAt())
                .closedAt(entity.getClosedAt())
                .build();
    }
} 