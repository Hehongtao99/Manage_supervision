package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.PermissionMapper;
import com.example.auth.mapper.RolePermissionMapper;
import com.example.auth.model.entity.Permission;
import com.example.auth.model.entity.RolePermission;
import com.example.auth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Service
public class PermissionServiceImpl implements PermissionService {
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    @Override
    public List<Permission> findAll() {
        return permissionMapper.selectList(null);
    }
    
    @Override
    public Permission findById(Long id) {
        return permissionMapper.selectById(id);
    }
    
    @Override
    public Permission findByCode(String code) {
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getCode, code);
        return permissionMapper.selectOne(queryWrapper);
    }
    
    @Override
    public List<Permission> findByUserId(Long userId) {
        return rolePermissionMapper.selectPermissionsByUserId(userId);
    }
    
    @Override
    public List<Permission> findByRoleId(Long roleId) {
        return rolePermissionMapper.selectPermissionsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void assignToRole(Long roleId, List<Long> permissionIds) {
        // 删除原有权限
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(queryWrapper);
        
        // 分配新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreateTime(LocalDateTime.now());
                rolePermissionMapper.insert(rolePermission);
            }
        }
    }
    
    @Override
    @Transactional
    public Permission save(Permission permission) {
        if (permission.getId() == null) {
            // 新增
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            permissionMapper.insert(permission);
        } else {
            // 更新
            permission.setUpdateTime(LocalDateTime.now());
            permissionMapper.updateById(permission);
        }
        return permission;
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        // 删除权限
        permissionMapper.deleteById(id);
        
        // 删除角色-权限关联
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getPermissionId, id);
        rolePermissionMapper.delete(queryWrapper);
    }
    
    @Override
    public List<Permission> buildTree(List<Permission> permissions) {
        // 按父ID分组
        Map<Long, List<Permission>> permissionMap = permissions.stream()
                .collect(Collectors.groupingBy(Permission::getParentId));
        
        // 获取顶级权限（父ID为0或null）
        List<Permission> rootPermissions = permissionMap.getOrDefault(0L, new ArrayList<>());
        if (rootPermissions.isEmpty() && !permissions.isEmpty()) {
            // 如果没有父ID为0的权限，则取第一个权限作为根
            rootPermissions = List.of(permissions.get(0));
        }
        
        // 递归设置子权限
        rootPermissions.forEach(permission -> setChildren(permission, permissionMap));
        
        // 按排序字段排序
        return rootPermissions.stream()
                .sorted(Comparator.comparing(Permission::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
    
    /**
     * 递归设置子权限
     */
    private void setChildren(Permission permission, Map<Long, List<Permission>> permissionMap) {
        List<Permission> children = permissionMap.getOrDefault(permission.getId(), new ArrayList<>());
        if (!children.isEmpty()) {
            // 排序
            children = children.stream()
                    .sorted(Comparator.comparing(Permission::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
            
            // 设置子权限
            children.forEach(child -> setChildren(child, permissionMap));
        }
    }
} 