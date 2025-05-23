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
    
    /**
     * 获取当前活动状态的考勤ID
     * @return 活动考勤ID，如果没有活动考勤则返回null
     */
    Long getActiveAttendanceId();
    
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

    /**
     * 保存特殊考勤记录（无活动考勤时的记录）
     * @param recordDTO 考勤记录DTO
     * @return 是否保存成功
     */
    boolean saveSpecialAttendanceRecord(AttendanceRecordDTO recordDTO);

    /**
     * 获取所有特殊考勤记录（无活动考勤时的系统记录）
     * @param page 页码
     * @param size 每页大小
     * @return 特殊考勤记录列表
     */
    PageResponse<AttendanceRecordDTO> getSpecialAttendanceRecords(int page, int size);

    /**
     * 管理员获取所有打卡记录（包括正常考勤记录和特殊记录）
     * @param page 页码
     * @param size 每页大小
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param username 用户名（可选）
     * @param recordType 记录类型：all-所有记录，normal-正常考勤记录，special-特殊记录
     * @return 打卡记录列表
     */
    PageResponse<AttendanceRecordDTO> getAllAttendanceRecordsForAdmin(int page, int size, String startDate, String endDate, String username, String recordType);
    
    /**
     * 获取打卡记录统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> getAttendanceRecordsStatistics(String startDate, String endDate);
} 