package com.example.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.AdvertisementApplicationMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.AdvertisementApplicationDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.AdvertisementApplication;
import com.example.auth.model.entity.User;
import com.example.auth.service.AdvertisementService;
import com.example.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    private AdvertisementApplicationMapper advertisementApplicationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RegionService regionService;
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResponse<AdvertisementApplicationDTO> getAdvertisementApplicationList(
            int page, int size, String applicationNumber, String area, 
            String location, String adType, String adNature, 
            BigDecimal minSize, BigDecimal maxSize, String status) {
        
        Page<AdvertisementApplication> pageParam = new Page<>(page, size);
        
        // 查询广告申请列表
        Page<AdvertisementApplication> advertisementPage = 
                (Page<AdvertisementApplication>) advertisementApplicationMapper.findByConditions(
                        pageParam, applicationNumber, area, location, adType, 
                        adNature, minSize, maxSize, status);
        
        // 转换为DTO列表
        List<AdvertisementApplicationDTO> advertisementDTOs = new ArrayList<>();
        
        for (AdvertisementApplication advertisement : advertisementPage.getRecords()) {
            AdvertisementApplicationDTO dto = convertToDTO(advertisement);
            
            // 查询申请人信息
            if (advertisement.getApplicantId() != null) {
                User applicant = userMapper.selectById(advertisement.getApplicantId());
                if (applicant != null) {
                    dto.setApplicantName(applicant.getRealName() != null ? 
                            applicant.getRealName() : applicant.getUsername());
                    dto.setApplicantPhone(applicant.getPhone());
                }
            }
            
            advertisementDTOs.add(dto);
        }
        
        // 构建分页响应
        PageResponse<AdvertisementApplicationDTO> response = new PageResponse<>(
                advertisementDTOs, 
                advertisementPage.getTotal(), 
                page, 
                size);
        
        return response;
    }
    
    @Override
    @Transactional
    public AdvertisementApplicationDTO createAdvertisementApplication(AdvertisementApplicationDTO dto) {
        AdvertisementApplication advertisement = new AdvertisementApplication();
        
        // 设置申请编号
        String applicationNumber = generateApplicationNumber();
        advertisement.setApplicationNumber(applicationNumber);
        
        advertisement.setArea(dto.getArea());
        advertisement.setLocation(dto.getLocation());
        advertisement.setAdType(dto.getAdType());
        advertisement.setAdNature(dto.getAdNature());
        advertisement.setSize(dto.getSize());
        advertisement.setStatus("pending");
        advertisement.setApplicantId(dto.getApplicantId());
        advertisement.setRemark(dto.getRemark());
        advertisement.setDetailedAddress(dto.getDetailedAddress());
        advertisement.setLongitude(dto.getLongitude());
        advertisement.setLatitude(dto.getLatitude());
        
        // 设置省市区街道
        advertisement.setProvinceId(dto.getProvinceId());
        advertisement.setCityId(dto.getCityId());
        advertisement.setDistrictId(dto.getDistrictId());
        advertisement.setStreetId(dto.getStreetId());
        
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        advertisement.setCreateTime(now);
        advertisement.setUpdateTime(now);
        
        // 保存广告申请
        advertisementApplicationMapper.insert(advertisement);
        
        // 返回DTO
        return getAdvertisementApplicationById(advertisement.getId());
    }
    
    @Override
    @Transactional
    public AdvertisementApplicationDTO updateAdvertisementApplication(Long id, AdvertisementApplicationDTO dto) {
        AdvertisementApplication advertisement = advertisementApplicationMapper.selectById(id);
        
        if (advertisement != null) {
            // 基本信息
            if (dto.getArea() != null) {
                advertisement.setArea(dto.getArea());
            }
            
            if (dto.getLocation() != null) {
                advertisement.setLocation(dto.getLocation());
            }
            
            if (dto.getAdType() != null) {
                advertisement.setAdType(dto.getAdType());
            }
            
            if (dto.getAdNature() != null) {
                advertisement.setAdNature(dto.getAdNature());
            }
            
            if (dto.getSize() != null) {
                advertisement.setSize(dto.getSize());
            }
            
            if (dto.getRemark() != null) {
                advertisement.setRemark(dto.getRemark());
            }
            
            advertisement.setDetailedAddress(dto.getDetailedAddress());
            advertisement.setLongitude(dto.getLongitude());
            advertisement.setLatitude(dto.getLatitude());
            
            // 设置省市区街道
            advertisement.setProvinceId(dto.getProvinceId());
            advertisement.setCityId(dto.getCityId());
            advertisement.setDistrictId(dto.getDistrictId());
            advertisement.setStreetId(dto.getStreetId());
            
            // 更新时间
            advertisement.setUpdateTime(LocalDateTime.now());
            
            // 更新广告申请
            advertisementApplicationMapper.updateById(advertisement);
            
            // 返回更新后的DTO
            return getAdvertisementApplicationById(id);
        }
        
        return null;
    }
    
    @Override
    @Transactional
    public AdvertisementApplicationDTO reviewAdvertisementApplication(Long id, String status, String remark) {
        AdvertisementApplication advertisement = advertisementApplicationMapper.selectById(id);
        
        if (advertisement != null) {
            // 更新状态和备注
            advertisement.setStatus(status);
            if (remark != null && !remark.isEmpty()) {
                advertisement.setRemark(remark);
            }
            
            // 更新时间
            advertisement.setUpdateTime(LocalDateTime.now());
            
            // 更新广告申请
            advertisementApplicationMapper.updateById(advertisement);
            
            // 返回更新后的DTO
            return getAdvertisementApplicationById(id);
        }
        
        return null;
    }
    
    @Override
    public AdvertisementApplicationDTO getAdvertisementApplicationById(Long id) {
        AdvertisementApplication advertisement = advertisementApplicationMapper.selectById(id);
        
        if (advertisement != null) {
            AdvertisementApplicationDTO dto = convertToDTO(advertisement);
            
            // 查询申请人信息
            if (advertisement.getApplicantId() != null) {
                User applicant = userMapper.selectById(advertisement.getApplicantId());
                if (applicant != null) {
                    dto.setApplicantName(applicant.getRealName() != null ? 
                            applicant.getRealName() : applicant.getUsername());
                    dto.setApplicantPhone(applicant.getPhone());
                }
            }
            
            return dto;
        }
        
        return null;
    }
    
    @Override
    public String generateApplicationNumber() {
        // 生成基于时间的申请编号，格式：AD + 年月日 + 4位随机数
        String prefix = "AD";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        Random random = new Random();
        String randomStr = String.format("%04d", random.nextInt(10000));
        
        return prefix + dateStr + randomStr;
    }
    
    /**
     * 将实体转换为DTO
     */
    private AdvertisementApplicationDTO convertToDTO(AdvertisementApplication advertisement) {
        AdvertisementApplicationDTO dto = new AdvertisementApplicationDTO();
        dto.setId(advertisement.getId());
        dto.setApplicationNumber(advertisement.getApplicationNumber());
        dto.setArea(advertisement.getArea());
        dto.setLocation(advertisement.getLocation());
        dto.setAdType(advertisement.getAdType());
        dto.setAdNature(advertisement.getAdNature());
        dto.setSize(advertisement.getSize());
        dto.setStatus(advertisement.getStatus());
        dto.setApplicantId(advertisement.getApplicantId());
        dto.setRemark(advertisement.getRemark());
        dto.setDetailedAddress(advertisement.getDetailedAddress());
        dto.setLongitude(advertisement.getLongitude());
        dto.setLatitude(advertisement.getLatitude());
        
        // 设置省市区街道ID
        dto.setProvinceId(advertisement.getProvinceId());
        dto.setCityId(advertisement.getCityId());
        dto.setDistrictId(advertisement.getDistrictId());
        dto.setStreetId(advertisement.getStreetId());
        
        // 如果有街道ID，获取街道详情
        if (advertisement.getStreetId() != null) {
            try {
                RegionDTO street = regionService.getRegionById(advertisement.getStreetId());
                if (street != null) {
                    dto.setStreetName(street.getName());
                    dto.setStreetImageUrl(street.getImageUrl());
                    if (dto.getLongitude() == null) {
                        dto.setLongitude(street.getLongitude() != null ? street.getLongitude().doubleValue() : null);
                    }
                    if (dto.getLatitude() == null) {
                        dto.setLatitude(street.getLatitude() != null ? street.getLatitude().doubleValue() : null);
                    }
                }
            } catch (Exception e) {
                // 忽略错误，不影响主流程
            }
        }
        
        // 如果有区县ID，获取区县名称
        if (advertisement.getDistrictId() != null) {
            try {
                RegionDTO district = regionService.getRegionById(advertisement.getDistrictId());
                if (district != null) {
                    dto.setDistrictName(district.getName());
                }
            } catch (Exception e) {
                // 忽略错误，不影响主流程
            }
        }
        
        // 如果有城市ID，获取城市名称
        if (advertisement.getCityId() != null) {
            try {
                RegionDTO city = regionService.getRegionById(advertisement.getCityId());
                if (city != null) {
                    dto.setCityName(city.getName());
                }
            } catch (Exception e) {
                // 忽略错误，不影响主流程
            }
        }
        
        // 如果有省份ID，获取省份名称
        if (advertisement.getProvinceId() != null) {
            try {
                RegionDTO province = regionService.getRegionById(advertisement.getProvinceId());
                if (province != null) {
                    dto.setProvinceName(province.getName());
                }
            } catch (Exception e) {
                // 忽略错误，不影响主流程
            }
        }
        
        // 格式化时间
        if (advertisement.getCreateTime() != null) {
            dto.setCreateTime(advertisement.getCreateTime().format(formatter));
        }
        
        if (advertisement.getUpdateTime() != null) {
            dto.setUpdateTime(advertisement.getUpdateTime().format(formatter));
        }
        
        return dto;
    }
} 