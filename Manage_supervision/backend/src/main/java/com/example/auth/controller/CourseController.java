package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.Course;
import com.example.auth.model.entity.CourseCategory;
import com.example.auth.model.entity.User;
import com.example.auth.service.CourseCategoryService;
import com.example.auth.service.CourseService;
import com.example.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private CourseCategoryService courseCategoryService;
    
    @Autowired
    private UserService userService;
    
    // ==================== 课程管理 ====================
    
    @GetMapping("/courses")
    public ResponseEntity<?> getCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        try {
            Page<Course> coursePage = courseService.getCoursePage(page, size, courseName, teacherName, category, status);
            PageResponse<Course> response = new PageResponse<>(
                coursePage.getRecords(), coursePage.getTotal(), page, size
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取课程列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/courses")
    @RequireRole("ADMIN")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        try {
            boolean success = courseService.saveCourse(course);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "课程创建成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "课程创建失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程创建失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/courses/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        try {
            course.setId(id);
            boolean success = courseService.updateCourse(course);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "课程更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "课程更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/courses/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            boolean success = courseService.deleteCourse(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "课程删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "课程删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/courses/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            Course course = courseService.getById(id);
            if (course != null) {
                return ResponseEntity.ok(course);
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "课程不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取课程详情失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/courses/teacher/{teacherId}")
    public ResponseEntity<?> getCoursesByTeacher(@PathVariable Long teacherId) {
        try {
            List<Course> courses = courseService.getCoursesByTeacherId(teacherId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取教师课程列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/courses/active")
    public ResponseEntity<?> getActiveCourses() {
        try {
            List<Course> courses = courseService.getActiveCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取活跃课程列表失败: " + e.getMessage()));
        }
    }
    
    // ==================== 课程类别管理 ====================
    
    @GetMapping("/courses/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<String> categories = courseCategoryService.getAllCategoryNames();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取课程类别失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/courses/categories/list")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getCategoryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String status) {
        try {
            Page<CourseCategory> categoryPage = courseCategoryService.getCategoryPage(page, size, categoryName, status);
            PageResponse<CourseCategory> response = new PageResponse<>(
                categoryPage.getRecords(), categoryPage.getTotal(), page, size
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取课程类别列表失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/courses/categories")
    @RequireRole("ADMIN")
    public ResponseEntity<?> createCategory(@RequestBody CourseCategory category) {
        try {
            boolean success = courseCategoryService.saveCategory(category);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "课程类别创建成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "课程类别创建失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程类别创建失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/courses/categories/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CourseCategory category) {
        try {
            category.setId(id);
            boolean success = courseCategoryService.updateCategory(category);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "课程类别更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "课程类别更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程类别更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/courses/categories/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            boolean success = courseCategoryService.deleteCategory(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "课程类别删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "课程类别删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程类别删除失败: " + e.getMessage()));
        }
    }
    
    // ==================== 教师管理 ====================
    
    @GetMapping("/courses/teachers")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getAllTeachers() {
        try {
            List<User> teachers = userService.getTeacherList();
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取教师列表失败: " + e.getMessage()));
        }
    }
}