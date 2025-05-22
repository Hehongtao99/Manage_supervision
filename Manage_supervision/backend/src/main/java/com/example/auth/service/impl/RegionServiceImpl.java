package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.RegionMapper;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.Region;
import com.example.auth.service.MinioService;
import com.example.auth.service.RegionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 省市区街道服务实现类
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Value("${file.access.path:http://localhost:8081/uploads/}")
    private String accessPath;
    
    @Autowired
    private MinioService minioService;

    /**
     * 递归获取树形结构的区域数据
     */
    @Override
    public List<RegionDTO> getRegionTree(Long parentId) {
        // 获取第一级数据
        List<Region> regions = baseMapper.findByParentId(parentId);
        List<RegionDTO> result = regions.stream().map(this::convertToDTO).collect(Collectors.toList());
        
        // 递归加载子节点
        for (RegionDTO dto : result) {
            if (dto.getHasChildren()) {
                dto.setChildren(getRegionTree(dto.getId()));
            } else {
                dto.setChildren(new ArrayList<>());
            }
        }
        
        return result;
    }

    /**
     * 获取子区域列表
     */
    @Override
    public List<RegionDTO> getRegionList(Long parentId) {
        List<Region> regions = baseMapper.findByParentId(parentId);
        return regions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 保存区域信息
     */
    @Override
    @Transactional
    public RegionDTO saveRegion(RegionDTO regionDTO) {
        // 检查编码是否已存在
        int count = baseMapper.countByCode(regionDTO.getCode(), regionDTO.getId());
        if (count > 0) {
            throw new RuntimeException("区域编码已存在");
        }
        
        Region region = new Region();
        BeanUtils.copyProperties(regionDTO, region);
        
        // 如果不是街道级别，清除经纬度和图片信息
        if (region.getLevel() != null && region.getLevel() != 4) {
            region.setLongitude(null);
            region.setLatitude(null);
            region.setImageUrl(null);
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (region.getId() == null) {
            // 新增
            region.setCreateTime(now);
            region.setUpdateTime(now);
            baseMapper.insert(region);
        } else {
            // 修改
            region.setUpdateTime(now);
            baseMapper.updateById(region);
        }
        
        RegionDTO result = convertToDTO(region);
        result.setHasChildren(false); // 新增的节点肯定没有子节点
        return result;
    }

    /**
     * 根据ID查询区域
     */
    @Override
    public RegionDTO getRegionById(Long id) {
        Region region = baseMapper.selectById(id);
        if (region == null) {
            return null;
        }
        
        RegionDTO dto = convertToDTO(region);
        // 检查是否有子节点
        int childCount = baseMapper.countChildrenById(id);
        dto.setHasChildren(childCount > 0);
        
        return dto;
    }

    /**
     * 删除区域（如果有子区域则不允许删除）
     */
    @Override
    @Transactional
    public boolean deleteRegion(Long id) {
        // 检查是否有子节点
        int childCount = baseMapper.countChildrenById(id);
        if (childCount > 0) {
            throw new RuntimeException("该区域下有子区域，不能删除");
        }
        
        // 执行删除操作
        return baseMapper.deleteById(id) > 0;
    }

    /**
     * 上传街道图片
     */
    @Override
    public String uploadStreetImage(MultipartFile file, Long streetId) {
        // 检查街道是否存在
        Region street = baseMapper.selectById(streetId);
        if (street == null) {
            throw new RuntimeException("街道不存在");
        }
        
        // 检查是否是街道级别
        if (street.getLevel() != 4) {
            throw new RuntimeException("只有街道级别才能上传图片");
        }
        
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 构建MinIO对象名称
            String objectName = "region_images/" + dateDir + "/" + newFileName;
            
            // 上传到MinIO
            String imageUrl = minioService.uploadFile(file, objectName);
            
            // 更新街道图片URL
            street.setImageUrl(imageUrl);
            street.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(street);
            
            return imageUrl;
        } catch (Exception e) {
            throw new RuntimeException("上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 获取完整的区域路径（从省到当前级别）
     */
    @Override
    public List<RegionDTO> getRegionPath(Long id) {
        List<Region> path = baseMapper.findPath(id);
        return path.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为DTO
     */
    private RegionDTO convertToDTO(Region region) {
        if (region == null) {
            return null;
        }
        
        RegionDTO dto = new RegionDTO();
        BeanUtils.copyProperties(region, dto);
        
        // 确保布尔值字段以布尔类型返回
        dto.setIsStreet(region.getIsStreet() != null ? region.getIsStreet() : region.getLevel() == 4);
        dto.setHasChildren(region.getHasChildren() != null ? region.getHasChildren() : false);
        
        return dto;
    }
} 