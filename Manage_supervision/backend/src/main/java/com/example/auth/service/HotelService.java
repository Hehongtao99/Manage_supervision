package com.example.auth.service;

import com.example.auth.model.dto.HotelDTO;
import com.example.auth.model.dto.PageResponse;

import java.util.List;

public interface HotelService {
    
    /**
     * 获取酒店列表（分页）
     */
    PageResponse<HotelDTO> getHotelList(int page, int size, String name, 
                                     Long provinceId, Long cityId, Long districtId, 
                                     String level, Long scenicSpotId, String status);
    
    /**
     * 根据ID获取酒店详情
     */
    HotelDTO getHotelById(Long id);
    
    /**
     * 添加酒店
     */
    HotelDTO addHotel(HotelDTO hotelDTO);
    
    /**
     * 更新酒店
     */
    HotelDTO updateHotel(Long id, HotelDTO hotelDTO);
    
    /**
     * 删除酒店
     */
    boolean deleteHotel(Long id);
    
    /**
     * 切换酒店状态
     */
    boolean toggleHotelStatus(Long id);
    
    /**
     * 获取景区周边酒店
     */
    List<HotelDTO> getHotelsByScenicSpot(Long scenicSpotId, Integer limit);
    
    /**
     * 将实体转换为DTO
     */
    HotelDTO convertToDTO(com.example.auth.model.entity.Hotel hotel);
} 