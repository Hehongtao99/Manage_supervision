package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.CourseMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.CourseDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.Course;
import com.example.auth.model.entity.User;
import com.example.auth.service.CourseService;
import com.example.auth.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CourseCategoryService courseCategoryService;
    
    @Override
    public PageResponse<CourseDTO> getCourseList(int page, int size, String courseName, Long teacherId, String category, String status) {
        Page<Course> pageParam = new Page<>(page, size);
        
        // 使用条件查询
        Page<Course> coursePage;
        if (courseName != null || teacherId != null || category != null || status != null) {
            coursePage = (Page<Course>) courseMapper.findByConditions(pageParam, courseName, teacherId, category, status);
        } else {
            // 没有条件时，使用LambdaQueryWrapper查询所有非删除状态的课程
            LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ne(Course::getStatus, "deleted");
            queryWrapper.orderByDesc(Course::getCreateTime);
            coursePage = courseMapper.selectPage(pageParam, queryWrapper);
        }
        
        // 转换为DTO
        List<CourseDTO> courseDTOs = coursePage.getRecords().stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(courseDTOs, coursePage.getTotal(), (int)coursePage.getCurrent(), (int)coursePage.getSize());
    }
    
    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course != null && !"deleted".equals(course.getStatus())) {
            // 加载教师信息
            User teacher = userMapper.selectById(course.getTeacherId());
            course.setTeacher(teacher);
            return convertToCourseDTO(course);
        }
        return null;
    }
    
    @Override
    @Transactional
    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setTeacherId(courseDTO.getTeacherId());
        course.setDuration(courseDTO.getDuration());
        course.setCategory(courseDTO.getCategory());
        course.setCoverImage(courseDTO.getCoverImage());
        course.setStatus("active");
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        
        courseMapper.insert(course);
        
        // 设置教师信息
        if (course.getTeacherId() != null) {
            User teacher = userMapper.selectById(course.getTeacherId());
            course.setTeacher(teacher);
        }
        
        return convertToCourseDTO(course);
    }
    
    @Override
    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course existingCourse = courseMapper.selectById(id);
        if (existingCourse == null || "deleted".equals(existingCourse.getStatus())) {
            return null;
        }
        
        // 更新非空字段
        if (courseDTO.getCourseName() != null) {
            existingCourse.setCourseName(courseDTO.getCourseName());
        }
        
        if (courseDTO.getDescription() != null) {
            existingCourse.setDescription(courseDTO.getDescription());
        }
        
        if (courseDTO.getTeacherId() != null) {
            existingCourse.setTeacherId(courseDTO.getTeacherId());
        }
        
        if (courseDTO.getDuration() != null) {
            existingCourse.setDuration(courseDTO.getDuration());
        }
        
        if (courseDTO.getCategory() != null) {
            existingCourse.setCategory(courseDTO.getCategory());
        }
        
        if (courseDTO.getCoverImage() != null) {
            existingCourse.setCoverImage(courseDTO.getCoverImage());
        }
        
        if (courseDTO.getStatus() != null) {
            existingCourse.setStatus(courseDTO.getStatus());
        }
        
        existingCourse.setUpdateTime(LocalDateTime.now());
        
        courseMapper.updateById(existingCourse);
        
        // 设置教师信息
        if (existingCourse.getTeacherId() != null) {
            User teacher = userMapper.selectById(existingCourse.getTeacherId());
            existingCourse.setTeacher(teacher);
        }
        
        return convertToCourseDTO(existingCourse);
    }
    
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course != null) {
            // 逻辑删除，将状态设置为deleted
            course.setStatus("deleted");
            course.setUpdateTime(LocalDateTime.now());
            courseMapper.updateById(course);
        }
    }
    
    @Override
    public List<CourseDTO> getCoursesByTeacherId(Long teacherId) {
        List<Course> courses = courseMapper.findByTeacherId(teacherId);
        return courses.stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CourseDTO> getCoursesByCategory(String category) {
        List<Course> courses = courseMapper.findByCategory(category);
        return courses.stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long countCourses() {
        return courseMapper.countActiveCourses();
    }
    
    @Override
    public Long countCoursesByTeacherId(Long teacherId) {
        return courseMapper.countCoursesByTeacherId(teacherId);
    }
    
    /**
     * 将Course实体转换为CourseDTO
     */
    private CourseDTO convertToCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setTeacherId(course.getTeacherId());
        dto.setDuration(course.getDuration());
        dto.setCategory(course.getCategory());
        dto.setCoverImage(course.getCoverImage());
        dto.setStatus(course.getStatus());
        dto.setCreateTime(course.getCreateTime());
        dto.setUpdateTime(course.getUpdateTime());
        
        // 设置教师名称
        if (course.getTeacher() != null) {
            dto.setTeacherName(course.getTeacher().getRealName() != null ? 
                               course.getTeacher().getRealName() : 
                               course.getTeacher().getUsername());
        } else if (course.getTeacherId() != null) {
            User teacher = userMapper.selectById(course.getTeacherId());
            if (teacher != null) {
                dto.setTeacherName(teacher.getRealName() != null ? 
                                  teacher.getRealName() : 
                                  teacher.getUsername());
            }
        }
        
        return dto;
    }
} 