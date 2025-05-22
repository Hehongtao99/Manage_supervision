package com.example.auth.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 省市区街道数据传输对象
 */
@Data
public class RegionDTO {
    
    private Long id;
    
    private Long parentId;
    
    private String name;
    
    private String code;
    
    private Integer level;
    
    private Integer sortOrder;
    
    private BigDecimal longitude;
    
    private BigDecimal latitude;
    
    private String imageUrl;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Boolean hasChildren;
    
    private Boolean isStreet;
    
    /**
     * 子节点列表，用于树形结构
     */
    private List<RegionDTO> children;
} 