package com.example.auth.controller;

import com.example.auth.annotation.RequirePermission;
import com.example.auth.common.Result;
import com.example.auth.model.dto.RoleDTO;
import com.example.auth.model.entity.Permission;
import com.example.auth.service.AdminService;
import com.example.auth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取所有角色
     */
    @GetMapping
    @RequirePermission({"role:view"})
    public Result<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = adminService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 创建角色
     */
    @PostMapping
    @RequirePermission({"role:add"})
    public Result<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        RoleDTO newRole = adminService.createRole(roleDTO);
        return Result.success(newRole);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @RequirePermission({"role:edit"})
    public Result<RoleDTO> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        RoleDTO updatedRole = adminService.updateRole(id, roleDTO);
        if (updatedRole == null) {
            return Result.fail("角色不存在");
        }
        return Result.success(updatedRole);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @RequirePermission({"role:delete"})
    public Result<Void> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return Result.success();
    }

    /**
     * 获取角色的权限
     */
    @GetMapping("/{id}/permissions")
    @RequirePermission({"role:view"})
    public Result<List<String>> getRolePermissions(@PathVariable Long id) {
        List<Permission> permissions = permissionService.findByRoleId(id);
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
        return Result.success(permissionCodes);
    }

    /**
     * 分配角色权限
     */
    @PostMapping("/{id}/permissions")
    @RequirePermission({"role:edit"})
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<String>> request) {
        List<String> permissionCodes = request.get("permissions");
        if (permissionCodes == null) {
            return Result.fail("权限列表不能为空");
        }

        // 将权限编码转换为权限ID
        List<Long> permissionIds = permissionCodes.stream()
                .map(code -> {
                    Permission permission = permissionService.findByCode(code);
                    return permission != null ? permission.getId() : null;
                })
                .filter(permId -> permId != null)
                .collect(Collectors.toList());

        // 分配权限
        permissionService.assignToRole(id, permissionIds);
        return Result.success();
    }

    /**
     * 获取所有权限菜单树
     */
    @GetMapping("/permissions/tree")
    @RequirePermission({"role:view"})
    public Result<List<Permission>> getPermissionTree() {
        List<Permission> permissions = permissionService.findAll();
        List<Permission> tree = permissionService.buildTree(permissions);
        return Result.success(tree);
    }
} 