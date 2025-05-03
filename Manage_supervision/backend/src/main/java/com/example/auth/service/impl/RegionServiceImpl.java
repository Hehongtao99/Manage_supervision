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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;
    
    @Override
    public List<RegionDTO> getRegionTree() {
        long startTime = System.currentTimeMillis();
        
        // 1. 一次性获取所有地区数据
        List<Region> allRegions = regionMapper.findAll();
        
        // 2. 将数据按照id进行索引
        Map<Long, Region> regionMap = allRegions.stream()
                .collect(Collectors.toMap(Region::getId, region -> region));
        
        // 3. 构建父子关系映射
        Map<Long, List<Region>> parentChildMap = new HashMap<>();
        for (Region region : allRegions) {
            if (region.getParentId() != null) {
                parentChildMap.computeIfAbsent(region.getParentId(), k -> new ArrayList<>())
                        .add(region);
            }
        }
        
        // 4. 提取省级数据并构建树结构
        List<RegionDTO> result = allRegions.stream()
                .filter(region -> region.getLevel() == 1) // 只取省级
                .map(province -> {
                    RegionDTO provinceDTO = convertToDTO(province, regionMap);
                    
                    // 获取省下面的市
                    List<Region> cities = parentChildMap.getOrDefault(province.getId(), new ArrayList<>());
                    List<RegionDTO> cityDTOs = new ArrayList<>();
                    
                    for (Region city : cities) {
                        RegionDTO cityDTO = convertToDTO(city, regionMap);
                        
                        // 获取市下面的区/县
                        List<Region> districts = parentChildMap.getOrDefault(city.getId(), new ArrayList<>());
                        List<RegionDTO> districtDTOs = districts.stream()
                                .map(district -> convertToDTO(district, regionMap))
                                .collect(Collectors.toList());
                        
                        cityDTO.setChildren(districtDTOs);
                        cityDTOs.add(cityDTO);
                    }
                    
                    provinceDTO.setChildren(cityDTOs);
                    return provinceDTO;
                })
                .collect(Collectors.toList());
        
        long endTime = System.currentTimeMillis();
        System.out.println("获取地区树耗时: " + (endTime - startTime) + "ms");
        
        return result;
    }
    
    @Override
    public List<RegionDTO> getRegionsByLevel(Integer level) {
        List<Region> regions = regionMapper.findByLevel(level);
        
        // 一次性获取所有父级地区（用于获取parentName）
        List<Long> parentIds = regions.stream()
                .map(Region::getParentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Region> parentMap = new HashMap<>();
        if (!parentIds.isEmpty()) {
            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Region::getId, parentIds);
            List<Region> parents = regionMapper.selectList(queryWrapper);
            parentMap = parents.stream().collect(Collectors.toMap(Region::getId, parent -> parent));
        }
        
        // 转换为DTO
        final Map<Long, Region> finalParentMap = parentMap; // 创建一个final变量引用
        return regions.stream()
                .map(region -> {
                    RegionDTO dto = convertToDTO(region);
                    if (region.getParentId() != null && finalParentMap.containsKey(region.getParentId())) {
                        dto.setParentName(finalParentMap.get(region.getParentId()).getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RegionDTO> getRegionsByParentId(Long parentId) {
        List<Region> regions = regionMapper.findByParentId(parentId);
        
        // 获取父级地区信息
        Region parentRegion = null;
        if (parentId != null) {
            parentRegion = regionMapper.selectById(parentId);
        }
        
        final String parentName = parentRegion != null ? parentRegion.getName() : null; // 创建一个final变量引用
        
        // 转换为DTO
        return regions.stream()
                .map(region -> {
                    RegionDTO dto = convertToDTO(region);
                    dto.setParentName(parentName);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public RegionDTO getRegionById(Long id) {
        Region region = regionMapper.selectById(id);
        if (region == null) {
            return null;
        }
        
        RegionDTO dto = convertToDTO(region);
        
        // 设置子地区
        List<Region> children = regionMapper.findByParentId(id);
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
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
    public List<RegionDTO> getRegionPath(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        
        Region region = regionMapper.selectById(id);
        if (region == null) {
            return Collections.emptyList();
        }
        
        List<RegionDTO> path = new ArrayList<>();
        path.add(convertToDTO(region));
        
        // 递归获取父级地区
        Long parentId = region.getParentId();
        while (parentId != null) {
            Region parent = regionMapper.selectById(parentId);
            if (parent == null) {
                break;
            }
            path.add(0, convertToDTO(parent)); // 添加到列表最前面
            parentId = parent.getParentId();
        }
        
        return path;
    }
    
    /**
     * 优化的DTO转换方法，避免每次都查询父级信息
     */
    private RegionDTO convertToDTO(Region region, Map<Long, Region> regionMap) {
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
        
        // 设置父级名称（从缓存中获取）
        if (region.getParentId() != null) {
            Region parentRegion = regionMap.get(region.getParentId());
            if (parentRegion != null) {
                dto.setParentName(parentRegion.getName());
            }
        }
        
        return dto;
    }
    
    /**
     * 原始的DTO转换方法，需要查询父级信息
     */
    private RegionDTO convertToDTO(Region region) {
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
} 