package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.RegionMapper;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.Region;
import com.example.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;
    
    @Override
    public List<RegionDTO> getRegionTree() {
        // 获取所有省份
        List<Region> provinces = regionMapper.findAllProvinces();
        List<RegionDTO> result = new ArrayList<>();
        
        for (Region province : provinces) {
            RegionDTO provinceDTO = convertToDTO(province);
            
            // 获取省下面的市
            List<Region> cities = regionMapper.findByParentId(province.getId());
            List<RegionDTO> cityDTOs = new ArrayList<>();
            
            for (Region city : cities) {
                RegionDTO cityDTO = convertToDTO(city);
                
                // 获取市下面的区/县
                List<Region> districts = regionMapper.findByParentId(city.getId());
                List<RegionDTO> districtDTOs = districts.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
                
                cityDTO.setChildren(districtDTOs);
                cityDTOs.add(cityDTO);
            }
            
            provinceDTO.setChildren(cityDTOs);
            result.add(provinceDTO);
        }
        
        return result;
    }
    
    @Override
    public List<RegionDTO> getRegionsByLevel(Integer level) {
        List<Region> regions = regionMapper.findByLevel(level);
        return regions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
    public RegionDTO addRegion(RegionDTO regionDTO) {
        // 1. 检查父级ID是否存在
        if (regionDTO.getParentId() != null) {
            Region parentRegion = regionMapper.selectById(regionDTO.getParentId());
            if (parentRegion == null) {
                throw new IllegalArgumentException("父级地区不存在");
            }
            
            // 设置级别 = 父级级别 + 1
            regionDTO.setLevel(parentRegion.getLevel() + 1);
        } else {
            // 如果没有父级，则设为省级 (level=1)
            regionDTO.setLevel(1);
        }
        
        // 2. 检查编码是否重复
        Region existCode = regionMapper.findByCode(regionDTO.getCode());
        if (existCode != null) {
            throw new IllegalArgumentException("地区编码已存在");
        }
        
        // 3. 构建实体并保存
        Region region = new Region();
        region.setName(regionDTO.getName());
        region.setCode(regionDTO.getCode());
        region.setParentId(regionDTO.getParentId());
        region.setLevel(regionDTO.getLevel());
        region.setSort(regionDTO.getSort() != null ? regionDTO.getSort() : 0);
        region.setStatus("active");
        region.setCreateTime(LocalDateTime.now());
        region.setUpdateTime(LocalDateTime.now());
        
        regionMapper.insert(region);
        
        // 返回创建后的数据
        return convertToDTO(region);
    }
    
    @Override
    @Transactional
    public RegionDTO updateRegion(Long id, RegionDTO regionDTO) {
        Region region = regionMapper.selectById(id);
        if (region == null) {
            throw new IllegalArgumentException("地区不存在");
        }
        
        // 检查编码是否重复（不是自己）
        if (!region.getCode().equals(regionDTO.getCode())) {
            Region existCode = regionMapper.findByCode(regionDTO.getCode());
            if (existCode != null) {
                throw new IllegalArgumentException("地区编码已存在");
            }
        }
        
        // 更新字段
        region.setName(regionDTO.getName());
        region.setCode(regionDTO.getCode());
        region.setSort(regionDTO.getSort() != null ? regionDTO.getSort() : region.getSort());
        region.setUpdateTime(LocalDateTime.now());
        
        regionMapper.updateById(region);
        
        return convertToDTO(region);
    }
    
    @Override
    @Transactional
    public boolean deleteRegion(Long id) {
        // 检查是否有子地区，如果有不允许删除
        Integer childCount = regionMapper.countByParentId(id);
        if (childCount > 0) {
            throw new IllegalArgumentException("该地区下有子地区，无法删除");
        }
        
        int result = regionMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean toggleRegionStatus(Long id) {
        Region region = regionMapper.selectById(id);
        if (region == null) {
            return false;
        }
        
        // 切换状态
        String newStatus = "active".equals(region.getStatus()) ? "inactive" : "active";
        region.setStatus(newStatus);
        region.setUpdateTime(LocalDateTime.now());
        
        return regionMapper.updateById(region) > 0;
    }
    
    @Override
    public RegionDTO convertToDTO(Region region) {
        if (region == null) {
            return null;
        }
        
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setCode(region.getCode());
        dto.setParentId(region.getParentId());
        dto.setLevel(region.getLevel());
        dto.setSort(region.getSort());
        dto.setStatus(region.getStatus());
        dto.setCreateTime(region.getCreateTime());
        dto.setUpdateTime(region.getUpdateTime());
        
        // 设置父级名称
        if (region.getParentId() != null) {
            Region parentRegion = regionMapper.selectById(region.getParentId());
            if (parentRegion != null) {
                dto.setParentName(parentRegion.getName());
            }
        }
        
        return dto;
    }
    
    @Override
    public String getRegionPath(Long regionId) {
        StringBuilder path = new StringBuilder();
        
        Region region = regionMapper.selectById(regionId);
        if (region == null) {
            return "";
        }
        
        // 构建完整路径
        List<String> names = new ArrayList<>();
        names.add(region.getName());
        
        // 循环查找父级
        Long parentId = region.getParentId();
        while (parentId != null) {
            Region parentRegion = regionMapper.selectById(parentId);
            if (parentRegion == null) {
                break;
            }
            
            names.add(parentRegion.getName());
            parentId = parentRegion.getParentId();
        }
        
        // 逆序连接
        Collections.reverse(names);
        return String.join("/", names);
    }
} 