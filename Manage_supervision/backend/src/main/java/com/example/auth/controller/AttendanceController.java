package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.AttendanceDTO;
import com.example.auth.model.dto.AttendanceRecordDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.User;
import com.example.auth.service.AttendanceService;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.service.UserService;
import com.example.auth.util.FaceRecognitionUtil;
import com.example.auth.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private UserContext userContext;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;
    
    // ========== 管理员接口 ==========
    
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<AttendanceDTO> createAttendance(@RequestBody AttendanceDTO attendanceDTO) {
        // 设置创建者ID
        attendanceDTO.setCreatorId(userContext.getCurrentUser().getId());
        AttendanceDTO result = attendanceService.createAttendance(attendanceDTO);
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<AttendanceDTO> updateAttendance(
            @PathVariable Long id,
            @RequestBody AttendanceDTO attendanceDTO) {
        AttendanceDTO result = attendanceService.updateAttendance(id, attendanceDTO);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/admin/list")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<AttendanceDTO>> getAttendanceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        PageResponse<AttendanceDTO> result = attendanceService.getAttendanceList(page, size, status);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDTO> getAttendanceById(@PathVariable Long id) {
        AttendanceDTO result = attendanceService.getAttendanceById(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/records")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<AttendanceRecordDTO>> getAttendanceRecords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<AttendanceRecordDTO> result = attendanceService.getAttendanceRecords(id, page, size);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/unchecked")
    @RequireRole("ADMIN")
    public ResponseEntity<List<AttendanceRecordDTO>> getUncheckedUsers(@PathVariable Long id) {
        List<AttendanceRecordDTO> result = attendanceService.getUncheckedUsers(id);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/statistics")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(@PathVariable Long id) {
        Map<String, Object> result = attendanceService.getAttendanceStatistics(id);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}/export")
    @RequireRole("ADMIN")
    public ResponseEntity<Resource> exportAttendanceRecords(@PathVariable Long id) {
        try {
            AttendanceDTO attendance = attendanceService.getAttendanceById(id);
            if (attendance == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] excelBytes = attendanceService.exportAttendanceRecords(id);
            ByteArrayResource resource = new ByteArrayResource(excelBytes);
            
            String filename = URLEncoder.encode(attendance.getTitle() + "-考勤记录.xlsx", StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelBytes.length)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/admin/summary")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getAttendanceSummary(
            @RequestParam(required = false) String startDate, 
            @RequestParam(required = false) String endDate) {
        
        // 如果没有提供日期范围，默认查询最近30天的数据
        if (startDate == null || endDate == null) {
            LocalDateTime endDateTime = LocalDateTime.now();
            LocalDateTime startDateTime = endDateTime.minusDays(30);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            startDate = startDateTime.format(formatter);
            endDate = endDateTime.format(formatter);
        }
        
        Map<String, Object> result = attendanceService.getAttendanceSummary(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/special-records")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<AttendanceRecordDTO>> getSpecialAttendanceRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<AttendanceRecordDTO> result = attendanceService.getSpecialAttendanceRecords(page, size);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 管理员接口：获取所有打卡记录（包括正常考勤记录和特殊记录）
     */
    @GetMapping("/admin/all-records")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<AttendanceRecordDTO>> getAllAttendanceRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String recordType) {
        PageResponse<AttendanceRecordDTO> result = attendanceService.getAllAttendanceRecordsForAdmin(page, size, startDate, endDate, username, recordType);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 管理员接口：获取打卡记录统计数据
     */
    @GetMapping("/admin/records-statistics")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getAttendanceRecordsStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = attendanceService.getAttendanceRecordsStatistics(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 管理员接口：获取所有打卡记录统计数据（包括系统记录）- 用于考勤记录管理页面
     */
    @GetMapping("/admin/all-records-statistics")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> getAllRecordsStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = attendanceService.getAllRecordsStatistics(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 管理员接口：获取每日打卡统计
     */
    @GetMapping("/admin/daily-statistics")
    @RequireRole("ADMIN")
    public ResponseEntity<List<Map<String, Object>>> getDailyCheckInStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = attendanceService.getDailyCheckInStatistics(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 管理员接口：获取打卡时间分布
     */
    @GetMapping("/admin/time-distribution")
    @RequireRole("ADMIN")
    public ResponseEntity<List<Map<String, Object>>> getCheckInTimeDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = attendanceService.getCheckInTimeDistribution(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 管理员接口：获取所有记录的每日打卡统计（包括系统记录）- 用于考勤汇总页面
     */
    @GetMapping("/admin/all-daily-statistics")
    @RequireRole("ADMIN")
    public ResponseEntity<List<Map<String, Object>>> getAllDailyCheckInStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = attendanceService.getAllDailyCheckInStatistics(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 管理员接口：获取所有记录的打卡时间分布（包括系统记录）- 用于考勤汇总页面
     */
    @GetMapping("/admin/all-time-distribution")
    @RequireRole("ADMIN")
    public ResponseEntity<List<Map<String, Object>>> getAllCheckInTimeDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = attendanceService.getAllCheckInTimeDistribution(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    
    // ========== 用户接口 ==========
    
    /**
     * 普通签到 (已禁用，改为强制要求人脸验证签到)
     */
    @PostMapping("/{id}/check-in")
    public ResponseEntity<AttendanceRecordDTO> checkIn(
            @PathVariable("id") Long id,
            @RequestBody(required = false) AttendanceRecordDTO recordDTO,
            @RequestHeader("Authorization") String auth) {
        
        // 禁用普通签到，只允许人脸验证签到
        return ResponseEntity.badRequest().body(new AttendanceRecordDTO(
            "error", 
            "普通签到已禁用，请使用人脸验证签到"
        ));
    }
    
    @PostMapping("/{id}/face-check-in")
    public ResponseEntity<AttendanceRecordDTO> faceCheckIn(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Long userId = userContext.getCurrentUser().getId();
        
        // 从请求体中获取签到记录和人脸数据
        AttendanceRecordDTO recordDTO = new AttendanceRecordDTO();
        
        if (requestBody.containsKey("location")) {
            recordDTO.setLocation((String) requestBody.get("location"));
        }
        
        if (requestBody.containsKey("notes")) {
            recordDTO.setNotes((String) requestBody.get("notes"));
        }
        
        if (!requestBody.containsKey("faceData")) {
            return ResponseEntity.badRequest().body(new AttendanceRecordDTO("error", "未提供人脸数据"));
        }
        
        String faceData = (String) requestBody.get("faceData");
        
        AttendanceRecordDTO result = attendanceService.faceCheckIn(id, userId, recordDTO, faceData);
        
        // 检查结果是否包含错误信息
        if (result.getError() != null) {
            // 有错误，返回400状态码但包含详细错误信息
            return ResponseEntity.status(400).body(result);
        }
        
        // 签到成功，返回200状态码
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/user/list")
    public ResponseEntity<List<AttendanceDTO>> getCurrentUserAttendances() {
        Long userId = userContext.getCurrentUser().getId();
        List<AttendanceDTO> result = attendanceService.getCurrentUserAttendances(userId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/user/records")
    public ResponseEntity<List<AttendanceRecordDTO>> getCurrentUserAttendanceRecords() {
        Long userId = userContext.getCurrentUser().getId();
        List<AttendanceRecordDTO> result = attendanceService.getCurrentUserAttendanceRecords(userId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 公开接口：人脸考勤（无需登录）
     * 直接通过人脸识别进行考勤，即使没有活动考勤也能工作
     */
    @PostMapping("/face-checkin")
    public ResponseEntity<Map<String, Object>> publicFaceCheckIn(@RequestBody Map<String, Object> requestBody) {
        logger.info("收到公共人脸考勤请求");
        
        Map<String, Object> response = new HashMap<>();
        
        if (!requestBody.containsKey("faceData")) {
            response.put("success", false);
            response.put("message", "未提供人脸数据");
            return ResponseEntity.badRequest().body(response);
        }
        
        String faceData = (String) requestBody.get("faceData");
        
        try {
            // 使用多次验证进行人脸识别（5次验证确保最高准确度）
            Map<String, Object> matchResult = faceRecognitionService.findMatchingUserWithMultipleVerification(faceData, 5);
            
            if (!(Boolean) matchResult.get("success")) {
                response.put("success", false);
                response.put("message", matchResult.get("message"));
                response.put("verificationDetails", matchResult.get("allVerificationDetails"));
                return ResponseEntity.ok(response);
            }
            
            User matchedUser = (User) matchResult.get("user");
            Map<String, Object> verificationResult = (Map<String, Object>) matchResult.get("verificationResult");
            
            if (matchedUser == null) {
                response.put("success", false);
                response.put("message", "未找到匹配的用户");
                return ResponseEntity.ok(response);
            }
            
            logger.info("多次验证识别成功，用户: {}，平均相似度: {}", 
                       matchedUser.getUsername(), verificationResult.get("averageSimilarity"));
            
            // 检查是否有重复打卡（10分钟内）
            boolean hasRecentCheckIn = attendanceService.hasRecentCheckIn(matchedUser.getId(), 10);
            if (hasRecentCheckIn) {
                response.put("success", false);
                response.put("message", "您在10分钟内已经打卡过了，请勿重复打卡");
                return ResponseEntity.ok(response);
            }
            
            // 获取当前活跃的考勤ID
            Long activeAttendanceId = attendanceService.getActiveAttendanceId();
            
            AttendanceRecordDTO result;
            
            if (activeAttendanceId != null) {
                // 有活跃考勤，进行正常考勤打卡
                AttendanceRecordDTO recordDTO = new AttendanceRecordDTO();
                recordDTO.setUserId(matchedUser.getId());
                recordDTO.setUsername(matchedUser.getUsername());
                recordDTO.setUserNumber(matchedUser.getUserNumber());
                recordDTO.setFaceVerified(true);
                
                result = attendanceService.faceCheckIn(
                    activeAttendanceId,
                    matchedUser.getId(),
                    recordDTO,
                    faceData
                );
                
                logger.info("用户 {} 考勤打卡成功", matchedUser.getUsername());
            } else {
                // 没有活跃考勤，创建特殊记录
                AttendanceRecordDTO specialRecord = new AttendanceRecordDTO();
                specialRecord.setAttendanceId(-1L); // 特殊记录用-1表示无关联考勤
                specialRecord.setUserId(matchedUser.getId());
                specialRecord.setUsername(matchedUser.getUsername());
                specialRecord.setUserNumber(matchedUser.getUserNumber());
                specialRecord.setCheckInTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                specialRecord.setFaceVerified(true);
                specialRecord.setLocation("系统自动记录"); // 添加位置信息
                specialRecord.setNotes("非考勤时间人脸识别记录"); // 添加备注
                
                // 保存特殊记录到数据库
                boolean saveSuccess = attendanceService.saveSpecialAttendanceRecord(specialRecord);
                if (saveSuccess) {
                    result = specialRecord;
                    logger.info("用户 {} 在非考勤时间进行了人脸识别记录，已保存到数据库", matchedUser.getUsername());
                } else {
                    logger.error("保存用户 {} 的特殊考勤记录失败", matchedUser.getUsername());
                    response.put("success", false);
                    response.put("message", "保存考勤记录失败，请稍后重试");
                    return ResponseEntity.ok(response);
                }
            }
            
            // 构建成功响应
            response.put("success", true);
            response.put("message", "人脸识别考勤成功");
            response.put("userName", matchedUser.getUsername());
            response.put("userNumber", matchedUser.getUserNumber());
            response.put("userId", matchedUser.getId());
            response.put("entryTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            response.put("status", "success");
            
            // 添加多次验证的详细信息
            response.put("verificationDetails", verificationResult);
            response.put("averageSimilarity", Math.round((Double) verificationResult.get("averageStructuralSimilarity") * 100));
            response.put("successRate", Math.round((Double) verificationResult.get("successRate") * 100));
            response.put("totalAttempts", verificationResult.get("totalAttempts"));
            response.put("successCount", verificationResult.get("successCount"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("公共人脸考勤失败", e);
            response.put("success", false);
            response.put("message", "人脸识别失败，请稍后重试: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
} 