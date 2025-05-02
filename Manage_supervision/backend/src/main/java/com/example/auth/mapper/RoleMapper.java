package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据角色名查找角色
     */
    @Select("SELECT * FROM roles WHERE name = #{name}")
    Role findByName(@Param("name") String name);
    
    /**
     * 根据多个角色名查找角色
     */
    @Select("<script>" +
            "SELECT * FROM roles WHERE name IN " +
            "<foreach collection='names' item='name' open='(' separator=',' close=')'>" +
            "#{name}" +
            "</foreach>" +
            "</script>")
    List<Role> findByNameIn(@Param("names") List<String> names);
    
    /**
     * 查询用户的所有角色
     */
    @Select("SELECT r.* FROM roles r " +
            "JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
} 