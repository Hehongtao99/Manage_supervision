package com.example.auth.service.impl;

import com.example.auth.dto.DataReportDTO;
import com.example.auth.dto.DataReportQueryDTO;
import com.example.auth.dto.MonitorDataDTO;
import com.example.auth.entity.DataReport;
import com.example.auth.entity.MonitorData;
import com.example.auth.entity.User;
import com.example.auth.repository.DataReportRepository;
import com.example.auth.repository.MonitorDataRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.DataReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataReportServiceImpl implements DataReportService {

    private final DataReportRepository dataReportRepository;
    private final MonitorDataRepository monitorDataRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DataReport createDataReport(DataReport dataReport) {
        // 设置初始状态
        dataReport.setStatus(0); // 待处理
        dataReport.setReportTime(LocalDateTime.now());
        return dataReportRepository.save(dataReport);
    }

    @Override
    public Page<DataReportDTO> queryDataReports(DataReportQueryDTO queryDTO) {
        int page = queryDTO.getPage() != null ? queryDTO.getPage() : 0;
        int size = queryDTO.getSize() != null ? queryDTO.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size);

        // 根据不同条件查询
        Page<DataReport> reportPage;
        if (queryDTO.getReporterId() != null) {
            reportPage = dataReportRepository.findByReporterIdOrderByReportTimeDesc(queryDTO.getReporterId(), pageable);
        } else if (queryDTO.getHandlerId() != null) {
            reportPage = dataReportRepository.findByHandlerIdOrderByStatusAscReportTimeDesc(queryDTO.getHandlerId(), pageable);
        } else if (queryDTO.getStatus() != null) {
            reportPage = dataReportRepository.findByStatusOrderByReportTimeDesc(queryDTO.getStatus(), pageable);
        } else if (queryDTO.getStartDate() != null && queryDTO.getEndDate() != null) {
            reportPage = dataReportRepository.findByTimeRangeAndStatus(
                    queryDTO.getStartDate(),
                    queryDTO.getEndDate(),
                    queryDTO.getStatus(),
                    pageable);
        } else {
            reportPage = dataReportRepository.findAll(pageable);
        }

        List<DataReportDTO> dtoList = convertToDTOList(reportPage.getContent());
        return new PageImpl<>(dtoList, pageable, reportPage.getTotalElements());
    }

    @Override
    public DataReportDTO getDataReportDetail(Long id) {
        DataReport report = dataReportRepository.findById(id).orElse(null);
        if (report == null) {
            return null;
        }

        DataReportDTO dto = DataReportDTO.fromEntity(report);
        
        // 加载关联的监控数据
        if (report.getMonitorDataId() != null) {
            monitorDataRepository.findById(report.getMonitorDataId())
                    .ifPresent(monitorData -> dto.setMonitorData(convertToMonitorDTO(monitorData)));
        }
        
        // 加载用户信息
        userRepository.findById(report.getReporterId())
                .ifPresent(user -> dto.setReporterName(user.getUsername()));
        
        if (report.getHandlerId() != null) {
            userRepository.findById(report.getHandlerId())
                    .ifPresent(user -> dto.setHandlerName(user.getUsername()));
        }
        
        return dto;
    }

    @Override
    @Transactional
    public DataReportDTO handleDataReport(Long id, Long handlerId, Integer status, String resolution) {
        DataReport report = dataReportRepository.findById(id).orElse(null);
        if (report == null) {
            return null;
        }
        
        report.setHandlerId(handlerId);
        report.setStatus(status);
        report.setResolution(resolution);
        report.setUpdatedAt(LocalDateTime.now());
        
        // 如果状态是已解决或已关闭，设置关闭时间，并尝试更新关联的 MonitorData
        if (status == 2 || status == 3) {
            report.setClosedAt(LocalDateTime.now());
            
            // 如果有关联的监控数据ID，则将其标记为已解决
            if (report.getMonitorDataId() != null) {
                monitorDataRepository.findById(report.getMonitorDataId()).ifPresent(monitorData -> {
                    monitorData.setResolved(true);
                    monitorData.setUpdatedAt(LocalDateTime.now()); // 更新时间
                    monitorDataRepository.save(monitorData);
                });
            }
        }
        
        DataReport updatedReport = dataReportRepository.save(report);
        return getDataReportDetail(updatedReport.getId());
    }

    @Override
    public Page<DataReportDTO> getUserSubmittedReports(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DataReport> reportPage = dataReportRepository.findByReporterIdOrderByReportTimeDesc(userId, pageable);
        List<DataReportDTO> dtoList = convertToDTOList(reportPage.getContent());
        return new PageImpl<>(dtoList, pageable, reportPage.getTotalElements());
    }

    @Override
    public long countPendingReports() {
        // 统计待处理和处理中的报告
        return dataReportRepository.countByStatusIn(Arrays.asList(0, 1));
    }

    @Override
    public Map<Integer, Long> getReportStatusStats() {
        List<Object[]> statsData = dataReportRepository.countByStatusGroups();
        Map<Integer, Long> statsMap = new HashMap<>();
        
        for (Object[] row : statsData) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            statsMap.put(status, count);
        }
        
        return statsMap;
    }

    @Override
    public List<DataReportDTO> getRecentReports() {
        List<DataReport> reports = dataReportRepository.findTop10ByOrderByReportTimeDesc();
        return convertToDTOList(reports);
    }
    
    /**
     * 将实体列表转换为DTO列表
     */
    private List<DataReportDTO> convertToDTOList(List<DataReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 收集所有用户ID和监控数据ID
        Set<Long> userIds = new HashSet<>();
        Set<Long> monitorDataIds = new HashSet<>();
        
        for (DataReport report : reports) {
            userIds.add(report.getReporterId());
            if (report.getHandlerId() != null) {
                userIds.add(report.getHandlerId());
            }
            if (report.getMonitorDataId() != null) {
                monitorDataIds.add(report.getMonitorDataId());
            }
        }
        
        // 批量查询用户和监控数据
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        Map<Long, MonitorData> monitorDataMap = monitorDataRepository.findAllById(monitorDataIds).stream()
                .collect(Collectors.toMap(MonitorData::getId, data -> data));
        
        // 转换为DTO并填充关联数据
        return reports.stream().map(report -> {
            DataReportDTO dto = DataReportDTO.fromEntity(report);
            
            // 设置用户信息
            User reporter = userMap.get(report.getReporterId());
            if (reporter != null) {
                dto.setReporterName(reporter.getUsername());
            }
            
            if (report.getHandlerId() != null) {
                User handler = userMap.get(report.getHandlerId());
                if (handler != null) {
                    dto.setHandlerName(handler.getUsername());
                }
            }
            
            // 设置监控数据
            if (report.getMonitorDataId() != null) {
                MonitorData monitorData = monitorDataMap.get(report.getMonitorDataId());
                if (monitorData != null) {
                    dto.setMonitorData(convertToMonitorDTO(monitorData));
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 将监控数据实体转换为DTO
     */
    private MonitorDataDTO convertToMonitorDTO(MonitorData monitorData) {
        return MonitorDataDTO.builder()
                .id(monitorData.getId())
                .dataType(monitorData.getDataType())
                .value(monitorData.getValue())
                .expectedMin(monitorData.getExpectedMin())
                .expectedMax(monitorData.getExpectedMax())
                .deviation(monitorData.getDeviation())
                .isAnomaly(monitorData.getIsAnomaly())
                .resolved(monitorData.getResolved())
                .recordTime(monitorData.getRecordTime())
                .description(monitorData.getDescription())
                .source(monitorData.getSource())
                .build();
    }
} 