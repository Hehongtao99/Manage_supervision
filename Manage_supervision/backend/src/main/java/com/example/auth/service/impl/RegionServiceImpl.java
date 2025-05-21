package com.example.auth.service.impl;

import com.example.auth.mapper.RegionMapper;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.Region;
import com.example.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<RegionDTO> getRegionTree() {
        // 获取所有省级区域
        List<Region> provinces = regionMapper.findProvinces();
        
        // 转换为DTO并构建树形结构
        return provinces.stream()
                .map(this::buildRegionTree)
                .collect(Collectors.toList());
    }
    
    /**
     * 递归构建区域树
     */
    private RegionDTO buildRegionTree(Region region) {
        RegionDTO dto = convertToDTO(region);
        
        // 查询子区域
        List<Region> children = regionMapper.findByParentId(region.getId());
        if (children != null && !children.isEmpty()) {
            List<RegionDTO> childrenDTOs = children.stream()
                    .map(this::buildRegionTree)
                    .collect(Collectors.toList());
            dto.setChildren(childrenDTOs);
        }
        
        return dto;
    }
    
    @Override
    public List<RegionDTO> getRegionsByParentId(Long parentId) {
        List<Region> regions = regionMapper.findByParentId(parentId);
        return regions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RegionDTO getRegionById(Long id) {
        Region region = regionMapper.selectById(id);
        if (region != null) {
            return convertToDTO(region);
        }
        return null;
    }
    
    @Override
    @Transactional
    public RegionDTO addRegion(Region region) {
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        region.setCreateTime(now);
        region.setUpdateTime(now);
        
        // 设置默认排序号
        if (region.getSortOrder() == null) {
            region.setSortOrder(0);
        }
        
        // 保存区域
        regionMapper.insert(region);
        
        return convertToDTO(region);
    }
    
    @Override
    @Transactional
    public RegionDTO updateRegion(Long id, Region region) {
        Region existingRegion = regionMapper.selectById(id);
        if (existingRegion != null) {
            // 更新区域信息
            existingRegion.setName(region.getName());
            existingRegion.setCode(region.getCode());
            existingRegion.setLevel(region.getLevel());
            existingRegion.setSortOrder(region.getSortOrder());
            existingRegion.setUpdateTime(LocalDateTime.now());
            
            // 更新区域
            regionMapper.updateById(existingRegion);
            
            return convertToDTO(existingRegion);
        }
        return null;
    }
    
    @Override
    @Transactional
    public boolean deleteRegion(Long id) {
        // 检查是否有子区域
        int childrenCount = regionMapper.countChildren(id);
        if (childrenCount > 0) {
            // 有子区域，不能删除
            return false;
        }
        
        // 删除区域
        regionMapper.deleteById(id);
        return true;
    }
    
    @Override
    public String getFullAddressPath(Long provinceId, Long cityId, Long districtId, Long streetId) {
        StringBuilder fullAddress = new StringBuilder();
        
        // 添加省级名称
        if (provinceId != null) {
            Region province = regionMapper.selectById(provinceId);
            if (province != null) {
                fullAddress.append(province.getName());
            }
        }
        
        // 添加市级名称
        if (cityId != null) {
            Region city = regionMapper.selectById(cityId);
            if (city != null) {
                fullAddress.append(city.getName());
            }
        }
        
        // 添加区/县级名称
        if (districtId != null) {
            Region district = regionMapper.selectById(districtId);
            if (district != null) {
                fullAddress.append(district.getName());
            }
        }
        
        // 添加街道/乡镇名称
        if (streetId != null) {
            Region street = regionMapper.selectById(streetId);
            if (street != null) {
                fullAddress.append(street.getName());
            }
        }
        
        return fullAddress.toString();
    }
    
    /**
     * 将Region实体转换为DTO
     */
    private RegionDTO convertToDTO(Region region) {
        if (region == null) {
            return null;
        }
        
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setParentId(region.getParentId());
        dto.setName(region.getName());
        dto.setCode(region.getCode());
        dto.setLevel(region.getLevel());
        dto.setSortOrder(region.getSortOrder());
        
        // 格式化时间
        if (region.getCreateTime() != null) {
            dto.setCreateTime(region.getCreateTime().format(formatter));
        }
        
        if (region.getUpdateTime() != null) {
            dto.setUpdateTime(region.getUpdateTime().format(formatter));
        }
        
        // 设置前端选择器需要的属性
        dto.setValue(region.getId().toString());
        dto.setLabel(region.getName());
        
        return dto;
    }
} 