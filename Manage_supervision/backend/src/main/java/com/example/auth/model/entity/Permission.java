package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("permissions")
public class Permission {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;
    
    private String name;
    
    private String description;
    
    @TableField("parent_id")
    private Long parentId;
    
    private String type;
    
    private String path;
    
    private String component;
    
    private String icon;
    
    private Integer sort;
    
    @TableField("is_visible")
    private Boolean isVisible;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;

    // Getters
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public String getType() {
        return type;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getComponent() {
        return component;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public Integer getSort() {
        return sort;
    }
    
    public Boolean getIsVisible() {
        return isVisible;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public void setComponent(String component) {
        this.component = component;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public void setSort(Integer sort) {
        this.sort = sort;
    }
    
    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
} 