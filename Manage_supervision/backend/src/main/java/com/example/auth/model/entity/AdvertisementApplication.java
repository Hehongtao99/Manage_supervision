package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("advertisement_applications")
public class AdvertisementApplication {
    
    @TableId(type = IdType.AUTO)
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
    
    // 状态（pending-待审核, approved-已批准, rejected-已拒绝）
    private String status;
    
    // 申请人ID
    private Long applicantId;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 备注
    private String remark;
    
    // 详细地址
    private String detailedAddress;
    
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
    
    // 申请人信息（非数据库字段）
    @TableField(exist = false)
    private User applicant;
} 