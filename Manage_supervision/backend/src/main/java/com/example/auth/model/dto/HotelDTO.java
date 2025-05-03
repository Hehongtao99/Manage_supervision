package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HotelDTO {
    private Long id;
    private String name;
    private String description;
    
    // 地区信息
    private Long provinceId;
    private String provinceName;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private String address;
    
    // 位置信息
    private String locationPath;
    
    // 酒店信息
    private String level;
    private String imageUrl;
    private String contactPhone;
    private Double startPrice;
    
    // 景区关联信息
    private Long scenicSpotId;
    private String scenicSpotName;
    private Double distanceToSpot;
    
    // 管理信息
    private String status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 