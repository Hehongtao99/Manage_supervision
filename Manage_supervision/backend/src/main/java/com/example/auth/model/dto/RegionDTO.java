package com.example.auth.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RegionDTO {
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private String parentName;
    private Integer level;
    private Integer sort;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<RegionDTO> children;
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCode() {
        return code;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public String getParentName() {
        return parentName;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public Integer getSort() {
        return sort;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public List<RegionDTO> getChildren() {
        return children;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }
    
    public void setSort(Integer sort) {
        this.sort = sort;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public void setChildren(List<RegionDTO> children) {
        this.children = children;
    }
} 