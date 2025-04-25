package com.example.auth.service;

import com.example.auth.dto.AttendanceDTO;
import com.example.auth.entity.Attendance;

import java.util.List;
import java.util.Map;

public interface AttendanceService {

    /**
     * 学生提交人脸识别考勤
     * @param studentId 学生ID
     * @param classId 班级ID
     * @param faceRecognized 是否成功识别人脸
     * @param recognitionDetails 识别详情
     * @return 考勤记录
     */
    Attendance submitAttendance(Long studentId, Long classId, boolean faceRecognized, String recognitionDetails);
    
    /**
     * 获取学生的考勤记录
     * @param studentId 学生ID
     * @return 考勤记录列表
     */
    List<AttendanceDTO> getStudentAttendanceRecords(Long studentId);
    
    /**
     * 检查学生今天是否已经考勤
     * @param studentId 学生ID
     * @return 是否已考勤
     */
    boolean isStudentAttendedToday(Long studentId);
    
    /**
     * 获取班级的考勤记录
     * @param classId 班级ID
     * @return 考勤记录列表
     */
    List<AttendanceDTO> getClassAttendanceRecords(Long classId);
    
    /**
     * 按日期范围获取班级的考勤记录
     * @param classId 班级ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 考勤记录列表
     */
    List<AttendanceDTO> getClassAttendanceRecordsByDateRange(Long classId, String startDate, String endDate);
    
    /**
     * 获取班级考勤统计信息
     * @param classId 班级ID
     * @return 统计信息
     */
    Map<String, Object> getClassAttendanceStatistics(Long classId);
    
    /**
     * 获取学生考勤统计信息
     * @param studentId 学生ID
     * @return 统计信息
     */
    Map<String, Object> getStudentAttendanceStatistics(Long studentId);
} 