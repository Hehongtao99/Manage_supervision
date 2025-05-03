package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.RegionMapper;
import com.example.auth.mapper.ScenicSpotMapper;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ScenicSpotDTO;
import com.example.auth.model.entity.Region;
import com.example.auth.model.entity.ScenicSpot;
import com.example.auth.service.RegionService;
import com.example.auth.service.ScenicSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScenicSpotServiceImpl implements ScenicSpotService {

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;
    
    @Autowired
    private RegionMapper regionMapper;
    
    @Autowired
    private RegionService regionService;
    
    @Override
    public PageResponse<ScenicSpotDTO> getScenicSpotList(int page, int size, String name, Long provinceId, 
                                                Long cityId, Long districtId, String level, String status) {
        Page<ScenicSpot> pageParam = new Page<>(page, size);
        
        Page<ScenicSpot> result = scenicSpotMapper.findByConditions(
                pageParam, name, provinceId, cityId, districtId, level, status);
        
        List<ScenicSpotDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(dtoList, result.getTotal(), page, size);
    }
    
    @Override
    public ScenicSpotDTO getScenicSpotById(Long id) {
        ScenicSpot scenicSpot = scenicSpotMapper.findDetailById(id);
        if (scenicSpot != null) {
            return convertToDTO(scenicSpot);
        }
        return null;
    }
    
    @Override
    @Transactional
    public ScenicSpotDTO addScenicSpot(ScenicSpotDTO scenicSpotDTO) {
        // 检查省份是否存在
        if (scenicSpotDTO.getProvinceId() != null) {
            Region province = regionMapper.selectById(scenicSpotDTO.getProvinceId());
            if (province == null) {
                throw new IllegalArgumentException("所选省份不存在");
            }
        } else {
            throw new IllegalArgumentException("省份ID不能为空");
        }
        
        // 检查城市是否存在（如果有）
        if (scenicSpotDTO.getCityId() != null) {
            Region city = regionMapper.selectById(scenicSpotDTO.getCityId());
            if (city == null) {
                throw new IllegalArgumentException("所选城市不存在");
            }
        }
        
        // 检查区县是否存在（如果有）
        if (scenicSpotDTO.getDistrictId() != null) {
            Region district = regionMapper.selectById(scenicSpotDTO.getDistrictId());
            if (district == null) {
                throw new IllegalArgumentException("所选区县不存在");
            }
        }
        
        // 构建实体
        ScenicSpot scenicSpot = new ScenicSpot();
        scenicSpot.setName(scenicSpotDTO.getName());
        scenicSpot.setDescription(scenicSpotDTO.getDescription());
        scenicSpot.setProvinceId(scenicSpotDTO.getProvinceId());
        scenicSpot.setCityId(scenicSpotDTO.getCityId());
        scenicSpot.setDistrictId(scenicSpotDTO.getDistrictId());
        scenicSpot.setAddress(scenicSpotDTO.getAddress());
        scenicSpot.setLevel(scenicSpotDTO.getLevel());
        scenicSpot.setBusinessHours(scenicSpotDTO.getBusinessHours());
        scenicSpot.setTicketPrice(scenicSpotDTO.getTicketPrice());
        scenicSpot.setContactPhone(scenicSpotDTO.getContactPhone());
        scenicSpot.setImageUrl(scenicSpotDTO.getImageUrl());
        scenicSpot.setStatus("active"); // 默认为启用状态
        scenicSpot.setSort(scenicSpotDTO.getSort() != null ? scenicSpotDTO.getSort() : 0);
        scenicSpot.setCreateTime(LocalDateTime.now());
        scenicSpot.setUpdateTime(LocalDateTime.now());
        
        // 保存景区
        scenicSpotMapper.insert(scenicSpot);
        
        // 查询详情（包含地区名称）并返回
        ScenicSpot savedSpot = scenicSpotMapper.findDetailById(scenicSpot.getId());
        return convertToDTO(savedSpot);
    }
    
    @Override
    @Transactional
    public ScenicSpotDTO updateScenicSpot(Long id, ScenicSpotDTO scenicSpotDTO) {
        // 检查景区是否存在
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(id);
        if (scenicSpot == null) {
            throw new IllegalArgumentException("景区不存在");
        }
        
        // 检查省份是否存在
        if (scenicSpotDTO.getProvinceId() != null) {
            Region province = regionMapper.selectById(scenicSpotDTO.getProvinceId());
            if (province == null) {
                throw new IllegalArgumentException("所选省份不存在");
            }
        }
        
        // 检查城市是否存在（如果有）
        if (scenicSpotDTO.getCityId() != null) {
            Region city = regionMapper.selectById(scenicSpotDTO.getCityId());
            if (city == null) {
                throw new IllegalArgumentException("所选城市不存在");
            }
        }
        
        // 检查区县是否存在（如果有）
        if (scenicSpotDTO.getDistrictId() != null) {
            Region district = regionMapper.selectById(scenicSpotDTO.getDistrictId());
            if (district == null) {
                throw new IllegalArgumentException("所选区县不存在");
            }
        }
        
        // 更新字段
        scenicSpot.setName(scenicSpotDTO.getName());
        scenicSpot.setDescription(scenicSpotDTO.getDescription());
        scenicSpot.setProvinceId(scenicSpotDTO.getProvinceId());
        scenicSpot.setCityId(scenicSpotDTO.getCityId());
        scenicSpot.setDistrictId(scenicSpotDTO.getDistrictId());
        scenicSpot.setAddress(scenicSpotDTO.getAddress());
        scenicSpot.setLevel(scenicSpotDTO.getLevel());
        scenicSpot.setBusinessHours(scenicSpotDTO.getBusinessHours());
        scenicSpot.setTicketPrice(scenicSpotDTO.getTicketPrice());
        scenicSpot.setContactPhone(scenicSpotDTO.getContactPhone());
        scenicSpot.setImageUrl(scenicSpotDTO.getImageUrl());
        scenicSpot.setSort(scenicSpotDTO.getSort());
        scenicSpot.setStatus(scenicSpotDTO.getStatus());
        scenicSpot.setUpdateTime(LocalDateTime.now());
        
        // 更新景区
        scenicSpotMapper.updateById(scenicSpot);
        
        // 查询详情（包含地区名称）并返回
        ScenicSpot updatedSpot = scenicSpotMapper.findDetailById(id);
        return convertToDTO(updatedSpot);
    }
    
    @Override
    @Transactional
    public boolean deleteScenicSpot(Long id) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(id);
        if (scenicSpot == null) {
            throw new IllegalArgumentException("景区不存在");
        }
        
        int result = scenicSpotMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean toggleScenicSpotStatus(Long id) {
        ScenicSpot scenicSpot = scenicSpotMapper.selectById(id);
        if (scenicSpot == null) {
            return false;
        }
        
        // 切换状态
        String newStatus = "active".equals(scenicSpot.getStatus()) ? "inactive" : "active";
        scenicSpot.setStatus(newStatus);
        scenicSpot.setUpdateTime(LocalDateTime.now());
        
        return scenicSpotMapper.updateById(scenicSpot) > 0;
    }
    
    @Override
    public List<ScenicSpotDTO> getHotScenicSpots(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认10条
        }
        
        List<ScenicSpot> hotSpots = scenicSpotMapper.findHotSpots(limit);
        return hotSpots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ScenicSpotDTO convertToDTO(ScenicSpot scenicSpot) {
        if (scenicSpot == null) {
            return null;
        }
        
        ScenicSpotDTO dto = new ScenicSpotDTO();
        dto.setId(scenicSpot.getId());
        dto.setName(scenicSpot.getName());
        dto.setDescription(scenicSpot.getDescription());
        dto.setProvinceId(scenicSpot.getProvinceId());
        dto.setCityId(scenicSpot.getCityId());
        dto.setDistrictId(scenicSpot.getDistrictId());
        dto.setAddress(scenicSpot.getAddress());
        dto.setLevel(scenicSpot.getLevel());
        dto.setBusinessHours(scenicSpot.getBusinessHours());
        dto.setTicketPrice(scenicSpot.getTicketPrice());
        dto.setContactPhone(scenicSpot.getContactPhone());
        dto.setImageUrl(scenicSpot.getImageUrl());
        dto.setStatus(scenicSpot.getStatus());
        dto.setSort(scenicSpot.getSort());
        dto.setCreateTime(scenicSpot.getCreateTime());
        dto.setUpdateTime(scenicSpot.getUpdateTime());
        
        // 设置地区名称
        dto.setProvinceName(scenicSpot.getProvinceName());
        dto.setCityName(scenicSpot.getCityName());
        dto.setDistrictName(scenicSpot.getDistrictName());
        
        // 构建地区完整路径
        StringBuilder locationPath = new StringBuilder();
        if (scenicSpot.getProvinceName() != null) {
            locationPath.append(scenicSpot.getProvinceName());
            
            if (scenicSpot.getCityName() != null) {
                locationPath.append("/").append(scenicSpot.getCityName());
                
                if (scenicSpot.getDistrictName() != null) {
                    locationPath.append("/").append(scenicSpot.getDistrictName());
                }
            }
        }
        dto.setLocationPath(locationPath.toString());
        
        return dto;
    }
} 