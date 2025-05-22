package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.AttendanceDTO;
import com.example.auth.model.dto.AttendanceRecordDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.service.AttendanceService;
import com.example.auth.util.UserContext;
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

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private UserContext userContext;
    
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
} 