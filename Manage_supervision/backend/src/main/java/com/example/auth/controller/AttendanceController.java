package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.AttendanceDTO;
import com.example.auth.entity.Attendance;
import com.example.auth.service.AttendanceService;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        token = token.substring(7); // 移除"Bearer "前缀
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("从token中获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 学生提交人脸识别考勤
     */
    @PostMapping("/face-recognition")
    @RequireRole("USER")
    public ResponseEntity<?> submitFaceAttendance(
            @RequestParam("classId") Long classId,
            @RequestParam("base64Image") String base64Image,
            HttpServletRequest request) {
        
        try {
            Long studentId = getUserIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "用户未登录"));
            }
            
            // 检查学生今天是否已经签到
            if (attendanceService.isStudentAttendedToday(studentId)) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "您今天已经完成考勤，无需重复考勤"
                ));
            }
            
            // 进行人脸识别
            boolean recognized = faceRecognitionService.recognizeFace(base64Image);
            
            // 无论识别结果如何，都记录考勤
            String details = recognized ? 
                    "人脸识别成功，考勤完成" : 
                    "人脸识别失败，请重试";
            
            // 保存考勤记录
            Attendance attendance = attendanceService.submitAttendance(
                    studentId, classId, recognized, details);
            
            LocalDateTime checkInTime = attendance.getCheckInTime();
            String formattedTime = checkInTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", recognized);
            result.put("message", recognized ? "考勤成功" : "人脸识别失败，请重试");
            result.put("attendanceId", attendance.getId());
            result.put("checkInTime", formattedTime);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("提交人脸考勤失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "考勤失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学生上传人脸图像考勤
     */
    @PostMapping("/upload-face")
    @RequireRole("USER")
    public ResponseEntity<?> uploadFaceImage(
            @RequestParam("classId") Long classId,
            @RequestParam("faceImage") MultipartFile faceImage,
            HttpServletRequest request) {
        
        try {
            Long studentId = getUserIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "用户未登录"));
            }
            
            // 检查学生今天是否已经签到
            if (attendanceService.isStudentAttendedToday(studentId)) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "您今天已经完成考勤，无需重复考勤"
                ));
            }
            
            // 处理上传的人脸图像
            boolean recognized = faceRecognitionService.processUploadedImage(faceImage);
            
            // 保存考勤记录
            String details = recognized ? 
                    "人脸识别成功，考勤完成" : 
                    "人脸识别失败，请重试";
            
            Attendance attendance = attendanceService.submitAttendance(
                    studentId, classId, recognized, details);
            
            LocalDateTime checkInTime = attendance.getCheckInTime();
            String formattedTime = checkInTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", recognized);
            result.put("message", recognized ? "考勤成功" : "人脸识别失败，请重试");
            result.put("attendanceId", attendance.getId());
            result.put("checkInTime", formattedTime);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("上传人脸图像考勤失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "考勤失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取学生的考勤记录
     */
    @GetMapping("/student-records")
    @RequireRole("USER")
    public ResponseEntity<?> getStudentAttendanceRecords(HttpServletRequest request) {
        try {
            Long studentId = getUserIdFromRequest(request);
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "用户未登录"));
            }
            
            List<AttendanceDTO> records = attendanceService.getStudentAttendanceRecords(studentId);
            Map<String, Object> statistics = attendanceService.getStudentAttendanceStatistics(studentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("records", records);
            result.put("statistics", statistics);
            result.put("attendedToday", attendanceService.isStudentAttendedToday(studentId));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取学生考勤记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "获取考勤记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取班级的考勤记录（教师和督导员用）
     */
    @GetMapping("/class-records/{classId}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getClassAttendanceRecords(
            @PathVariable Long classId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // 根据日期范围查询
            List<AttendanceDTO> records;
            if (startDate != null && endDate != null) {
                // 这里需要在AttendanceService中添加按日期范围查询的方法
                records = attendanceService.getClassAttendanceRecordsByDateRange(classId, startDate, endDate);
            } else {
                records = attendanceService.getClassAttendanceRecords(classId);
            }
            
            // 获取班级考勤统计信息
            Map<String, Object> statistics = attendanceService.getClassAttendanceStatistics(classId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("records", records);
            result.put("statistics", statistics);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取班级考勤记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "获取班级考勤记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取子女考勤记录（家长用）
     */
    @GetMapping("/child-records/{childId}")
    @RequireRole("PARENT")
    public ResponseEntity<?> getChildAttendanceRecords(@PathVariable Long childId, HttpServletRequest request) {
        try {
            Long parentId = getUserIdFromRequest(request);
            if (parentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "用户未登录"));
            }
            
            // 验证该学生是否为当前家长的子女
            // TODO: 需要在Service中添加验证逻辑
            
            List<AttendanceDTO> records = attendanceService.getStudentAttendanceRecords(childId);
            Map<String, Object> statistics = attendanceService.getStudentAttendanceStatistics(childId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("records", records);
            result.put("statistics", statistics);
            result.put("attendedToday", attendanceService.isStudentAttendedToday(childId));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("获取子女考勤记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "获取子女考勤记录失败: " + e.getMessage()));
        }
    }
} 