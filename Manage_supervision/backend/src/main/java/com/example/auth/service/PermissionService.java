package com.example.auth.service;

import com.example.auth.model.entity.Permission;
import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {
    
    /**
     * 查询所有权限
     */
    List<Permission> findAll();
    
    /**
     * 根据ID查询权限
     */
    Permission findById(Long id);
    
    /**
     * 根据编码查询权限
     */
    Permission findByCode(String code);
    
    /**
     * 查询用户的所有权限
     */
    List<Permission> findByUserId(Long userId);
    
    /**
     * 查询角色的所有权限
     */
    List<Permission> findByRoleId(Long roleId);
    
    /**
     * 为角色分配权限
     */
    void assignToRole(Long roleId, List<Long> permissionIds);
    
    /**
     * 创建或更新权限
     */
    Permission save(Permission permission);
    
    /**
     * 删除权限
     */
    void delete(Long id);
    
    /**
     * 构建树形权限菜单
     */
    List<Permission> buildTree(List<Permission> permissions);
} 