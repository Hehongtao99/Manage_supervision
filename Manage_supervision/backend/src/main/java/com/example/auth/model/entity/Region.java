package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("region")
public class Region {
    
    @TableId(type = IdType.AUTO)
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
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 子区域（非数据库字段）
    @TableField(exist = false)
    private List<Region> children;
} 