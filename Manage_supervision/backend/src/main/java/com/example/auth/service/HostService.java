package com.example.auth.service;

import com.example.auth.dto.HostDTO;
import com.example.auth.dto.PageRequestDTO;
import com.example.auth.dto.PageResponseDTO;
import com.example.auth.entity.Host;

import java.util.List;
import java.util.Map;

/**
 * 主机管理服务接口
 */
public interface HostService {
    
    /**
     * 获取主机列表（分页）
     */
    PageResponseDTO<HostDTO> getHosts(PageRequestDTO pageRequest);
    
    /**
     * 获取主机详情
     */
    HostDTO getHostById(Long id);
    
    /**
     * 添加主机
     */
    HostDTO addHost(HostDTO hostDTO);
    
    /**
     * 更新主机信息
     */
    HostDTO updateHost(Long id, HostDTO hostDTO);
    
    /**
     * 删除主机
     */
    void deleteHost(Long id);
    
    /**
     * 根据用户ID获取主机列表
     */
    List<HostDTO> getHostsByUserId(Long userId);
    
    /**
     * 根据状态获取主机列表
     */
    List<HostDTO> getHostsByStatus(String status);
    
    /**
     * 检查主机名是否已存在
     */
    boolean isHostnameExists(String hostname);
    
    /**
     * 检查IP是否已存在
     */
    boolean isIpExists(String ip);
    
    /**
     * 获取当前主机的真实信息
     */
    HostDTO getCurrentHostInfo();
    
    /**
     * 获取主机状态统计数据
     * @return 包含各状态主机数量的映射表，如{"online": 5, "offline": 2, "warning": 1, "error": 0}
     */
    Map<String, Integer> getHostStatusStats();
} 