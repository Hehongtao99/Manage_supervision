package com.example.auth.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ScenicSpotDTO {
    private Long id;
    private String name;
    private String description;
    private Long provinceId;
    private String provinceName;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private String address;
    private String level;
    private String businessHours;
    private BigDecimal ticketPrice;
    private String contactPhone;
    private String imageUrl;
    private String status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 地区完整路径
    private String locationPath;
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Long getProvinceId() {
        return provinceId;
    }
    
    public String getProvinceName() {
        return provinceName;
    }
    
    public Long getCityId() {
        return cityId;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    public Long getDistrictId() {
        return districtId;
    }
    
    public String getDistrictName() {
        return districtName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getLevel() {
        return level;
    }
    
    public String getBusinessHours() {
        return businessHours;
    }
    
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Integer getSort() {
        return sort;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public String getLocationPath() {
        return locationPath;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }
    
    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
    
    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }
    
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }
    
    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setSort(Integer sort) {
        this.sort = sort;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public void setLocationPath(String locationPath) {
        this.locationPath = locationPath;
    }
} 