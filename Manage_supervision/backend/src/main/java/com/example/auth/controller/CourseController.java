package com.example.auth.controller;

import com.example.auth.annotation.RequirePermission;
import com.example.auth.model.dto.CourseDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;
    
    /**
     * 分页获取课程列表
     */
    @GetMapping
    public ResponseEntity<PageResponse<CourseDTO>> getCourseList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        
        PageResponse<CourseDTO> response = courseService.getCourseList(page, size, courseName, teacherId, category, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO courseDTO = courseService.getCourseById(id);
        if (courseDTO != null) {
            return ResponseEntity.ok(courseDTO);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 创建新课程（仅管理员和教师可操作）
     */
    @PostMapping
    @RequirePermission({"course:add"})
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.ok(createdCourse);
    }
    
    /**
     * 更新课程信息（仅管理员和课程对应教师可操作）
     */
    @PutMapping("/{id}")
    @RequirePermission({"course:edit"})
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
        if (updatedCourse != null) {
            return ResponseEntity.ok(updatedCourse);
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 删除课程（仅管理员可操作）
     */
    @DeleteMapping("/{id}")
    @RequirePermission({"course:delete"})
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 根据教师ID获取课程列表
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByTeacherId(@PathVariable Long teacherId) {
        List<CourseDTO> courses = courseService.getCoursesByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * 根据类别获取课程列表
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(@PathVariable String category) {
        List<CourseDTO> courses = courseService.getCoursesByCategory(category);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * 获取课程总数
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getCourseCount() {
        Long count = courseService.countCourses();
        return ResponseEntity.ok(count);
    }
    
    /**
     * 获取教师课程数量
     */
    @GetMapping("/teacher/{teacherId}/count")
    public ResponseEntity<Long> getCourseCountByTeacherId(@PathVariable Long teacherId) {
        Long count = courseService.countCoursesByTeacherId(teacherId);
        return ResponseEntity.ok(count);
    }
} 