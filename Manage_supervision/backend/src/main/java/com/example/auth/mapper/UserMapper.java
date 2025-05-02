package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据角色ID查找用户
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId}")
    List<User> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID分页查询用户
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId}")
    IPage<User> findByRoleIdPage(Page<User> page, @Param("roleId") Long roleId);
    
    /**
     * 根据多条件模糊查询用户
     */
    @Select("SELECT DISTINCT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE (#{username} IS NULL OR u.username LIKE CONCAT('%', #{username}, '%')) " +
            "AND (#{roleName} IS NULL OR r.name = #{roleName}) " +
            "AND (#{status} IS NULL OR u.status = #{status})")
    IPage<User> findByConditions(Page<User> page, 
                                @Param("username") String username, 
                                @Param("roleName") String roleName, 
                                @Param("status") String status);
    
    /**
     * 计算特定角色的用户数量
     */
    @Select("SELECT COUNT(DISTINCT u.id) FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.id = #{roleId}")
    Long countByRoleId(@Param("roleId") Long roleId);
} 