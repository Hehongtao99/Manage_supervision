package com.example.auth.repository;

import com.example.auth.entity.HostMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 主机监控数据Repository
 */
@Repository
public interface HostMetricRepository extends JpaRepository<HostMetric, Long> {
    
    /**
     * 根据主机ID查询最近的监控数据
     */
    @Query("SELECT m FROM HostMetric m WHERE m.hostId = :hostId AND m.collectionTime >= :startTime ORDER BY m.collectionTime DESC")
    List<HostMetric> findRecentMetricsByHostId(@Param("hostId") Long hostId, @Param("startTime") LocalDateTime startTime);
    
    /**
     * 根据主机ID查询最新的一条监控数据
     */
    @Query("SELECT m FROM HostMetric m WHERE m.hostId = :hostId ORDER BY m.collectionTime DESC")
    List<HostMetric> findLatestByHostId(@Param("hostId") Long hostId, Pageable pageable);
    
    /**
     * 根据主机ID删除监控数据
     */
    void deleteByHostId(Long hostId);
} 