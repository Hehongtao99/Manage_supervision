package com.example.auth.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegionDTO {
    
    private Long id;
    
    // 父级ID
    private Long parentId;
    
    // 区域名称
    private String name;
    
    // 区域编码
    private String code;
    
    // 区域级别：1-省, 2-市, 3-区/县, 4-街道/乡镇
    private Integer level;
    
    // 排序号
    private Integer sortOrder;
    
    // 创建时间
    private String createTime;
    
    // 更新时间
    private String updateTime;
    
    // 子区域
    private List<RegionDTO> children;
    
    // 值，用于前端选择器
    private String value;
    
    // 标签，用于前端选择器
    private String label;
} 