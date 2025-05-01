package com.example.auth.repository;

import com.example.auth.entity.Host;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 主机信息Repository
 */
@Repository
public interface HostRepository extends JpaRepository<Host, Long> {
    
    /**
     * 根据主机名或IP模糊查询
     */
    @Query("SELECT h FROM Host h WHERE h.hostname LIKE %:query% OR h.ip LIKE %:query%")
    Page<Host> findByHostnameOrIpContaining(@Param("query") String query, Pageable pageable);
    
    /**
     * 根据用户ID查询主机列表
     */
    List<Host> findByUserId(Long userId);
    
    /**
     * 根据状态查询主机列表
     */
    List<Host> findByStatus(String status);
    
    /**
     * 根据主机名查询
     */
    Optional<Host> findByHostname(String hostname);
    
    /**
     * 根据IP查询
     */
    Optional<Host> findByIp(String ip);
} 