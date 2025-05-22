package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdvertisementApplicationDTO {
    
    private Long id;
    
    // 申请编号
    private String applicationNumber;
    
    // 区域
    private String area;
    
    // 广告位置
    private String location;
    
    // 广告设置类型
    private String adType;
    
    // 广告性质
    private String adNature;
    
    // 面积
    private BigDecimal size;
    
    // 状态
    private String status;
    
    // 申请人ID
    private Long applicantId;
    
    // 申请人用户名
    private String applicantName;
    
    // 申请人联系方式
    private String applicantPhone;
    
    // 创建时间
    private String createTime;
    
    // 更新时间
    private String updateTime;
    
    // 备注
    private String remark;
    
    // 详细地址
    private String detailedAddress;
    
    // 完整地址
    private String fullAddress;
    
    // 经度
    private Double longitude;
    
    // 纬度
    private Double latitude;
    
    // 省份ID
    private Long provinceId;
    
    // 城市ID
    private Long cityId;
    
    // 区县ID
    private Long districtId;
    
    // 街道ID
    private Long streetId;
    
    // 省份名称
    private String provinceName;
    
    // 城市名称
    private String cityName;
    
    // 区县名称
    private String districtName;
    
    // 街道名称
    private String streetName;
    
    // 街道图片URL
    private String streetImageUrl;
} 