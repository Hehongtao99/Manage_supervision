package com.example.auth.repository;

import com.example.auth.entity.Attendance;
import com.example.auth.entity.Class;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    /**
     * 根据学生ID查询考勤记录
     */
    List<Attendance> findByStudentIdOrderByCheckInTimeDesc(Long studentId);
    
    /**
     * 根据班级ID查询考勤记录
     */
    List<Attendance> findByStudentClassIdOrderByCheckInTimeDesc(Long classId);
    
    /**
     * 查询学生在某个时间范围内的考勤记录
     */
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.checkInTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.checkInTime DESC")
    List<Attendance> findByStudentIdAndTimeRange(
            @Param("studentId") Long studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询班级在某个时间范围内的考勤记录
     */
    @Query("SELECT a FROM Attendance a WHERE a.studentClass.id = :classId " +
           "AND a.checkInTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.checkInTime DESC")
    List<Attendance> findByStudentClassIdAndTimeRange(
            @Param("classId") Long classId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询学生在当天是否已有考勤记录
     */
    @Query("SELECT COUNT(a) > 0 FROM Attendance a WHERE a.student.id = :studentId " +
           "AND CAST(a.checkInTime AS date) = CURRENT_DATE")
    boolean existsByStudentIdAndCurrentDate(@Param("studentId") Long studentId);
} 