package com.example.auth.service;

import com.example.auth.model.dto.AttendanceDTO;
import com.example.auth.model.dto.AttendanceRecordDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.Attendance;
import com.example.auth.model.entity.AttendanceRecord;

import java.util.List;
import java.util.Map;

public interface AttendanceService {

    // 管理员操作
    AttendanceDTO createAttendance(AttendanceDTO attendanceDTO);
    AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO);
    void deleteAttendance(Long id);
    AttendanceDTO getAttendanceById(Long id);
    PageResponse<AttendanceDTO> getAttendanceList(int page, int size, String status);
    
    // 用户签到操作
    /**
     * 普通签到 - 已弃用，系统要求使用人脸签到
     * @deprecated 使用 {@link #faceCheckIn(Long, Long, AttendanceRecordDTO, String)} 代替
     */
    @Deprecated
    AttendanceRecordDTO checkIn(Long attendanceId, Long userId, AttendanceRecordDTO recordDTO);
    
    /**
     * 人脸识别签到 - 系统强制使用此方法进行签到
     */
    AttendanceRecordDTO faceCheckIn(Long attendanceId, Long userId, AttendanceRecordDTO recordDTO, String faceData);
    
    // 获取考勤记录和统计
    PageResponse<AttendanceRecordDTO> getAttendanceRecords(Long attendanceId, int page, int size);
    List<AttendanceRecordDTO> getUncheckedUsers(Long attendanceId);
    Map<String, Object> getAttendanceStatistics(Long attendanceId);
    
    // 学生查看自己的考勤
    List<AttendanceDTO> getCurrentUserAttendances(Long userId);
    List<AttendanceRecordDTO> getCurrentUserAttendanceRecords(Long userId);
    
    // 考勤导出相关
    List<AttendanceRecordDTO> getAllAttendanceRecords(Long attendanceId);
    byte[] exportAttendanceRecords(Long attendanceId) throws Exception;
    Map<String, Object> getAttendanceSummary(String startDate, String endDate);
} 