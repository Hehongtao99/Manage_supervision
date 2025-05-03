package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("scenic_spots")
public class ScenicSpot {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;
    
    private String description;
    
    @TableField("province_id")
    private Long provinceId;
    
    @TableField("city_id")
    private Long cityId;
    
    @TableField("district_id")
    private Long districtId;
    
    private String address;
    
    private String level;
    
    @TableField("business_hours")
    private String businessHours;
    
    @TableField("ticket_price")
    private BigDecimal ticketPrice;
    
    @TableField("contact_phone")
    private String contactPhone;
    
    @TableField("image_url")
    private String imageUrl;
    
    private String status;
    
    private Integer sort;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 额外字段，不在数据库中
    @TableField(exist = false)
    private String provinceName;
    
    @TableField(exist = false)
    private String cityName;
    
    @TableField(exist = false)
    private String districtName;

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

    public Long getCityId() {
        return cityId;
    }

    public Long getDistrictId() {
        return districtId;
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

    public String getProvinceName() {
        return provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getDistrictName() {
        return districtName;
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

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
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

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }
} 