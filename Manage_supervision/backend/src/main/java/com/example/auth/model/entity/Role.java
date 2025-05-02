package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("roles")
public class Role {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;
    
    private String description;
    
    private String permissions;
    
    @TableField("create_time")
    private LocalDateTime createTime;

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
    
    public String getPermissions() {
        return permissions;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
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
    
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
} 