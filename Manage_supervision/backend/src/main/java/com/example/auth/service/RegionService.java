package com.example.auth.service;

import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.Region;

import java.util.List;

public interface RegionService {
    
    /**
     * 获取省市区树形结构
     * @return 省市区树形结构
     */
    List<RegionDTO> getRegionTree();
    
    /**
     * 根据父ID获取子区域
     * @param parentId 父ID
     * @return 子区域列表
     */
    List<RegionDTO> getRegionsByParentId(Long parentId);
    
    /**
     * 根据ID获取区域
     * @param id 区域ID
     * @return 区域信息
     */
    RegionDTO getRegionById(Long id);
    
    /**
     * 添加区域
     * @param region 区域信息
     * @return 添加后的区域信息
     */
    RegionDTO addRegion(Region region);
    
    /**
     * 更新区域
     * @param id 区域ID
     * @param region 区域信息
     * @return 更新后的区域信息
     */
    RegionDTO updateRegion(Long id, Region region);
    
    /**
     * 删除区域
     * @param id 区域ID
     * @return 是否删除成功
     */
    boolean deleteRegion(Long id);
    
    /**
     * 根据区域编码获取完整的地址路径
     * @param provinceId 省份ID
     * @param cityId 城市ID
     * @param districtId 区县ID
     * @param streetId 街道ID
     * @return 完整地址字符串，例如"广东省深圳市南山区科技园街道"
     */
    String getFullAddressPath(Long provinceId, Long cityId, Long districtId, Long streetId);
} 