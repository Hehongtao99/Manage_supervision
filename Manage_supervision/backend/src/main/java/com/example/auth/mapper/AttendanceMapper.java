package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
    
    @Select("SELECT COUNT(*) FROM attendance_records WHERE attendance_id = #{attendanceId}")
    int countCheckedInUsers(@Param("attendanceId") Long attendanceId);
    
    @Select("SELECT COUNT(*) FROM users WHERE status = 'active'")
    int countTotalActiveUsers();
} 