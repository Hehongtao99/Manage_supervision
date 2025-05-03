package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hotels")
public class Hotel {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 酒店名称
    private String name;
    
    // 酒店描述
    private String description;
    
    // 省市区ID
    private Long provinceId;
    
    @TableField(exist = false)
    private String provinceName;
    
    private Long cityId;
    
    @TableField(exist = false)
    private String cityName;
    
    private Long districtId;
    
    @TableField(exist = false)
    private String districtName;
    
    // 详细地址
    private String address;
    
    // 酒店等级（五星、四星等）
    private String level;
    
    // 酒店图片
    private String imageUrl;
    
    // 联系电话
    private String contactPhone;
    
    // 关联景区ID
    private Long scenicSpotId;
    
    @TableField(exist = false)
    private String scenicSpotName;
    
    // 距离景区距离（单位：公里）
    private BigDecimal distanceToSpot;
    
    // 价格范围（起始价格）
    private BigDecimal startPrice;
    
    // 状态：active-启用，inactive-禁用
    private String status;
    
    // 排序值
    private Integer sort;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 