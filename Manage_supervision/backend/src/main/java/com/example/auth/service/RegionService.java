package com.example.auth.service;

import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.Region;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 省市区街道服务接口
 */
public interface RegionService {
    
    /**
     * 根据父ID查询子区域（树形结构）
     */
    List<RegionDTO> getRegionTree(Long parentId);
    
    /**
     * 根据父ID查询子区域（列表结构）
     */
    List<RegionDTO> getRegionList(Long parentId);
    
    /**
     * 保存区域信息
     */
    RegionDTO saveRegion(RegionDTO regionDTO);
    
    /**
     * 根据ID查询区域
     */
    RegionDTO getRegionById(Long id);
    
    /**
     * 删除区域（如果有子区域则不允许删除）
     */
    boolean deleteRegion(Long id);
    
    /**
     * 上传街道图片
     */
    String uploadStreetImage(MultipartFile file, Long streetId);
    
    /**
     * 获取完整的区域路径（从省到当前级别）
     */
    List<RegionDTO> getRegionPath(Long id);
} 