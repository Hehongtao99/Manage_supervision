package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    /**
     * 分页查询课程（带条件）
     */
    IPage<Course> findByConditions(Page<Course> page, 
                                  @Param("courseName") String courseName,
                                  @Param("teacherName") String teacherName,
                                  @Param("category") String category,
                                  @Param("status") String status);
    
    /**
     * 根据教师ID查询课程
     */
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据课程类别查询课程
     */
    List<Course> findByCategory(@Param("category") String category);
} 