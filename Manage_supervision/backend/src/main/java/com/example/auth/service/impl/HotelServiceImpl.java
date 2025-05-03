package com.example.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.HotelMapper;
import com.example.auth.mapper.RegionMapper;
import com.example.auth.mapper.ScenicSpotMapper;
import com.example.auth.model.dto.HotelDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.Hotel;
import com.example.auth.model.entity.Region;
import com.example.auth.model.entity.ScenicSpot;
import com.example.auth.service.HotelService;
import com.example.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelServiceImpl implements HotelService {

    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private RegionMapper regionMapper;
    
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;
    
    @Override
    public PageResponse<HotelDTO> getHotelList(int page, int size, String name, 
                                           Long provinceId, Long cityId, Long districtId, 
                                           String level, Long scenicSpotId, String status) {
        Page<Hotel> pageParam = new Page<>(page, size);
        
        Page<Hotel> result = hotelMapper.findByConditions(
                pageParam, name, provinceId, cityId, districtId, level, scenicSpotId, status);
        
        List<HotelDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(dtoList, result.getTotal(), page, size);
    }
    
    @Override
    public HotelDTO getHotelById(Long id) {
        Hotel hotel = hotelMapper.findDetailById(id);
        if (hotel != null) {
            return convertToDTO(hotel);
        }
        return null;
    }
    
    @Override
    @Transactional
    public HotelDTO addHotel(HotelDTO hotelDTO) {
        // 验证省份
        if (hotelDTO.getProvinceId() != null) {
            Region province = regionMapper.selectById(hotelDTO.getProvinceId());
            if (province == null) {
                throw new IllegalArgumentException("所选省份不存在");
            }
        } else {
            throw new IllegalArgumentException("省份ID不能为空");
        }
        
        // 验证城市
        if (hotelDTO.getCityId() != null) {
            Region city = regionMapper.selectById(hotelDTO.getCityId());
            if (city == null) {
                throw new IllegalArgumentException("所选城市不存在");
            }
        }
        
        // 验证区县
        if (hotelDTO.getDistrictId() != null) {
            Region district = regionMapper.selectById(hotelDTO.getDistrictId());
            if (district == null) {
                throw new IllegalArgumentException("所选区县不存在");
            }
        }
        
        // 验证关联景区
        if (hotelDTO.getScenicSpotId() != null) {
            ScenicSpot scenicSpot = scenicSpotMapper.selectById(hotelDTO.getScenicSpotId());
            if (scenicSpot == null) {
                throw new IllegalArgumentException("所选景区不存在");
            }
        }
        
        // 构建实体
        Hotel hotel = new Hotel();
        hotel.setName(hotelDTO.getName());
        hotel.setDescription(hotelDTO.getDescription());
        hotel.setProvinceId(hotelDTO.getProvinceId());
        hotel.setCityId(hotelDTO.getCityId());
        hotel.setDistrictId(hotelDTO.getDistrictId());
        hotel.setAddress(hotelDTO.getAddress());
        hotel.setLevel(hotelDTO.getLevel());
        hotel.setContactPhone(hotelDTO.getContactPhone());
        hotel.setImageUrl(hotelDTO.getImageUrl());
        hotel.setScenicSpotId(hotelDTO.getScenicSpotId());
        hotel.setDistanceToSpot(hotelDTO.getDistanceToSpot() != null ? 
                new BigDecimal(hotelDTO.getDistanceToSpot().toString()) : null);
        hotel.setStartPrice(hotelDTO.getStartPrice() != null ? 
                new BigDecimal(hotelDTO.getStartPrice().toString()) : null);
        hotel.setStatus("active"); // 默认为启用状态
        hotel.setSort(hotelDTO.getSort() != null ? hotelDTO.getSort() : 0);
        hotel.setCreateTime(LocalDateTime.now());
        hotel.setUpdateTime(LocalDateTime.now());
        
        // 保存酒店
        hotelMapper.insert(hotel);
        
        // 查询详情并返回
        Hotel savedHotel = hotelMapper.findDetailById(hotel.getId());
        return convertToDTO(savedHotel);
    }
    
    @Override
    @Transactional
    public HotelDTO updateHotel(Long id, HotelDTO hotelDTO) {
        // 检查酒店是否存在
        Hotel hotel = hotelMapper.selectById(id);
        if (hotel == null) {
            throw new IllegalArgumentException("酒店不存在");
        }
        
        // 验证省份
        if (hotelDTO.getProvinceId() != null) {
            Region province = regionMapper.selectById(hotelDTO.getProvinceId());
            if (province == null) {
                throw new IllegalArgumentException("所选省份不存在");
            }
        }
        
        // 验证城市
        if (hotelDTO.getCityId() != null) {
            Region city = regionMapper.selectById(hotelDTO.getCityId());
            if (city == null) {
                throw new IllegalArgumentException("所选城市不存在");
            }
        }
        
        // 验证区县
        if (hotelDTO.getDistrictId() != null) {
            Region district = regionMapper.selectById(hotelDTO.getDistrictId());
            if (district == null) {
                throw new IllegalArgumentException("所选区县不存在");
            }
        }
        
        // 验证关联景区
        if (hotelDTO.getScenicSpotId() != null) {
            ScenicSpot scenicSpot = scenicSpotMapper.selectById(hotelDTO.getScenicSpotId());
            if (scenicSpot == null) {
                throw new IllegalArgumentException("所选景区不存在");
            }
        }
        
        // 更新字段
        hotel.setName(hotelDTO.getName());
        hotel.setDescription(hotelDTO.getDescription());
        hotel.setProvinceId(hotelDTO.getProvinceId());
        hotel.setCityId(hotelDTO.getCityId());
        hotel.setDistrictId(hotelDTO.getDistrictId());
        hotel.setAddress(hotelDTO.getAddress());
        hotel.setLevel(hotelDTO.getLevel());
        hotel.setContactPhone(hotelDTO.getContactPhone());
        hotel.setImageUrl(hotelDTO.getImageUrl());
        hotel.setScenicSpotId(hotelDTO.getScenicSpotId());
        hotel.setDistanceToSpot(hotelDTO.getDistanceToSpot() != null ? 
                new BigDecimal(hotelDTO.getDistanceToSpot().toString()) : null);
        hotel.setStartPrice(hotelDTO.getStartPrice() != null ? 
                new BigDecimal(hotelDTO.getStartPrice().toString()) : null);
        hotel.setSort(hotelDTO.getSort());
        hotel.setUpdateTime(LocalDateTime.now());
        
        // 更新酒店
        hotelMapper.updateById(hotel);
        
        // 查询详情并返回
        Hotel updatedHotel = hotelMapper.findDetailById(id);
        return convertToDTO(updatedHotel);
    }
    
    @Override
    @Transactional
    public boolean deleteHotel(Long id) {
        Hotel hotel = hotelMapper.selectById(id);
        if (hotel == null) {
            throw new IllegalArgumentException("酒店不存在");
        }
        
        int result = hotelMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean toggleHotelStatus(Long id) {
        Hotel hotel = hotelMapper.selectById(id);
        if (hotel == null) {
            return false;
        }
        
        // 切换状态
        String newStatus = "active".equals(hotel.getStatus()) ? "inactive" : "active";
        hotel.setStatus(newStatus);
        hotel.setUpdateTime(LocalDateTime.now());
        
        return hotelMapper.updateById(hotel) > 0;
    }
    
    @Override
    public List<HotelDTO> getHotelsByScenicSpot(Long scenicSpotId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5; // 默认5条
        }
        
        List<Hotel> hotels = hotelMapper.findHotelsByScenicSpot(scenicSpotId, limit);
        return hotels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public HotelDTO convertToDTO(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        
        HotelDTO dto = new HotelDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setDescription(hotel.getDescription());
        
        // 地区信息
        dto.setProvinceId(hotel.getProvinceId());
        dto.setProvinceName(hotel.getProvinceName());
        dto.setCityId(hotel.getCityId());
        dto.setCityName(hotel.getCityName());
        dto.setDistrictId(hotel.getDistrictId());
        dto.setDistrictName(hotel.getDistrictName());
        dto.setAddress(hotel.getAddress());
        
        // 酒店信息
        dto.setLevel(hotel.getLevel());
        dto.setImageUrl(hotel.getImageUrl());
        dto.setContactPhone(hotel.getContactPhone());
        dto.setStartPrice(hotel.getStartPrice() != null ? hotel.getStartPrice().doubleValue() : null);
        
        // 景区关联信息
        dto.setScenicSpotId(hotel.getScenicSpotId());
        dto.setScenicSpotName(hotel.getScenicSpotName());
        dto.setDistanceToSpot(hotel.getDistanceToSpot() != null ? hotel.getDistanceToSpot().doubleValue() : null);
        
        // 管理信息
        dto.setStatus(hotel.getStatus());
        dto.setSort(hotel.getSort());
        dto.setCreateTime(hotel.getCreateTime());
        dto.setUpdateTime(hotel.getUpdateTime());
        
        // 构建地区完整路径
        StringBuilder locationPath = new StringBuilder();
        if (hotel.getProvinceName() != null) {
            locationPath.append(hotel.getProvinceName());
            
            if (hotel.getCityName() != null) {
                locationPath.append("/").append(hotel.getCityName());
                
                if (hotel.getDistrictName() != null) {
                    locationPath.append("/").append(hotel.getDistrictName());
                }
            }
        }
        dto.setLocationPath(locationPath.toString());
        
        return dto;
    }
} 