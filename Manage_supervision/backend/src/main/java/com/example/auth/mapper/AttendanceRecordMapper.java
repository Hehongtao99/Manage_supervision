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

@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
    
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number " +
           "FROM e981_attendance_record ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "WHERE ar.attendance_id = #{attendanceId} " +
           "ORDER BY ar.check_in_time DESC")
    IPage<AttendanceRecordDTO> getAttendanceRecords(Page<AttendanceRecordDTO> page, @Param("attendanceId") Long attendanceId);
    
    @Select("SELECT u.id, u.username, u.real_name, u.user_number " +
           "FROM users u " +
           "LEFT JOIN e981_attendance_record ar ON u.id = ar.user_id AND ar.attendance_id = #{attendanceId} " +
           "JOIN user_roles ur ON u.id = ur.user_id " +
           "JOIN roles r ON ur.role_id = r.id " +
           "WHERE r.name = 'USER' AND ar.id IS NULL AND u.status = 'active'")
    List<AttendanceRecordDTO> getUncheckedUsers(@Param("attendanceId") Long attendanceId);
    
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number " +
           "FROM e981_attendance_record ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "WHERE ar.attendance_id = #{attendanceId} " +
           "ORDER BY ar.check_in_time DESC")
    List<AttendanceRecordDTO> getAllAttendanceRecords(@Param("attendanceId") Long attendanceId);
    
    @Select("SELECT ar.*, u.username, u.real_name, u.user_number, a.title as attendance_title " +
           "FROM e981_attendance_record ar " +
           "JOIN users u ON ar.user_id = u.id " +
           "JOIN e981_attendance a ON ar.attendance_id = a.id " +
           "WHERE ar.check_in_time BETWEEN #{startDate} AND #{endDate} " +
           "ORDER BY ar.attendance_id, ar.check_in_time")
    List<AttendanceRecordDTO> getAttendanceRecordsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
} 