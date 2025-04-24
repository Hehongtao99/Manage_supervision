package com.example.auth.dto;

import java.time.LocalDateTime;

public class ParentChildRelationDTO {
    private Long id;
    private Long parentId;
    private String parentName;
    private String parentUsername;
    private Long childId;
    private String childName;
    private String childUsername;
    private String childUserNumber;
    private String relationType;
    private String status;
    private String className;
    private LocalDateTime createTime;
    
    public ParentChildRelationDTO() {
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getParentName() {
        return parentName;
    }
    
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
    public String getParentUsername() {
        return parentUsername;
    }
    
    public void setParentUsername(String parentUsername) {
        this.parentUsername = parentUsername;
    }
    
    public Long getChildId() {
        return childId;
    }
    
    public void setChildId(Long childId) {
        this.childId = childId;
    }
    
    public String getChildName() {
        return childName;
    }
    
    public void setChildName(String childName) {
        this.childName = childName;
    }
    
    public String getChildUsername() {
        return childUsername;
    }
    
    public void setChildUsername(String childUsername) {
        this.childUsername = childUsername;
    }
    
    public String getChildUserNumber() {
        return childUserNumber;
    }
    
    public void setChildUserNumber(String childUserNumber) {
        this.childUserNumber = childUserNumber;
    }
    
    public String getRelationType() {
        return relationType;
    }
    
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
} 