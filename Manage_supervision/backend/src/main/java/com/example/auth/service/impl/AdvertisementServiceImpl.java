package com.example.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.AdvertisementApplicationMapper;
import com.example.auth.mapper.RegionMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.AdvertisementApplicationDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.AdvertisementApplication;
import com.example.auth.model.entity.Region;
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

@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    private AdvertisementApplicationMapper advertisementApplicationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RegionMapper regionMapper;
    
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
            
            // 查询区域信息
            setRegionInfo(advertisement, dto);
            
            advertisementDTOs.add(dto);
        }
        
        return new PageResponse<>(
                advertisementDTOs, 
                advertisementPage.getTotal(), 
                (int)pageParam.getCurrent(), 
                (int)pageParam.getSize());
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
        
        // 设置省市区街道ID
        advertisement.setProvinceId(dto.getProvinceId());
        advertisement.setCityId(dto.getCityId());
        advertisement.setDistrictId(dto.getDistrictId());
        advertisement.setStreetId(dto.getStreetId());
        advertisement.setDetailedAddress(dto.getDetailedAddress());
        
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
            
            // 省市区街道信息
            advertisement.setProvinceId(dto.getProvinceId());
            advertisement.setCityId(dto.getCityId());
            advertisement.setDistrictId(dto.getDistrictId());
            advertisement.setStreetId(dto.getStreetId());
            advertisement.setDetailedAddress(dto.getDetailedAddress());
            
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
            
            // 查询区域信息
            setRegionInfo(advertisement, dto);
            
            return dto;
        }
        
        return null;
    }
    
    /**
     * 设置区域相关信息
     */
    private void setRegionInfo(AdvertisementApplication advertisement, AdvertisementApplicationDTO dto) {
        // 省份信息
        if (advertisement.getProvinceId() != null) {
            Region province = regionMapper.selectById(advertisement.getProvinceId());
            if (province != null) {
                dto.setProvinceName(province.getName());
            }
        }
        
        // 城市信息
        if (advertisement.getCityId() != null) {
            Region city = regionMapper.selectById(advertisement.getCityId());
            if (city != null) {
                dto.setCityName(city.getName());
            }
        }
        
        // 区/县信息
        if (advertisement.getDistrictId() != null) {
            Region district = regionMapper.selectById(advertisement.getDistrictId());
            if (district != null) {
                dto.setDistrictName(district.getName());
            }
        }
        
        // 街道/乡镇信息
        if (advertisement.getStreetId() != null) {
            Region street = regionMapper.selectById(advertisement.getStreetId());
            if (street != null) {
                dto.setStreetName(street.getName());
            }
        }
        
        // 设置完整地址
        String fullAddress = regionService.getFullAddressPath(
                advertisement.getProvinceId(),
                advertisement.getCityId(),
                advertisement.getDistrictId(),
                advertisement.getStreetId());
        
        if (advertisement.getDetailedAddress() != null && !advertisement.getDetailedAddress().isEmpty()) {
            fullAddress += advertisement.getDetailedAddress();
        }
        
        dto.setFullAddress(fullAddress);
    }
    
    @Override
    public String generateApplicationNumber() {
        // 生成申请编号，格式：AD + 年月日 + 4位序号
        String prefix = "AD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询最大的申请编号
        String maxApplicationNumber = advertisementApplicationMapper.getMaxApplicationNumber();
        
        // 如果没有申请记录，从0001开始
        if (maxApplicationNumber == null || maxApplicationNumber.isEmpty()) {
            return prefix + "0001";
        }
        
        // 如果有申请记录，获取序号并加1
        try {
            String currentDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 如果最大的申请编号不是今天的，从0001开始
            if (!maxApplicationNumber.contains(currentDay)) {
                return prefix + "0001";
            }
            
            // 获取序号
            String sequence = maxApplicationNumber.substring(maxApplicationNumber.length() - 4);
            int nextSequence = Integer.parseInt(sequence) + 1;
            
            // 格式化为4位数字
            return prefix + String.format("%04d", nextSequence);
        } catch (Exception e) {
            // 异常情况下，使用时间戳
            return prefix + "0001";
        }
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
        
        // 区域相关信息
        dto.setProvinceId(advertisement.getProvinceId());
        dto.setCityId(advertisement.getCityId());
        dto.setDistrictId(advertisement.getDistrictId());
        dto.setStreetId(advertisement.getStreetId());
        dto.setDetailedAddress(advertisement.getDetailedAddress());
        
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