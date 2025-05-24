package com.example.auth.controller;

import com.example.auth.annotation.RequirePermission;
import com.example.auth.common.Result;
import com.example.auth.model.entity.Permission;
import com.example.auth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 获取所有权限
     */
    @GetMapping
    @RequirePermission({"permission:view"})
    public Result<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionService.findAll();
        return Result.success(permissions);
    }
    
    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    @RequirePermission({"permission:view"})
    public Result<List<Permission>> getPermissionTree() {
        List<Permission> permissions = permissionService.findAll();
        List<Permission> tree = permissionService.buildTree(permissions);
        return Result.success(tree);
    }
    
    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    @RequirePermission({"permission:view"})
    public Result<Permission> getPermissionById(@PathVariable Long id) {
        Permission permission = permissionService.findById(id);
        if (permission == null) {
            return Result.fail("权限不存在");
        }
        return Result.success(permission);
    }
    
    /**
     * 创建权限
     */
    @PostMapping
    @RequirePermission({"permission:add"})
    public Result<Permission> createPermission(@RequestBody Permission permission) {
        // 检查权限编码是否已存在
        Permission existingPermission = permissionService.findByCode(permission.getCode());
        if (existingPermission != null) {
            return Result.fail("权限编码已存在");
        }
        
        Permission savedPermission = permissionService.save(permission);
        return Result.success(savedPermission);
    }
    
    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @RequirePermission({"permission:edit"})
    public Result<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        Permission existingPermission = permissionService.findById(id);
        if (existingPermission == null) {
            return Result.fail("权限不存在");
        }
        
        // 检查权限编码是否已被其他权限使用
        Permission codePermission = permissionService.findByCode(permission.getCode());
        if (codePermission != null && !codePermission.getId().equals(id)) {
            return Result.fail("权限编码已被其他权限使用");
        }
        
        permission.setId(id);
        Permission updatedPermission = permissionService.save(permission);
        return Result.success(updatedPermission);
    }
    
    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @RequirePermission({"permission:delete"})
    public Result<Void> deletePermission(@PathVariable Long id) {
        Permission permission = permissionService.findById(id);
        if (permission == null) {
            return Result.fail("权限不存在");
        }
        
        permissionService.delete(id);
        return Result.success();
    }
    
    /**
     * 为角色分配权限
     */
    @PostMapping("/assign")
    @RequirePermission({"permission:assign"})
    public Result<Void> assignPermissionsToRole(@RequestParam Long roleId, @RequestBody List<Long> permissionIds) {
        permissionService.assignToRole(roleId, permissionIds);
        return Result.success();
    }
    
    /**
     * 获取角色的权限
     */
    @GetMapping("/role/{roleId}")
    @RequirePermission({"permission:view"})
    public Result<List<Permission>> getPermissionsByRoleId(@PathVariable Long roleId) {
        List<Permission> permissions = permissionService.findByRoleId(roleId);
        return Result.success(permissions);
    }
} 