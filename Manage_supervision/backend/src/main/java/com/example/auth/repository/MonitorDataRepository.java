package com.example.auth.repository;

import com.example.auth.entity.MonitorData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MonitorDataRepository extends JpaRepository<MonitorData, Long> {

    /**
     * 根据用户ID查询监控数据
     */
    Page<MonitorData> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户ID和时间范围查询监控数据
     */
    Page<MonitorData> findByUserIdAndRecordTimeBetween(
            Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据用户ID查询异常数据
     */
    Page<MonitorData> findByUserIdAndIsAnomalyTrue(Long userId, Pageable pageable);

    /**
     * 根据用户ID和时间范围查询异常数据
     */
    Page<MonitorData> findByUserIdAndIsAnomalyTrueAndRecordTimeBetween(
            Long userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询用户的监控数据总数
     */
    Long countByUserId(Long userId);

    /**
     * 查询用户的异常数据总数
     */
    Long countByUserIdAndIsAnomalyTrue(Long userId);

    /**
     * 查询用户已处理的异常数据总数
     */
    Long countByUserIdAndIsAnomalyTrueAndResolvedTrue(Long userId);

    /**
     * 根据用户ID查询所有异常数据（带排序）
     */
    List<MonitorData> findByUserIdAndIsAnomalyTrue(Long userId, Sort sort);

    /**
     * 获取最近的监控数据记录时间
     */
    @Query("SELECT MAX(m.recordTime) FROM MonitorData m WHERE m.userId = :userId")
    LocalDateTime findLatestRecordTimeByUserId(@Param("userId") Long userId);
    
    /**
     * 根据数据类型分组统计异常数
     */
    @Query("SELECT m.dataType, COUNT(m) FROM MonitorData m WHERE m.userId = :userId AND m.isAnomaly = true GROUP BY m.dataType")
    List<Object[]> countAnomaliesByDataType(@Param("userId") Long userId);
    
    /**
     * 根据时间范围和阈值查询异常数据
     */
    @Query("SELECT m FROM MonitorData m WHERE m.userId = :userId AND m.deviation >= :threshold AND " +
           "(:startTime IS NULL OR m.recordTime >= :startTime) AND " +
           "(:endTime IS NULL OR m.recordTime <= :endTime)")
    Page<MonitorData> findAnomaliesByThreshold(
            @Param("userId") Long userId,
            @Param("threshold") Double threshold,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
            
    /**
     * 根据数据类型和时间范围查询监控数据
     */
    List<MonitorData> findByDataTypeAndRecordTimeBetween(
            String dataType, LocalDateTime startTime, LocalDateTime endTime);
            
    /**
     * 查询所有异常数据总数
     */
    Long countByIsAnomalyTrue();
    
    /**
     * 查询未处理的异常数据总数
     */
    Long countByIsAnomalyTrueAndResolvedFalse();
    
    /**
     * 查询指定时间点之后的未处理异常数据总数
     */
    Long countByIsAnomalyTrueAndResolvedFalseAndRecordTimeAfter(LocalDateTime startTime);
    
    /**
     * 根据时间范围查询未处理的异常数据
     */
    Page<MonitorData> findByIsAnomalyTrueAndResolvedFalseAndRecordTimeBetween(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // 添加分页版本的查询方法
    Page<MonitorData> findByDataTypeAndRecordTimeBetween(String dataType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
} 