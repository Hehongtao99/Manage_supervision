package com.example.auth.service;

import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.Region;
import java.util.List;

public interface RegionService {
    
    /**
     * 获取所有地区树形结构
     */
    List<RegionDTO> getRegionTree();
    
    /**
     * 获取指定级别的地区列表
     */
    List<RegionDTO> getRegionsByLevel(Integer level);
    
    /**
     * 获取指定父级ID的子地区列表
     */
    List<RegionDTO> getRegionsByParentId(Long parentId);
    
    /**
     * 根据ID获取地区详情
     */
    RegionDTO getRegionById(Long id);
    
    /**
     * 添加地区
     */
    RegionDTO addRegion(RegionDTO regionDTO);
    
    /**
     * 更新地区
     */
    RegionDTO updateRegion(Long id, RegionDTO regionDTO);
    
    /**
     * 删除地区
     */
    boolean deleteRegion(Long id);
    
    /**
     * 切换地区状态（启用/停用）
     */
    boolean toggleRegionStatus(Long id);
    
    /**
     * 将实体转换为DTO
     */
    RegionDTO convertToDTO(Region region);
    
    /**
     * 获取地区路径（省市区完整路径）
     */
    String getRegionPath(Long regionId);
} 