package com.example.auth.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserRoleMapper {
    
    /**
     * 添加用户角色关联
     */
    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 删除用户的所有角色关联
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);
    
    /**
     * 删除用户的特定角色关联
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 统计特定角色的用户数量
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE role_id = #{roleId}")
    int countUsersByRoleId(@Param("roleId") Long roleId);
} 