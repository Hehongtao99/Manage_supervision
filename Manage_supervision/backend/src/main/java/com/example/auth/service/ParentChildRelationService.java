package com.example.auth.service;

import java.util.List;
import com.example.auth.entity.ParentChildRelation;

/**
 * 家长与子女关系服务接口
 */
public interface ParentChildRelationService {
    
    /**
     * 检查是否是子女的家长
     * 
     * @param parentId 家长ID
     * @param childId 子女ID
     * @return 是否为子女的家长
     */
    boolean isParentOf(Long parentId, Long childId);
    
    /**
     * 获取父子关系
     * 
     * @param parentId 家长ID
     * @param childId 子女ID
     * @return 父子关系实体
     */
    ParentChildRelation getRelation(Long parentId, Long childId);
    
    /**
     * 根据家长ID获取所有已确认的子女关系
     * 
     * @param parentId 家长ID
     * @return 子女关系列表
     */
    List<ParentChildRelation> getConfirmedRelationsByParentId(Long parentId);
    
    /**
     * 检查家长和子女是否有关系
     * 
     * @param parentId 家长ID
     * @param childId 子女ID
     * @return 是否存在关系
     */
    boolean hasRelation(Long parentId, Long childId);
} 