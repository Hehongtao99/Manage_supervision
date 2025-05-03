package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;

@TableName("regions")
public class Region {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;
    
    private String code;
    
    @TableField("parent_id")
    private Long parentId;
    
    private Integer level;
    
    private Integer sort;
    
    private String status;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private List<Region> children;

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

    public List<Region> getChildren() {
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

    public void setChildren(List<Region> children) {
        this.children = children;
    }
} 