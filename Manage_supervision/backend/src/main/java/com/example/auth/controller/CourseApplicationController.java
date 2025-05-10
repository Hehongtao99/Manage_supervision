package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.CourseApplicationDTO;
import com.example.auth.service.CourseApplicationService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程申请控制器
 */
@RestController
@RequestMapping("/api")
public class CourseApplicationController {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseApplicationController.class);
    
    @Autowired
    private CourseApplicationService courseApplicationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 教师提交课程申请
     */
    @PostMapping("/supervisor/course-applications")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> createApplication(
            @RequestHeader("Authorization") String auth,
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam("hourlyPrice") String hourlyPrice,
            @RequestParam("description") String description,
            @RequestParam("workTimeStart") Integer workTimeStart,
            @RequestParam("workTimeEnd") Integer workTimeEnd,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师提交课程申请, 教师ID: {}, 标题: {}, 科目: {}", teacherId, title, subject);
            
            List<MultipartFile> imageList = images != null ? 
                    Arrays.stream(images).filter(img -> img != null && !img.isEmpty()).collect(Collectors.toList()) : 
                    new ArrayList<>();
            
            Long applicationId = courseApplicationService.createApplication(
                    teacherId, title, subject, hourlyPrice, description, 
                    workTimeStart, workTimeEnd, imageList);
            
            if (applicationId != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "课程申请提交成功，等待管理员审核");
                response.put("applicationId", applicationId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "课程申请提交失败，请检查输入信息"
                ));
            }
        } catch (Exception e) {
            logger.error("提交课程申请出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师重新提交被拒绝的课程申请
     */
    @PutMapping("/supervisor/course-applications/{id}/resubmit")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> resubmitApplication(
            @PathVariable("id") Long applicationId,
            @RequestHeader("Authorization") String auth,
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam("hourlyPrice") String hourlyPrice,
            @RequestParam("description") String description,
            @RequestParam("workTimeStart") Integer workTimeStart,
            @RequestParam("workTimeEnd") Integer workTimeEnd,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师重新提交课程申请, 申请ID: {}, 教师ID: {}, 标题: {}", applicationId, teacherId, title);
            
            // 获取应用，检查是否是该教师的申请
            CourseApplicationDTO application = courseApplicationService.getApplicationById(applicationId);
            if (application == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "课程申请不存在"
                ));
            }
            
            if (!application.getTeacherId().equals(teacherId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "无权修改此课程申请"
                ));
            }
            
            if (!"REJECTED".equals(application.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "只能重新提交被拒绝的申请"
                ));
            }
            
            List<MultipartFile> imageList = images != null ? 
                    Arrays.stream(images).filter(img -> img != null && !img.isEmpty()).collect(Collectors.toList()) : 
                    new ArrayList<>();
            
            boolean success = courseApplicationService.resubmitApplication(
                    applicationId, title, subject, hourlyPrice, description,
                    workTimeStart, workTimeEnd, imageList);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "课程申请重新提交成功，等待管理员审核"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "课程申请重新提交失败，请检查输入信息"
                ));
            }
        } catch (Exception e) {
            logger.error("重新提交课程申请出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师获取自己的课程申请列表
     */
    @GetMapping("/supervisor/course-applications")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTeacherApplications(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师获取自己的课程申请列表, 教师ID: {}", teacherId);
            
            List<CourseApplicationDTO> applications = courseApplicationService.getApplicationsByTeacher(teacherId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            logger.error("获取教师课程申请列表出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师获取单个课程申请详情
     */
    @GetMapping("/supervisor/course-applications/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTeacherApplicationDetail(
            @PathVariable("id") Long applicationId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师获取课程申请详情, 申请ID: {}, 教师ID: {}", applicationId, teacherId);
            
            CourseApplicationDTO application = courseApplicationService.getApplicationById(applicationId);
            if (application == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "课程申请不存在"
                ));
            }
            
            if (!application.getTeacherId().equals(teacherId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "无权查看此课程申请"
                ));
            }
            
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            logger.error("获取课程申请详情出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 管理员获取所有课程申请列表
     */
    @GetMapping("/admin/course-applications")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getAllApplications() {
        try {
            logger.info("管理员获取所有课程申请列表");
            List<CourseApplicationDTO> applications = courseApplicationService.getAllApplications();
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            logger.error("获取所有课程申请列表出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 管理员获取单个课程申请详情
     */
    @GetMapping("/admin/course-applications/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getAdminApplicationDetail(@PathVariable("id") Long applicationId) {
        try {
            logger.info("管理员获取课程申请详情, 申请ID: {}", applicationId);
            CourseApplicationDTO application = courseApplicationService.getApplicationById(applicationId);
            
            if (application == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "课程申请不存在"
                ));
            }
            
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            logger.error("获取课程申请详情出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 管理员审核课程申请
     */
    @PostMapping("/admin/course-applications/{id}/review")
    @RequireRole("ADMIN")
    public ResponseEntity<?> reviewApplication(
            @PathVariable("id") Long applicationId,
            @RequestParam("approved") boolean approved,
            @RequestParam(value = "rejectionReason", required = false) String rejectionReason) {
        
        try {
            logger.info("管理员审核课程申请 - ID: {}, 通过状态: {}", applicationId, approved);
            
            boolean success = courseApplicationService.reviewApplication(applicationId, approved, rejectionReason);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", approved ? "已通过课程申请" : "已拒绝课程申请"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "操作失败，请检查申请是否存在"
                ));
            }
        } catch (Exception e) {
            logger.error("审核课程申请出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
} 