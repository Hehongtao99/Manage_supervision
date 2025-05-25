package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.CourseCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /**
     * 根据名称模糊查询课程类别
     */
    @Select("SELECT * FROM course_categories WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<CourseCategory> findByName(@Param("name") String name);
    
    /**
     * 分页查询课程类别
     */
    @Select("SELECT * FROM course_categories ORDER BY create_time DESC")
    IPage<CourseCategory> findPage(Page<CourseCategory> page);
    
    /**
     * 根据名称分页模糊查询
     */
    @Select("SELECT * FROM course_categories WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY create_time DESC")
    IPage<CourseCategory> findPageByName(Page<CourseCategory> page, @Param("name") String name);
} 