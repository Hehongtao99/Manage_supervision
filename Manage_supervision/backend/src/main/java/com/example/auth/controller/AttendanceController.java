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
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!requestBody.containsKey("faceData")) {
                response.put("success", false);
                response.put("message", "未提供人脸数据，请允许摄像头访问并正对摄像头");
                return ResponseEntity.ok(response);
            }
            
            String faceData = (String) requestBody.get("faceData");
            
            // 直接使用人脸识别服务的findMatchingUser方法查找匹配的用户
            Map<String, Object> matchResult = faceRecognitionService.findMatchingUser(faceData);
            
            if (!(boolean)matchResult.get("success")) {
                // 未找到匹配的用户
                response.put("success", false);
                response.put("message", matchResult.get("message"));
                return ResponseEntity.ok(response);
            }
            
            // 获取匹配的用户和相似度
            User matchedUser = (User)matchResult.get("user");
            double similarity = (double)matchResult.get("similarity");
            
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            
            // 获取当前活动考勤ID
            Long activeAttendanceId = attendanceService.getActiveAttendanceId();
            
            // 构建成功响应
            response.put("success", true);
            response.put("userName", matchedUser.getRealName() != null ? matchedUser.getRealName() : matchedUser.getUsername());
            response.put("userNumber", matchedUser.getUserNumber());
            response.put("similarity", (int)(similarity * 100)); // 转换为百分比
            response.put("entryTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            response.put("status", "success");
            
            // 创建考勤记录DTO
            AttendanceRecordDTO recordDTO = new AttendanceRecordDTO();
            recordDTO.setLocation("人脸签到系统");
            recordDTO.setNotes("通过公共人脸签到接口");
            
            // 如果存在活动考勤，则记录签到
            if (activeAttendanceId != null) {
                try {
                    // 检查用户是否已经在当前考勤中签到过
                    List<AttendanceRecordDTO> userRecords = attendanceService.getCurrentUserAttendanceRecords(matchedUser.getId());
                    boolean alreadyCheckedIn = userRecords.stream()
                        .anyMatch(record -> record.getAttendanceId().equals(activeAttendanceId));
                    
                    if (alreadyCheckedIn) {
                        logger.info("用户{}已在当前考勤{}中签到过", matchedUser.getId(), activeAttendanceId);
                        response.put("attendanceRecorded", false);
                        response.put("attendanceMessage", "您已在当前考勤中签到过");
                    } else {
                        // 使用真实数据进行签到
                        AttendanceRecordDTO result = attendanceService.faceCheckIn(
                            activeAttendanceId, 
                            matchedUser.getId(), 
                            recordDTO, 
                            faceData
                        );
                        
                        // 检查结果是否有错误
                        if (result.getError() != null) {
                            logger.warn("考勤记录失败: {}", result.getError());
                            response.put("attendanceRecorded", false);
                            response.put("attendanceMessage", result.getError());
                        } else {
                            // 考勤记录成功
                            logger.info("用户{}成功完成考勤签到，考勤ID: {}", matchedUser.getId(), activeAttendanceId);
                            response.put("attendanceRecorded", true);
                            response.put("attendanceId", activeAttendanceId);
                            response.put("attendanceMessage", "签到成功");
                        }
                    }
                } catch (Exception e) {
                    // 记录签到出现异常，但不影响主响应
                    logger.warn("考勤记录过程中发生异常: {}", e.getMessage());
                    response.put("attendanceRecorded", false);
                    response.put("attendanceMessage", "考勤记录失败: " + e.getMessage());
                }
            } else {
                // 无活动考勤，检查今天是否已创建过特殊记录，避免重复
                try {
                    // 检查今天是否已有特殊记录
                    String todayStart = now.toLocalDate().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String todayEnd = now.toLocalDate().atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    
                    List<AttendanceRecordDTO> todaySpecialRecords = attendanceService.getCurrentUserAttendanceRecords(matchedUser.getId())
                        .stream()
                        .filter(record -> record.getAttendanceId() == -1L && 
                                record.getCheckInTime() != null &&
                                record.getCheckInTime().compareTo(todayStart) >= 0 &&
                                record.getCheckInTime().compareTo(todayEnd) <= 0)
                        .collect(Collectors.toList());
                    
                    if (!todaySpecialRecords.isEmpty()) {
                        logger.info("用户{}今天已有特殊考勤记录，跳过创建", matchedUser.getId());
                        response.put("attendanceRecorded", false);
                        response.put("attendanceMessage", "当前没有活动考勤，但您今天已有进出记录");
                        response.put("specialRecordExists", true);
                    } else {
                        // 创建一条特殊考勤记录
                        AttendanceRecordDTO specialRecord = new AttendanceRecordDTO();
                        specialRecord.setUserId(matchedUser.getId());
                        specialRecord.setCheckInTime(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                        specialRecord.setStatus("系统记录");
                        specialRecord.setLocation("人脸签到系统");
                        specialRecord.setNotes("无活动考勤，系统自动记录");
                        specialRecord.setFaceVerified(true);
                        
                        // 调用服务保存特殊记录
                        boolean saved = attendanceService.saveSpecialAttendanceRecord(specialRecord);
                        
                        if (saved) {
                            logger.info("为用户{}创建了无活动考勤的人脸识别记录", matchedUser.getId());
                            response.put("specialRecordCreated", true);
                            response.put("attendanceRecorded", false);
                            response.put("attendanceMessage", "当前没有活动考勤，但系统已记录此次人脸识别");
                        } else {
                            logger.warn("创建特殊考勤记录失败");
                            response.put("specialRecordCreated", false);
                            response.put("attendanceRecorded", false);
                            response.put("attendanceMessage", "当前没有活动考勤，记录保存失败");
                        }
                    }
                } catch (Exception e) {
                    logger.error("创建特殊考勤记录时发生异常", e);
                    response.put("specialRecordCreated", false);
                    response.put("attendanceRecorded", false);
                    response.put("attendanceMessage", "当前没有活动考勤，系统记录失败");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("公共人脸签到失败", e);
            response.put("success", false);
            response.put("message", "服务器处理请求时发生错误: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
} 