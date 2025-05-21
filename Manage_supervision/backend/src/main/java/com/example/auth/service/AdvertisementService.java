package com.example.auth.service;

import com.example.auth.model.dto.AdvertisementApplicationDTO;
import com.example.auth.model.dto.PageResponse;

import java.math.BigDecimal;

public interface AdvertisementService {
    
    /**
     * 获取广告申请列表
     * @param page 页码
     * @param size 每页数量
     * @param applicationNumber 申请编号
     * @param area 区域
     * @param location 广告位置
     * @param adType 广告类型
     * @param adNature 广告性质
     * @param minSize 最小面积
     * @param maxSize 最大面积
     * @param status 状态
     * @return 分页数据
     */
    PageResponse<AdvertisementApplicationDTO> getAdvertisementApplicationList(
            int page, int size, String applicationNumber, String area, 
            String location, String adType, String adNature, 
            BigDecimal minSize, BigDecimal maxSize, String status);
    
    /**
     * 创建广告申请
     * @param dto 广告申请DTO
     * @return 创建后的DTO
     */
    AdvertisementApplicationDTO createAdvertisementApplication(AdvertisementApplicationDTO dto);
    
    /**
     * 更新广告申请
     * @param id 申请ID
     * @param dto 广告申请DTO
     * @return 更新后的DTO
     */
    AdvertisementApplicationDTO updateAdvertisementApplication(Long id, AdvertisementApplicationDTO dto);
    
    /**
     * 审核广告申请
     * @param id 申请ID
     * @param status 审核状态
     * @param remark 备注
     * @return 更新后的DTO
     */
    AdvertisementApplicationDTO reviewAdvertisementApplication(Long id, String status, String remark);
    
    /**
     * 获取广告申请详情
     * @param id 申请ID
     * @return 申请DTO
     */
    AdvertisementApplicationDTO getAdvertisementApplicationById(Long id);
    
    /**
     * 生成广告申请编号
     * @return 申请编号
     */
    String generateApplicationNumber();
} 