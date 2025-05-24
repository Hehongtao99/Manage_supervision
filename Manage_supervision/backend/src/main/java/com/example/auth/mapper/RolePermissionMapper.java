package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Permission;
import com.example.auth.model.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查询权限列表
     */
    @Select("SELECT DISTINCT p.* FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Permission> selectPermissionsByUserId(@Param("userId") Long userId);
} 