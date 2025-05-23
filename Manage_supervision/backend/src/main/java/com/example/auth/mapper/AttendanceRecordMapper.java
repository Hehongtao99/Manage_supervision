package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.AttendanceRecord;
import com.example.auth.model.dto.AttendanceRecordDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
    
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "WHERE ar.attendance_id = #{attendanceId} " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getAttendanceRecords(Page<AttendanceRecordDTO> page, @Param("attendanceId") Long attendanceId);
    
    @Select("SELECT u.id, u.username, u.real_name, u.user_number " +
           "FROM users u " +
           "LEFT JOIN attendance_records ar ON u.id = ar.user_id AND ar.attendance_id = #{attendanceId} " +
           "JOIN user_roles ur ON u.id = ur.user_id " +
           "JOIN roles r ON ur.role_id = r.id " +
           "WHERE r.name = 'USER' AND ar.id IS NULL AND u.status = 'active'")
    List<AttendanceRecordDTO> getUncheckedUsers(@Param("attendanceId") Long attendanceId);
    
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "WHERE ar.attendance_id = #{attendanceId} " +
           "ORDER BY ar.check_in_time DESC")
    List<AttendanceRecordDTO> getAllAttendanceRecords(@Param("attendanceId") Long attendanceId);
    
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number, " +
           "CASE WHEN ar.attendance_id = -1 THEN '公共人脸识别记录' ELSE a.title END as attendance_title " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "LEFT JOIN attendance a ON ar.attendance_id = a.id " +
           "WHERE ar.check_in_time BETWEEN #{startDate} AND #{endDate} " +
           "ORDER BY ar.check_in_time DESC")
    List<AttendanceRecordDTO> getAttendanceRecordsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("SELECT ar.*, u.username, u.real_name, u.user_number " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "WHERE ar.attendance_id = -1 " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getSpecialAttendanceRecords(Page<AttendanceRecordDTO> page);
    
    // 简化的查询方法，避免复杂的动态SQL在注解中使用
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number, " +
           "CASE WHEN ar.attendance_id = -1 THEN '公共人脸识别记录' ELSE a.title END as attendance_title " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "LEFT JOIN attendance a ON ar.attendance_id = a.id " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getAllAttendanceRecordsForAdmin(Page<AttendanceRecordDTO> page);
    
    // 根据日期范围查询
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number, " +
           "CASE WHEN ar.attendance_id = -1 THEN '公共人脸识别记录' ELSE a.title END as attendance_title " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "LEFT JOIN attendance a ON ar.attendance_id = a.id " +
           "WHERE ar.check_in_time >= #{startDate} AND ar.check_in_time <= #{endDate} " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getAllAttendanceRecordsForAdminByDateRange(
        Page<AttendanceRecordDTO> page, 
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
    
    // 根据用户名查询
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number, " +
           "CASE WHEN ar.attendance_id = -1 THEN '公共人脸识别记录' ELSE a.title END as attendance_title " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "LEFT JOIN attendance a ON ar.attendance_id = a.id " +
           "WHERE (u.username LIKE CONCAT('%', #{username}, '%') OR u.real_name LIKE CONCAT('%', #{username}, '%') OR u.user_number LIKE CONCAT('%', #{username}, '%')) " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getAllAttendanceRecordsForAdminByUsername(
        Page<AttendanceRecordDTO> page, 
        @Param("username") String username
    );
    
    // 根据记录类型查询
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number, " +
           "CASE WHEN ar.attendance_id = -1 THEN '公共人脸识别记录' ELSE a.title END as attendance_title " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "LEFT JOIN attendance a ON ar.attendance_id = a.id " +
           "WHERE ar.attendance_id > 0 " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getNormalAttendanceRecords(Page<AttendanceRecordDTO> page);
    
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number, " +
           "CASE WHEN ar.attendance_id = -1 THEN '公共人脸识别记录' ELSE a.title END as attendance_title " +
           "FROM attendance_records ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "LEFT JOIN attendance a ON ar.attendance_id = a.id " +
           "WHERE ar.attendance_id = -1 " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getSpecialAttendanceRecordsForAdmin(Page<AttendanceRecordDTO> page);
    
    // 统计查询 - 只统计前台考勤记录
    @Select("SELECT " +
           "COUNT(*) as totalRecords, " +
           "COUNT(DISTINCT ar.user_id) as uniqueUsers, " +
           "COUNT(CASE WHEN ar.face_verified = 1 THEN 1 END) as faceVerifiedRecords " +
           "FROM attendance_records ar " +
           "WHERE ar.attendance_id > 0")
    Map<String, Object> getAttendanceRecordsStatistics();
    
    @Select("SELECT " +
           "COUNT(*) as totalRecords, " +
           "COUNT(DISTINCT ar.user_id) as uniqueUsers, " +
           "COUNT(CASE WHEN ar.face_verified = 1 THEN 1 END) as faceVerifiedRecords " +
           "FROM attendance_records ar " +
           "WHERE ar.attendance_id > 0 AND ar.check_in_time >= #{startDate} AND ar.check_in_time <= #{endDate}")
    Map<String, Object> getAttendanceRecordsStatisticsByDateRange(
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
    
    // 统计所有打卡记录（包括系统记录）- 用于考勤记录管理页面
    @Select("SELECT " +
           "COUNT(*) as totalRecords, " +
           "COUNT(DISTINCT ar.user_id) as uniqueUsers, " +
           "COUNT(CASE WHEN ar.face_verified = 1 THEN 1 END) as faceVerifiedRecords " +
           "FROM attendance_records ar")
    Map<String, Object> getAllRecordsStatistics();
    
    @Select("SELECT " +
           "COUNT(*) as totalRecords, " +
           "COUNT(DISTINCT ar.user_id) as uniqueUsers, " +
           "COUNT(CASE WHEN ar.face_verified = 1 THEN 1 END) as faceVerifiedRecords " +
           "FROM attendance_records ar " +
           "WHERE ar.check_in_time >= #{startDate} AND ar.check_in_time <= #{endDate}")
    Map<String, Object> getAllRecordsStatisticsByDateRange(
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
    
    // 获取每日打卡统计
    @Select("SELECT " +
           "DATE(ar.check_in_time) as date, " +
           "COUNT(*) as count " +
           "FROM attendance_records ar " +
           "WHERE ar.attendance_id > 0 AND ar.check_in_time >= #{startDate} AND ar.check_in_time <= #{endDate} " +
           "GROUP BY DATE(ar.check_in_time) " +
           "ORDER BY date")
    List<Map<String, Object>> getDailyCheckInStatistics(
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
    
    // 获取所有记录的每日打卡统计（包括系统记录）
    @Select("SELECT " +
           "DATE(ar.check_in_time) as date, " +
           "COUNT(*) as count " +
           "FROM attendance_records ar " +
           "WHERE ar.check_in_time >= #{startDate} AND ar.check_in_time <= #{endDate} " +
           "GROUP BY DATE(ar.check_in_time) " +
           "ORDER BY date")
    List<Map<String, Object>> getAllDailyCheckInStatistics(
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
    
    // 获取打卡时间分布（按小时和星期分组）
    @Select("SELECT " +
           "DAYOFWEEK(ar.check_in_time) - 1 as dayOfWeek, " +
           "HOUR(ar.check_in_time) as hour, " +
           "COUNT(*) as count " +
           "FROM attendance_records ar " +
           "WHERE ar.attendance_id > 0 AND ar.check_in_time >= #{startDate} AND ar.check_in_time <= #{endDate} " +
           "GROUP BY DAYOFWEEK(ar.check_in_time) - 1, HOUR(ar.check_in_time) " +
           "ORDER BY dayOfWeek, hour")
    List<Map<String, Object>> getCheckInTimeDistribution(
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
    
    // 获取所有记录的打卡时间分布（包括系统记录）
    @Select("SELECT " +
           "DAYOFWEEK(ar.check_in_time) - 1 as dayOfWeek, " +
           "HOUR(ar.check_in_time) as hour, " +
           "COUNT(*) as count " +
           "FROM attendance_records ar " +
           "WHERE ar.check_in_time >= #{startDate} AND ar.check_in_time <= #{endDate} " +
           "GROUP BY DAYOFWEEK(ar.check_in_time) - 1, HOUR(ar.check_in_time) " +
           "ORDER BY dayOfWeek, hour")
    List<Map<String, Object>> getAllCheckInTimeDistribution(
        @Param("startDate") String startDate, 
        @Param("endDate") String endDate
    );
} 