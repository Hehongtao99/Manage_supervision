package com.example.auth.repository;

import com.example.auth.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * 用户操作日志Repository接口
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    /**
     * 分页查询操作日志
     * @param username 用户名（可选）
     * @param operation 操作描述（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param module 模块（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT o FROM OperationLog o WHERE " +
           "(:username IS NULL OR o.username LIKE %:username%) AND " +
           "(:operation IS NULL OR o.operation LIKE %:operation%) AND " +
           "(:startTime IS NULL OR o.operationTime >= :startTime) AND " +
           "(:endTime IS NULL OR o.operationTime <= :endTime) AND " +
           "(:module IS NULL OR o.module = :module) " +
           "ORDER BY o.operationTime DESC")
    Page<OperationLog> findByConditions(
            @Param("username") String username,
            @Param("operation") String operation,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("module") String module,
            Pageable pageable);
} 