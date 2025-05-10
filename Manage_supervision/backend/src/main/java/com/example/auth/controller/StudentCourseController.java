package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.CourseApplicationDTO;
import com.example.auth.model.dto.PageResult;
import com.example.auth.service.CourseApplicationService;
import com.example.auth.service.ChatService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生课程浏览控制器
 */
@RestController
@RequestMapping("/api/student")
public class StudentCourseController {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentCourseController.class);
    
    @Autowired
    private CourseApplicationService courseApplicationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取所有已审核通过的课程
     * @return 课程列表
     */
    @GetMapping("/courses/approved")
    @RequireRole("USER")
    public ResponseEntity<List<CourseApplicationDTO>> getAllApprovedCourses() {
        logger.info("获取所有已审核通过的课程");
        List<CourseApplicationDTO> approvedCourses = courseApplicationService.getAllApprovedCourseApplications();
        return ResponseEntity.ok(approvedCourses);
    }
    
    /**
     * 获取单个课程详情
     * @param courseId 课程ID
     * @return 课程详情
     */
    @GetMapping("/courses/{courseId}")
    @RequireRole("USER")
    public ResponseEntity<CourseApplicationDTO> getCourseDetail(@PathVariable Long courseId) {
        logger.info("获取课程详情: {}", courseId);
        CourseApplicationDTO courseDetail = courseApplicationService.getCourseApplicationById(courseId);
        return ResponseEntity.ok(courseDetail);
    }

    /**
     * 学生获取已审核通过的课程（分页）
     */
    @GetMapping("/courses/approved/page")
    @RequireRole("USER")
    public ResponseEntity<PageResult<CourseApplicationDTO>> getApprovedCoursesPaged(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {
        logger.info("获取已审核通过的课程列表（分页）: page={}, size={}", page, size);
        PageResult<CourseApplicationDTO> courses = courseApplicationService.getAllApprovedCourseApplicationsPaged(page, size);
        return ResponseEntity.ok(courses);
    }
} 