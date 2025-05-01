package com.example.auth.service;

import com.example.auth.dto.DataReportDTO;
import com.example.auth.dto.DataReportQueryDTO;
import com.example.auth.entity.DataReport;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 数据上报服务接口
 */
public interface DataReportService {

    /**
     * 创建数据上报
     * @param dataReport 数据上报实体
     * @return 创建的数据上报
     */
    DataReport createDataReport(DataReport dataReport);

    /**
     * 查询数据上报
     * @param queryDTO 查询参数
     * @return 分页数据上报列表
     */
    Page<DataReportDTO> queryDataReports(DataReportQueryDTO queryDTO);

    /**
     * 获取数据上报详情
     * @param id 上报ID
     * @return 数据上报详情
     */
    DataReportDTO getDataReportDetail(Long id);

    /**
     * 处理数据上报
     * @param id 上报ID
     * @param handlerId 处理人ID
     * @param status 状态
     * @param resolution 处理结果
     * @return 更新后的数据上报
     */
    DataReportDTO handleDataReport(Long id, Long handlerId, Integer status, String resolution);

    /**
     * 获取用户提交的上报
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页数据上报列表
     */
    Page<DataReportDTO> getUserSubmittedReports(Long userId, int page, int size);

    /**
     * 获取待处理的上报数量
     * @return 待处理上报数量
     */
    long countPendingReports();

    /**
     * 获取上报状态统计
     * @return 状态统计映射
     */
    Map<Integer, Long> getReportStatusStats();

    /**
     * 获取最近上报列表
     * @return 最近上报列表
     */
    List<DataReportDTO> getRecentReports();
} 