package com.example.auth.service.impl;

import com.example.auth.dto.AttendanceDTO;
import com.example.auth.entity.Attendance;
import com.example.auth.entity.Class;
import com.example.auth.entity.User;
import com.example.auth.repository.AttendanceRepository;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.repository.ClassStudentRelationRepository;
import com.example.auth.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private ClassStudentRelationRepository classStudentRelationRepository;

    @Override
    public Attendance submitAttendance(Long studentId, Long classId, boolean faceRecognized, String recognitionDetails) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        Class studentClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setStudentClass(studentClass);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setFaceRecognized(faceRecognized);
        attendance.setRecognitionDetails(recognitionDetails);
        
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<AttendanceDTO> getStudentAttendanceRecords(Long studentId) {
        List<Attendance> attendances = attendanceRepository.findByStudentIdOrderByCheckInTimeDesc(studentId);
        return convertToDTO(attendances);
    }

    @Override
    public boolean isStudentAttendedToday(Long studentId) {
        return attendanceRepository.existsByStudentIdAndCurrentDate(studentId);
    }

    @Override
    public List<AttendanceDTO> getClassAttendanceRecords(Long classId) {
        List<Attendance> attendances = attendanceRepository.findByStudentClassIdOrderByCheckInTimeDesc(classId);
        return convertToDTO(attendances);
    }

    @Override
    public List<AttendanceDTO> getClassAttendanceRecordsByDateRange(Long classId, String startDate, String endDate) {
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        
        // 需要在AttendanceRepository中添加按班级和日期范围查询的方法
        List<Attendance> attendances = attendanceRepository.findByStudentClassIdAndTimeRange(
                classId, startDateTime, endDateTime);
        return convertToDTO(attendances);
    }
    
    @Override
    public Map<String, Object> getClassAttendanceStatistics(Long classId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取班级信息
        Class studentClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        // 获取当天的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(23, 59, 59);
        
        // 获取当月的开始和结束时间
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime monthEnd = lastDayOfMonth.atTime(23, 59, 59);
        
        // 查询当天的考勤记录
        List<Attendance> todayAttendances = attendanceRepository.findByStudentClassIdAndTimeRange(
                classId, dayStart, dayEnd);
        
        // 查询当月的考勤记录
        List<Attendance> monthlyAttendances = attendanceRepository.findByStudentClassIdAndTimeRange(
                classId, monthStart, monthEnd);
        
        // 获取班级总人数
        List<User> classStudents = classStudentRelationRepository.findActiveStudentsByClassId(classId);
        int totalStudents = classStudents.size();
        
        // 计算今日考勤人数
        int todayAttended = (int) todayAttendances.stream()
                .map(a -> a.getStudent().getId())
                .distinct()
                .count();
        
        // 计算考勤率
        double attendanceRate = totalStudents > 0 ? 
                ((double) todayAttended / totalStudents) * 100 : 0;
        
        // 统计信息
        statistics.put("totalStudents", totalStudents);
        statistics.put("todayAttended", todayAttended);
        statistics.put("totalAttendances", monthlyAttendances.size());
        statistics.put("attendanceRate", String.format("%.2f", attendanceRate));
        
        return statistics;
    }

    @Override
    public Map<String, Object> getStudentAttendanceStatistics(Long studentId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取当月的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime monthEnd = lastDayOfMonth.atTime(23, 59, 59);
        
        // 查询当月的考勤记录
        List<Attendance> monthlyAttendances = attendanceRepository.findByStudentIdAndTimeRange(
                studentId, monthStart, monthEnd);
        
        // 计算当月考勤率
        int daysInMonth = today.lengthOfMonth();
        int attendedDays = monthlyAttendances.size();
        double attendanceRate = (double) attendedDays / daysInMonth * 100;
        
        // 格式化考勤记录日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> attendanceDates = monthlyAttendances.stream()
                .map(a -> a.getCheckInTime().format(formatter))
                .collect(Collectors.toList());
        
        statistics.put("totalDays", daysInMonth);
        statistics.put("attendedDays", attendedDays);
        statistics.put("attendanceRate", String.format("%.2f", attendanceRate));
        statistics.put("attendanceDates", attendanceDates);
        
        return statistics;
    }
    
    /**
     * 将考勤实体转换为DTO
     */
    private List<AttendanceDTO> convertToDTO(List<Attendance> attendances) {
        List<AttendanceDTO> dtoList = new ArrayList<>();
        
        for (Attendance attendance : attendances) {
            AttendanceDTO dto = new AttendanceDTO();
            dto.setId(attendance.getId());
            dto.setStudentId(attendance.getStudent().getId());
            dto.setStudentName(attendance.getStudent().getRealName() != null ? 
                    attendance.getStudent().getRealName() : attendance.getStudent().getUsername());
            dto.setClassId(attendance.getStudentClass().getId());
            dto.setClassName(attendance.getStudentClass().getClassName());
            dto.setCheckInTime(attendance.getCheckInTime());
            dto.setFaceRecognized(attendance.isFaceRecognized());
            dto.setRecognitionDetails(attendance.getRecognitionDetails());
            dto.setCreateTime(attendance.getCreateTime());
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
} 