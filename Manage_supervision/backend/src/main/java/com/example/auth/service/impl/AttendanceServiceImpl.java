package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.AttendanceMapper;
import com.example.auth.mapper.AttendanceRecordMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.AttendanceDTO;
import com.example.auth.model.dto.AttendanceRecordDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.entity.Attendance;
import com.example.auth.model.entity.AttendanceRecord;
import com.example.auth.model.entity.User;
import com.example.auth.service.AttendanceService;
import com.example.auth.service.FaceRecognitionService;
import com.example.auth.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    @Autowired
    private AttendanceMapper attendanceMapper;
    
    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserContext userContext;
    
    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Override
    @Transactional
    public AttendanceDTO createAttendance(AttendanceDTO attendanceDTO) {
        // 转换DTO为实体
        Attendance attendance = new Attendance();
        BeanUtils.copyProperties(attendanceDTO, attendance);
        
        // 设置创建时间和状态
        attendance.setCreateTime(LocalDateTime.now());
        attendance.setStatus("active");
        
        // 保存到数据库
        attendanceMapper.insert(attendance);
        
        // 设置返回的DTO
        attendanceDTO.setId(attendance.getId());
        attendanceDTO.setCreateTime(attendance.getCreateTime());
        attendanceDTO.setStatus(attendance.getStatus());
        
        // 获取创建者信息
        User creator = userMapper.selectById(attendance.getCreatorId());
        if (creator != null) {
            attendanceDTO.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
        }
        
        // 设置统计信息
        attendanceDTO.setTotalUsers(attendanceMapper.countTotalActiveUsers());
        attendanceDTO.setCheckedInUsers(0);
        
        return attendanceDTO;
    }

    @Override
    @Transactional
    public AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO) {
        // 检查考勤是否存在
        Attendance existingAttendance = attendanceMapper.selectById(id);
        if (existingAttendance == null) {
            throw new RuntimeException("考勤不存在");
        }
        
        // 更新考勤信息
        BeanUtils.copyProperties(attendanceDTO, existingAttendance);
        existingAttendance.setId(id); // 确保ID不变
        
        // 保存到数据库
        attendanceMapper.updateById(existingAttendance);
        
        // 返回更新后的考勤信息
        return getAttendanceById(id);
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        // 先删除相关的考勤记录
        QueryWrapper<AttendanceRecord> recordWrapper = new QueryWrapper<>();
        recordWrapper.eq("attendance_id", id);
        attendanceRecordMapper.delete(recordWrapper);
        
        // 然后删除考勤
        attendanceMapper.deleteById(id);
    }

    @Override
    public AttendanceDTO getAttendanceById(Long id) {
        Attendance attendance = attendanceMapper.selectById(id);
        if (attendance == null) {
            return null;
        }
        
        AttendanceDTO attendanceDTO = new AttendanceDTO();
        BeanUtils.copyProperties(attendance, attendanceDTO);
        
        // 获取创建者信息
        User creator = userMapper.selectById(attendance.getCreatorId());
        if (creator != null) {
            attendanceDTO.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
        }
        
        // 获取统计信息
        attendanceDTO.setTotalUsers(attendanceMapper.countTotalActiveUsers());
        attendanceDTO.setCheckedInUsers(attendanceMapper.countCheckedInUsers(id));
        
        return attendanceDTO;
    }

    @Override
    public PageResponse<AttendanceDTO> getAttendanceList(int page, int size, String status) {
        // 构建查询条件
        QueryWrapper<Attendance> queryWrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");
        
        // 分页查询
        Page<Attendance> pageResult = new Page<>(page, size);
        Page<Attendance> attendancePage = attendanceMapper.selectPage(pageResult, queryWrapper);
        
        // 转换为DTO
        List<AttendanceDTO> attendanceDTOs = attendancePage.getRecords().stream().map(attendance -> {
            AttendanceDTO dto = new AttendanceDTO();
            BeanUtils.copyProperties(attendance, dto);
            
            // 获取创建者信息
            User creator = userMapper.selectById(attendance.getCreatorId());
            if (creator != null) {
                dto.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
            }
            
            // 获取统计信息
            dto.setTotalUsers(attendanceMapper.countTotalActiveUsers());
            dto.setCheckedInUsers(attendanceMapper.countCheckedInUsers(attendance.getId()));
            
            return dto;
        }).collect(Collectors.toList());
        
        return new PageResponse<>(attendanceDTOs, attendancePage.getTotal(), (int) attendancePage.getCurrent(), (int) attendancePage.getSize());
    }

    @Override
    @Transactional
    public AttendanceRecordDTO checkIn(Long attendanceId, Long userId, AttendanceRecordDTO recordDTO) {
        // 检查考勤是否存在并且在有效时间内
        Attendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw new RuntimeException("考勤不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(attendance.getStartTime()) || now.isAfter(attendance.getEndTime())) {
            throw new RuntimeException("不在考勤时间范围内");
        }
        
        // 检查用户是否已经签到过
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attendance_id", attendanceId)
                   .eq("user_id", userId);
        if (attendanceRecordMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("您已经签到过了");
        }
        
        // 创建签到记录
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceId(attendanceId);
        record.setUserId(userId);
        record.setCheckInTime(now);
        record.setStatus("正常");
        record.setFaceVerified(false); // 非人脸识别签到
        
        if (recordDTO != null) {
            record.setLocation(recordDTO.getLocation());
            record.setNotes(recordDTO.getNotes());
        }
        
        // 保存签到记录
        attendanceRecordMapper.insert(record);
        
        // 返回签到信息
        AttendanceRecordDTO resultDTO = new AttendanceRecordDTO();
        resultDTO.setId(record.getId());
        resultDTO.setAttendanceId(record.getAttendanceId());
        resultDTO.setUserId(record.getUserId());
        resultDTO.setCheckInTime(record.getCheckInTime());
        resultDTO.setStatus(record.getStatus());
        resultDTO.setLocation(record.getLocation());
        resultDTO.setNotes(record.getNotes());
        resultDTO.setFaceVerified(record.getFaceVerified());
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user != null) {
            resultDTO.setUsername(user.getUsername());
            resultDTO.setRealName(user.getRealName());
            resultDTO.setUserNumber(user.getUserNumber());
        }
        
        return resultDTO;
    }

    @Override
    @Transactional
    public AttendanceRecordDTO faceCheckIn(Long attendanceId, Long userId, AttendanceRecordDTO recordDTO, String faceData) {
        logger.info("用户 {} 开始使用人脸识别签到考勤 {}", userId, attendanceId);
        
        try {
            // 1. 验证考勤ID有效性
            Attendance attendance = attendanceMapper.selectById(attendanceId);
            if (attendance == null) {
                logger.warn("签到失败: 考勤不存在, ID: {}", attendanceId);
                return new AttendanceRecordDTO("error", "考勤不存在");
            }
            
            // 2. 验证考勤时间有效性
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(attendance.getStartTime())) {
                logger.warn("签到失败: 考勤尚未开始, 当前时间: {}, 开始时间: {}", now, attendance.getStartTime());
                return new AttendanceRecordDTO("error", "考勤尚未开始");
            }
            
            if (now.isAfter(attendance.getEndTime())) {
                logger.warn("签到失败: 考勤已结束, 当前时间: {}, 结束时间: {}", now, attendance.getEndTime());
                return new AttendanceRecordDTO("error", "考勤已结束");
            }
            
            // 3. 检查是否已签到
            QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attendance_id", attendanceId).eq("user_id", userId);
            AttendanceRecord existingRecord = attendanceRecordMapper.selectOne(queryWrapper);
            if (existingRecord != null) {
                logger.warn("签到失败: 用户已签到, 签到时间: {}", existingRecord.getCheckInTime());
                return new AttendanceRecordDTO("error", "您已经完成签到");
            }
            
            // 4. 进行人脸验证
            if (faceData == null || faceData.isEmpty()) {
                logger.warn("签到失败: 未提供人脸数据");
                return new AttendanceRecordDTO("error", "未提供人脸数据，无法完成签到");
            }
            
            // 5. 调用人脸识别服务进行验证
            boolean faceVerified = false;
            try {
                logger.info("开始进行人脸验证，用户ID: {}", userId);
                faceVerified = faceRecognitionService.verifyFace(userId, faceData);
                
                if (!faceVerified) {
                    logger.warn("签到失败: 人脸验证未通过, 用户ID: {}", userId);
                    return new AttendanceRecordDTO("face_mismatch", "人脸验证失败: 当前人脸与系统中已注册的人脸不匹配，请确保是本人操作。若确认是本人操作，请尝试调整光线、角度或更新人脸信息");
                }
                
                logger.info("人脸验证通过，用户ID: {}", userId);
            } catch (Exception e) {
                logger.error("人脸验证过程中发生异常: {}", e.getMessage(), e);
                return new AttendanceRecordDTO("error", "人脸验证失败: " + e.getMessage());
            }
            
            // 6. 创建签到记录
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(attendanceId);
            record.setUserId(userId);
            record.setCheckInTime(now);
            record.setStatus("正常");
            
            // 设置位置信息（如果有）
            if (recordDTO != null && recordDTO.getLocation() != null) {
                record.setLocation(recordDTO.getLocation());
            }
            
            // 设置备注信息（如果有）
            if (recordDTO != null && recordDTO.getNotes() != null) {
                record.setNotes(recordDTO.getNotes());
            }
            
            // 标记为人脸验证通过
            record.setFaceVerified(true);
            
            // 保存签到记录
            attendanceRecordMapper.insert(record);
            logger.info("用户 {} 成功签到考勤 {}", userId, attendanceId);
            
            // 返回签到记录DTO
            AttendanceRecordDTO result = convertToDTO(record);
            
            // 添加考勤标题
            result.setAttendanceTitle(attendance.getTitle());
            
            // 查询用户信息
            User user = userMapper.selectById(userId);
            if (user != null) {
                result.setUsername(user.getUsername());
                result.setRealName(user.getRealName());
                result.setUserNumber(user.getUserNumber());
            }
            
            return result;
        } catch (Exception e) {
            logger.error("签到过程中发生异常", e);
            return new AttendanceRecordDTO("error", "签到过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 将AttendanceRecord实体转换为AttendanceRecordDTO
     */
    private AttendanceRecordDTO convertToDTO(AttendanceRecord record) {
        if (record == null) {
            return null;
        }
        
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setAttendanceId(record.getAttendanceId());
        dto.setUserId(record.getUserId());
        dto.setCheckInTime(record.getCheckInTime());
        dto.setStatus(record.getStatus());
        dto.setLocation(record.getLocation());
        dto.setNotes(record.getNotes());
        dto.setFaceVerified(record.getFaceVerified());
        
        // 获取考勤标题
        Attendance attendance = attendanceMapper.selectById(record.getAttendanceId());
        if (attendance != null) {
            dto.setAttendanceTitle(attendance.getTitle());
        }
        
        // 获取用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getRealName());
            dto.setUserNumber(user.getUserNumber());
        }
        
        return dto;
    }

    @Override
    public PageResponse<AttendanceRecordDTO> getAttendanceRecords(Long attendanceId, int page, int size) {
        Page<AttendanceRecordDTO> pageParam = new Page<>(page, size);
        var records = attendanceRecordMapper.getAttendanceRecords(pageParam, attendanceId);
        
        return new PageResponse<>(records.getRecords(), records.getTotal(), (int) records.getCurrent(), (int) records.getSize());
    }

    @Override
    public List<AttendanceRecordDTO> getUncheckedUsers(Long attendanceId) {
        return attendanceRecordMapper.getUncheckedUsers(attendanceId);
    }

    @Override
    public Map<String, Object> getAttendanceStatistics(Long attendanceId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取考勤信息
        Attendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw new RuntimeException("考勤不存在");
        }
        
        // 统计已签到人数
        int checkedInCount = attendanceMapper.countCheckedInUsers(attendanceId);
        int totalUsers = attendanceMapper.countTotalActiveUsers();
        int uncheckedCount = totalUsers - checkedInCount;
        
        statistics.put("attendanceId", attendanceId);
        statistics.put("title", attendance.getTitle());
        statistics.put("startTime", attendance.getStartTime());
        statistics.put("endTime", attendance.getEndTime());
        statistics.put("totalUsers", totalUsers);
        statistics.put("checkedInCount", checkedInCount);
        statistics.put("uncheckedCount", uncheckedCount);
        statistics.put("checkedInPercentage", totalUsers > 0 ? (double) checkedInCount / totalUsers * 100 : 0);
        
        return statistics;
    }

    @Override
    public List<AttendanceDTO> getCurrentUserAttendances(Long userId) {
        // 查询所有活跃状态的考勤
        QueryWrapper<Attendance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "active")
                   .orderByDesc("create_time");
        List<Attendance> attendances = attendanceMapper.selectList(queryWrapper);
        
        // 转换为DTO并添加签到状态
        return attendances.stream().map(attendance -> {
            AttendanceDTO dto = new AttendanceDTO();
            BeanUtils.copyProperties(attendance, dto);
            
            // 获取创建者信息
            User creator = userMapper.selectById(attendance.getCreatorId());
            if (creator != null) {
                dto.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
            }
            
            // 检查用户是否已签到
            QueryWrapper<AttendanceRecord> recordWrapper = new QueryWrapper<>();
            recordWrapper.eq("attendance_id", attendance.getId())
                        .eq("user_id", userId);
            long count = attendanceRecordMapper.selectCount(recordWrapper);
            
            // 获取统计信息
            dto.setTotalUsers(attendanceMapper.countTotalActiveUsers());
            dto.setCheckedInUsers(attendanceMapper.countCheckedInUsers(attendance.getId()));
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordDTO> getCurrentUserAttendanceRecords(Long userId) {
        // 查询用户的所有签到记录
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("check_in_time");
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(queryWrapper);
        
        // 转换为DTO
        List<AttendanceRecordDTO> recordDTOs = new ArrayList<>();
        for (AttendanceRecord record : records) {
            AttendanceRecordDTO dto = new AttendanceRecordDTO();
            BeanUtils.copyProperties(record, dto);
            
            // 获取考勤信息
            Attendance attendance = attendanceMapper.selectById(record.getAttendanceId());
            if (attendance != null) {
                dto.setAttendanceId(attendance.getId());
            }
            
            // 获取用户信息
            User user = userMapper.selectById(userId);
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setRealName(user.getRealName());
                dto.setUserNumber(user.getUserNumber());
            }
            
            recordDTOs.add(dto);
        }
        
        return recordDTOs;
    }

    @Override
    public List<AttendanceRecordDTO> getAllAttendanceRecords(Long attendanceId) {
        return attendanceRecordMapper.getAllAttendanceRecords(attendanceId);
    }
    
    @Override
    public byte[] exportAttendanceRecords(Long attendanceId) throws Exception {
        // 获取考勤信息
        Attendance attendance = attendanceMapper.selectById(attendanceId);
        if (attendance == null) {
            throw new RuntimeException("考勤不存在");
        }
        
        // 获取所有签到记录
        List<AttendanceRecordDTO> records = attendanceRecordMapper.getAllAttendanceRecords(attendanceId);
        
        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建工作表
            Sheet sheet = workbook.createSheet(attendance.getTitle());
            
            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "姓名", "学号", "签到时间", "状态", "位置", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            // 创建考勤信息行
            Row infoRow1 = sheet.createRow(1);
            Cell titleCell = infoRow1.createCell(0);
            titleCell.setCellValue("考勤标题:");
            
            Cell titleValueCell = infoRow1.createCell(1);
            titleValueCell.setCellValue(attendance.getTitle());
            
            Row infoRow2 = sheet.createRow(2);
            Cell timeCell = infoRow2.createCell(0);
            timeCell.setCellValue("考勤时间:");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Cell timeValueCell = infoRow2.createCell(1);
            timeValueCell.setCellValue(
                attendance.getStartTime().format(formatter) + " 至 " + 
                attendance.getEndTime().format(formatter)
            );
            
            Row infoRow3 = sheet.createRow(3);
            Cell statCell = infoRow3.createCell(0);
            statCell.setCellValue("签到统计:");
            
            Cell statValueCell = infoRow3.createCell(1);
            int totalUsers = attendanceMapper.countTotalActiveUsers();
            int checkedInUsers = records.size();
            statValueCell.setCellValue(
                checkedInUsers + " / " + totalUsers + 
                " (签到率: " + String.format("%.2f%%", (double) checkedInUsers / totalUsers * 100) + ")"
            );
            
            // 空行
            sheet.createRow(4);
            
            // 填充数据
            int rowNum = 5;
            for (int i = 0; i < records.size(); i++) {
                AttendanceRecordDTO record = records.get(i);
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(record.getRealName());
                row.createCell(2).setCellValue(record.getUserNumber());
                row.createCell(3).setCellValue(record.getCheckInTime().format(formatter));
                row.createCell(4).setCellValue(record.getStatus());
                row.createCell(5).setCellValue(record.getLocation() != null ? record.getLocation() : "");
                row.createCell(6).setCellValue(record.getNotes() != null ? record.getNotes() : "");
            }
            
            // 获取未签到用户列表
            List<AttendanceRecordDTO> uncheckedUsers = getUncheckedUsers(attendanceId);
            
            if (!uncheckedUsers.isEmpty()) {
                // 添加未签到用户标题
                Row uncheckedHeaderRow = sheet.createRow(rowNum++);
                Cell uncheckedHeaderCell = uncheckedHeaderRow.createCell(0);
                uncheckedHeaderCell.setCellValue("未签到用户");
                uncheckedHeaderCell.setCellStyle(headerStyle);
                
                // 填充未签到用户数据
                for (int i = 0; i < uncheckedUsers.size(); i++) {
                    AttendanceRecordDTO user = uncheckedUsers.get(i);
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(i + 1);
                    row.createCell(1).setCellValue(user.getRealName());
                    row.createCell(2).setCellValue(user.getUserNumber());
                    row.createCell(4).setCellValue("未签到");
                }
            }
            
            // 输出到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    @Override
    public Map<String, Object> getAttendanceSummary(String startDate, String endDate) {
        // 查询指定日期范围内的考勤记录
        List<AttendanceRecordDTO> records = attendanceRecordMapper.getAttendanceRecordsByDateRange(startDate, endDate);
        
        // 按考勤ID分组统计
        Map<Long, List<AttendanceRecordDTO>> recordsByAttendance = records.stream()
            .collect(Collectors.groupingBy(AttendanceRecordDTO::getAttendanceId));
        
        // 统计每个考勤的签到情况
        List<Map<String, Object>> attendanceSummaries = new ArrayList<>();
        int totalAttendance = 0;
        int totalUsers = attendanceMapper.countTotalActiveUsers();
        int totalCheckins = 0;
        
        for (Map.Entry<Long, List<AttendanceRecordDTO>> entry : recordsByAttendance.entrySet()) {
            Long attendanceId = entry.getKey();
            List<AttendanceRecordDTO> attendanceRecords = entry.getValue();
            
            Attendance attendance = attendanceMapper.selectById(attendanceId);
            if (attendance == null) continue;
            
            totalAttendance++;
            int checkedInCount = attendanceRecords.size();
            totalCheckins += checkedInCount;
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", attendanceId);
            summary.put("title", attendance.getTitle());
            summary.put("startTime", attendance.getStartTime());
            summary.put("endTime", attendance.getEndTime());
            summary.put("checkedInCount", checkedInCount);
            summary.put("totalUsers", totalUsers);
            summary.put("checkInRate", totalUsers > 0 ? (double) checkedInCount / totalUsers * 100 : 0);
            
            attendanceSummaries.add(summary);
        }
        
        // 计算总体签到率
        double overallCheckInRate = (totalAttendance > 0 && totalUsers > 0) 
            ? (double) totalCheckins / (totalAttendance * totalUsers) * 100 
            : 0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalAttendance", totalAttendance);
        result.put("totalUsers", totalUsers);
        result.put("totalCheckins", totalCheckins);
        result.put("overallCheckInRate", overallCheckInRate);
        result.put("attendanceSummaries", attendanceSummaries);
        
        return result;
    }
} 