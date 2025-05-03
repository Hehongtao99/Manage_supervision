package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ScenicSpotDTO;
import com.example.auth.model.entity.ScenicSpot;
import java.util.List;

public interface ScenicSpotService {
    
    /**
     * 分页查询景区列表
     */
    PageResponse<ScenicSpotDTO> getScenicSpotList(int page, int size, String name, Long provinceId, 
                                        Long cityId, Long districtId, String level, String status);
    
    /**
     * 获取景区详情
     */
    ScenicSpotDTO getScenicSpotById(Long id);
    
    /**
     * 添加景区
     */
    ScenicSpotDTO addScenicSpot(ScenicSpotDTO scenicSpotDTO);
    
    /**
     * 更新景区
     */
    ScenicSpotDTO updateScenicSpot(Long id, ScenicSpotDTO scenicSpotDTO);
    
    /**
     * 删除景区
     */
    boolean deleteScenicSpot(Long id);
    
    /**
     * 切换景区状态（启用/停用）
     */
    boolean toggleScenicSpotStatus(Long id);
    
    /**
     * 获取热门景区
     */
    List<ScenicSpotDTO> getHotScenicSpots(Integer limit);
    
    /**
     * 将实体转换为DTO
     */
    ScenicSpotDTO convertToDTO(ScenicSpot scenicSpot);
} 