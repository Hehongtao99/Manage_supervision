package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.auth.model.entity.Course;

import java.util.List;

public interface CourseService extends IService<Course> {
    
    Page<Course> getCoursePage(int current, int size, String courseName, String teacherName, String category, String status);
    
    boolean saveCourse(Course course);
    
    boolean updateCourse(Course course);
    
    boolean deleteCourse(Long id);
    
    List<Course> getCoursesByTeacherId(Long teacherId);
    
    List<Course> getActiveCourses();
} 