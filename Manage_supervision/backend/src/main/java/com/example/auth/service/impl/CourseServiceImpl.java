package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.CourseMapper;
import com.example.auth.model.entity.Course;
import com.example.auth.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    
    @Override
    public Page<Course> getCoursePage(int current, int size, String courseName, String teacherName, String category, String status) {
        Page<Course> page = new Page<>(current, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(courseName != null && !courseName.trim().isEmpty(), Course::getCourseName, courseName)
               .like(teacherName != null && !teacherName.trim().isEmpty(), Course::getTeacherName, teacherName)
               .eq(category != null && !category.trim().isEmpty(), Course::getCourseCategory, category)
               .eq(status != null && !status.trim().isEmpty(), Course::getStatus, status)
               .orderByDesc(Course::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    public boolean saveCourse(Course course) {
        if (course.getStatus() == null) {
            course.setStatus("active");
        }
        return this.save(course);
    }
    
    @Override
    public boolean updateCourse(Course course) {
        return this.updateById(course);
    }
    
    @Override
    public boolean deleteCourse(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setStatus("inactive");
        return this.updateById(course);
    }
    
    @Override
    public List<Course> getCoursesByTeacherId(Long teacherId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, teacherId)
               .eq(Course::getStatus, "active")
               .orderByDesc(Course::getCreateTime);
        return this.list(wrapper);
    }
    
    @Override
    public List<Course> getActiveCourses() {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, "active")
               .orderByDesc(Course::getCreateTime);
        return this.list(wrapper);
    }
} 