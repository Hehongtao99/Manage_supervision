package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.CompanionServiceDTO;
import com.example.auth.model.entity.CompanionService;

import java.util.List;

public interface CompanionServiceService {
    /**
     * 创建陪玩服务
     * @param companionService 陪玩服务信息
     * @return 创建的陪玩服务ID
     */
    Long createService(CompanionService companionService);
    
    /**
     * 更新陪玩服务
     * @param companionService 陪玩服务信息
     * @return 是否更新成功
     */
    boolean updateService(CompanionService companionService);
    
    /**
     * 根据ID获取陪玩服务
     * @param serviceId 服务ID
     * @return 陪玩服务DTO
     */
    CompanionServiceDTO getServiceById(Long serviceId);
    
    /**
     * 获取陪玩的所有服务
     * @param companionId 陪玩ID
     * @return 服务列表
     */
    List<CompanionServiceDTO> getServicesByCompanionId(Long companionId);
    
    /**
     * 分页获取所有激活的陪玩服务
     * @param page 页码
     * @param size 每页大小
     * @return 分页服务列表
     */
    Page<CompanionServiceDTO> getActiveServices(int page, int size);
    
    /**
     * 更新服务状态
     * @param serviceId 服务ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateServiceStatus(Long serviceId, String status);
    
    /**
     * 删除服务
     * @param serviceId 服务ID
     * @return 是否删除成功
     */
    boolean deleteService(Long serviceId);
    
    /**
     * 分页获取所有陪玩服务（管理员使用）
     * @param page 页码
     * @param size 每页大小
     * @return 分页服务列表
     */
    Page<CompanionServiceDTO> getAllServices(int page, int size);
    
    /**
     * 分页获取指定状态的陪玩服务
     * @param page 页码
     * @param size 每页大小
     * @param status 服务状态
     * @return 分页服务列表
     */
    Page<CompanionServiceDTO> getServicesByStatus(int page, int size, String status);
    
    /**
     * 审核通过陪玩服务
     * @param serviceId 服务ID
     * @param reviewerId 审核员ID
     * @return 是否操作成功
     */
    boolean approveService(Long serviceId, Long reviewerId);
    
    /**
     * 审核拒绝陪玩服务
     * @param serviceId 服务ID
     * @param reason 拒绝原因
     * @param reviewerId 审核员ID
     * @return 是否操作成功
     */
    boolean rejectService(Long serviceId, String reason, Long reviewerId);
} 