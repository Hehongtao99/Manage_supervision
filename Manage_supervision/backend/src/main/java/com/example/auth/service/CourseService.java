package com.example.auth.service;

import com.example.auth.model.dto.CourseDTO;
import com.example.auth.model.dto.PageResponse;

import java.util.List;

public interface CourseService {

    /**
     * 分页查询课程列表
     */
    PageResponse<CourseDTO> getCourseList(int page, int size, String courseName, Long teacherId, String category, String status);
    
    /**
     * 根据ID获取课程详情
     */
    CourseDTO getCourseById(Long id);
    
    /**
     * 创建新课程
     */
    CourseDTO createCourse(CourseDTO courseDTO);
    
    /**
     * 更新课程信息
     */
    CourseDTO updateCourse(Long id, CourseDTO courseDTO);
    
    /**
     * 删除课程（逻辑删除）
     */
    void deleteCourse(Long id);
    
    /**
     * 根据教师ID获取课程列表
     */
    List<CourseDTO> getCoursesByTeacherId(Long teacherId);
    
    /**
     * 根据类别获取课程列表
     */
    List<CourseDTO> getCoursesByCategory(String category);
    
    /**
     * 获取课程总数
     */
    Long countCourses();
    
    /**
     * 获取教师的课程数量
     */
    Long countCoursesByTeacherId(Long teacherId);
} 