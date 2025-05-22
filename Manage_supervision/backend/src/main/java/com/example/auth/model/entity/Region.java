package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 省市区街道实体类
 */
@Data
@TableName("region")
public class Region {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 父级ID
     */
    private Long parentId;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 编码
     */
    private String code;
    
    /**
     * 级别: 1-省, 2-市, 3-区县, 4-街道
     */
    private Integer level;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 经度，仅街道级别有
     */
    private BigDecimal longitude;
    
    /**
     * 纬度，仅街道级别有
     */
    private BigDecimal latitude;
    
    /**
     * 图片URL, 仅街道级别有
     */
    private String imageUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 非数据库字段，标记是否有子节点
     */
    @TableField(exist = false)
    private Boolean hasChildren;
    
    /**
     * 非数据库字段，是否是街道级别
     */
    @TableField(exist = false)
    private Boolean isStreet;
    
    public Boolean getIsStreet() {
        return this.level != null && this.level == 4;
    }
} 