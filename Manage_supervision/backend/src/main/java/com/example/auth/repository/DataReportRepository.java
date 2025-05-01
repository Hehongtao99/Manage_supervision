package com.example.auth.repository;

import com.example.auth.entity.DataReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DataReportRepository extends JpaRepository<DataReport, Long> {

    /**
     * 查询用户提交的报告
     */
    Page<DataReport> findByReporterIdOrderByReportTimeDesc(Long reporterId, Pageable pageable);
    
    /**
     * 查询处理人负责的报告
     */
    Page<DataReport> findByHandlerIdOrderByStatusAscReportTimeDesc(Long handlerId, Pageable pageable);
    
    /**
     * 根据状态查询报告
     */
    Page<DataReport> findByStatusOrderByReportTimeDesc(Integer status, Pageable pageable);
    
    /**
     * 查询特定监控数据的所有报告
     */
    List<DataReport> findByMonitorDataIdOrderByReportTimeDesc(Long monitorDataId);
    
    /**
     * 查询最近的报告
     */
    List<DataReport> findTop10ByOrderByReportTimeDesc();
    
    /**
     * 按时间范围和状态查询报告
     */
    @Query("SELECT d FROM DataReport d WHERE d.reportTime BETWEEN :startTime AND :endTime AND (:status IS NULL OR d.status = :status)")
    Page<DataReport> findByTimeRangeAndStatus(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") Integer status,
            Pageable pageable);
    
    /**
     * 统计每种状态的数量
     */
    @Query("SELECT d.status, COUNT(d) FROM DataReport d GROUP BY d.status")
    List<Object[]> countByStatusGroups();
    
    /**
     * 统计未处理的报告数量
     */
    long countByStatusIn(List<Integer> statusList);
} 