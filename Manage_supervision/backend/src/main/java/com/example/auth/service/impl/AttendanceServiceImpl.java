package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

        // 将LocalDateTime格式化为字符串
        if (record.getCheckInTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            resultDTO.setCheckInTime(record.getCheckInTime().format(formatter));
        }

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
        
        // 将LocalDateTime转换为格式化的字符串
        if (record.getCheckInTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setCheckInTime(record.getCheckInTime().format(formatter));
        }
        
        dto.setStatus(record.getStatus());
        dto.setLocation(record.getLocation());
        dto.setNotes(record.getNotes());
        dto.setFaceVerified(record.getFaceVerified());
        dto.setSimilarity(record.getSimilarity());
        
        // 获取考勤标题
        if (record.getAttendanceId() != null && record.getAttendanceId() > 0) {
            Attendance attendance = attendanceMapper.selectById(record.getAttendanceId());
            if (attendance != null) {
                dto.setAttendanceTitle(attendance.getTitle());
            }
        } else if (record.getAttendanceId() != null && record.getAttendanceId() == -1L) {
            dto.setAttendanceTitle("公共人脸识别记录");
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
            // 使用已修复的convertToDTO方法
            AttendanceRecordDTO dto = convertToDTO(record);
            if (dto != null) {
                recordDTOs.add(dto);
            }
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
                
                // 直接使用DTO中的checkInTime字符串，不需要再次格式化
                row.createCell(3).setCellValue(record.getCheckInTime() != null ? record.getCheckInTime() : "");
                
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
            
            // 如果是特殊考勤记录（ID为-1），则跳过
            if (attendanceId == -1L) {
                continue;
            }
            
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
    
    @Override
    public Long getActiveAttendanceId() {
        logger.info("获取当前活动考勤ID");
        try {
            // 构建查询条件：状态为活动且当前时间在考勤时间范围内
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Attendance::getStatus, "active")
                       .le(Attendance::getStartTime, now)  // 开始时间早于等于当前时间
                       .ge(Attendance::getEndTime, now)    // 结束时间晚于等于当前时间
                       .orderByDesc(Attendance::getCreateTime);
            
            // 查询符合条件的第一条记录
            Attendance attendance = attendanceMapper.selectOne(queryWrapper);
            
            if (attendance != null) {
                logger.info("找到当前活动考勤，ID: {}, 标题: {}", attendance.getId(), attendance.getTitle());
                return attendance.getId();
            } else {
                logger.info("当前没有活动考勤");
                
                // 如果没有正在进行中的考勤，则查找最近一次的考勤
                LambdaQueryWrapper<Attendance> recentWrapper = new LambdaQueryWrapper<>();
                recentWrapper.eq(Attendance::getStatus, "active")
                           .orderByDesc(Attendance::getCreateTime);
                
                Attendance recentAttendance = attendanceMapper.selectOne(recentWrapper);
                if (recentAttendance != null) {
                    logger.info("找到最近的考勤，ID: {}, 标题: {}", recentAttendance.getId(), recentAttendance.getTitle());
                    return recentAttendance.getId();
                }
                
                return null;
            }
        } catch (Exception e) {
            logger.error("获取当前活动考勤ID过程中发生异常", e);
            return null;
        }
    }

    @Override
    public boolean saveSpecialAttendanceRecord(AttendanceRecordDTO recordDTO) {
        try {
            logger.info("保存特殊考勤记录，用户ID: {}", recordDTO.getUserId());
            
            // 1. 创建特殊考勤记录
            AttendanceRecord record = new AttendanceRecord();
            
            // 设置基本字段
            record.setUserId(recordDTO.getUserId());
            record.setStatus(recordDTO.getStatus() != null ? recordDTO.getStatus() : "系统记录");
            record.setLocation(recordDTO.getLocation());
            record.setNotes(recordDTO.getNotes());
            record.setFaceVerified(true); // 特殊记录都是通过人脸验证的
            
            // 设置考勤ID（特殊值，表示系统记录）
            // 因为数据库中这个字段不能为null，所以我们使用一个特殊值-1
            record.setAttendanceId(-1L);
            
            // 设置签到时间
            if (recordDTO.getCheckInTime() != null) {
                // 如果传入了时间字符串，尝试解析
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    record.setCheckInTime(LocalDateTime.parse(recordDTO.getCheckInTime(), formatter));
                } catch (Exception e) {
                    // 解析失败则使用当前时间
                    record.setCheckInTime(LocalDateTime.now());
                    logger.warn("解析时间字符串失败：{}，使用当前时间", recordDTO.getCheckInTime());
                }
            } else {
                // 没有时间则使用当前时间
                record.setCheckInTime(LocalDateTime.now());
            }
            
            // 2. 保存考勤记录
            int result = attendanceRecordMapper.insert(record);
            
            logger.info("特殊考勤记录保存结果: {}, 记录ID: {}", result > 0, record.getId());
            return result > 0;
        } catch (Exception e) {
            logger.error("保存特殊考勤记录异常", e);
            return false;
        }
    }

    @Override
    public PageResponse<AttendanceRecordDTO> getSpecialAttendanceRecords(int page, int size) {
        Page<AttendanceRecordDTO> pageParam = new Page<>(page, size);
        var records = attendanceRecordMapper.getSpecialAttendanceRecords(pageParam);
        
        // 处理结果，添加特殊标记
        for (AttendanceRecordDTO record : records.getRecords()) {
            // 由于这些记录不属于任何考勤，设置一个特殊标题
            record.setAttendanceTitle("公共人脸识别记录");
            
            // 如果没有状态，设置默认状态
            if (record.getStatus() == null || record.getStatus().isEmpty()) {
                record.setStatus("系统记录");
            }
        }
        
        return new PageResponse<>(records.getRecords(), records.getTotal(), (int) records.getCurrent(), (int) records.getSize());
    }
    
    @Override
    public PageResponse<AttendanceRecordDTO> getAllAttendanceRecordsForAdmin(int page, int size, String startDate, String endDate, String username, String recordType) {
        logger.info("管理员查询所有打卡记录，页码: {}, 大小: {}, 开始日期: {}, 结束日期: {}, 用户名: {}, 记录类型: {}", 
                    page, size, startDate, endDate, username, recordType);
        
        Page<AttendanceRecordDTO> pageParam = new Page<>(page, size);
        IPage<AttendanceRecordDTO> records;
        
        // 如果没有提供日期范围，默认查询最近30天的数据
        if ((startDate == null || startDate.isEmpty()) && (endDate == null || endDate.isEmpty())) {
            LocalDateTime endDateTime = LocalDateTime.now();
            LocalDateTime startDateTime = endDateTime.minusDays(30);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            startDate = startDateTime.format(formatter);
            endDate = endDateTime.format(formatter);
        }
        
        // 根据不同的筛选条件调用不同的查询方法
        if (recordType != null && recordType.equals("normal")) {
            // 只查询正常考勤记录
            records = attendanceRecordMapper.getNormalAttendanceRecords(pageParam);
        } else if (recordType != null && recordType.equals("special")) {
            // 只查询特殊考勤记录
            records = attendanceRecordMapper.getSpecialAttendanceRecordsForAdmin(pageParam);
        } else if (username != null && !username.isEmpty()) {
            // 按用户名查询
            records = attendanceRecordMapper.getAllAttendanceRecordsForAdminByUsername(pageParam, username);
        } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            // 按日期范围查询
            records = attendanceRecordMapper.getAllAttendanceRecordsForAdminByDateRange(pageParam, startDate, endDate);
        } else {
            // 查询所有记录
            records = attendanceRecordMapper.getAllAttendanceRecordsForAdmin(pageParam);
        }
        
        // 处理结果，确保每条记录都有完整的信息
        for (AttendanceRecordDTO record : records.getRecords()) {
            // 处理考勤标题
            if (record.getAttendanceTitle() == null || record.getAttendanceTitle().isEmpty()) {
                if (record.getAttendanceId() == -1L) {
                    record.setAttendanceTitle("公共人脸识别记录");
                } else {
                    record.setAttendanceTitle("未知考勤");
                }
            }
            
            // 处理状态
            if (record.getStatus() == null || record.getStatus().isEmpty()) {
                if (record.getAttendanceId() == -1L) {
                    record.setStatus("系统记录");
                } else {
                    record.setStatus("正常");
                }
            }
            
            // 处理人脸验证状态
            if (record.getFaceVerified() == null) {
                record.setFaceVerified(false);
            }
        }
        
        return new PageResponse<>(records.getRecords(), records.getTotal(), (int) records.getCurrent(), (int) records.getSize());
    }
    
    @Override
    public Map<String, Object> getAttendanceRecordsStatistics(String startDate, String endDate) {
        logger.info("获取打卡记录统计数据，开始日期: {}, 结束日期: {}", startDate, endDate);
        
        Map<String, Object> statistics;
        
        // 如果没有提供日期范围，查询所有数据
        if ((startDate == null || startDate.isEmpty()) && (endDate == null || endDate.isEmpty())) {
            statistics = attendanceRecordMapper.getAttendanceRecordsStatistics();
        } else {
            // 如果没有提供完整的日期范围，默认查询最近30天的数据
            if ((startDate == null || startDate.isEmpty()) || (endDate == null || endDate.isEmpty())) {
                LocalDateTime endDateTime = LocalDateTime.now();
                LocalDateTime startDateTime = endDateTime.minusDays(30);
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                startDate = startDateTime.format(formatter);
                endDate = endDateTime.format(formatter);
            }
            
            statistics = attendanceRecordMapper.getAttendanceRecordsStatisticsByDateRange(startDate, endDate);
        }
        
        // 添加查询时间范围
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        
        // 处理可能为null的值
        if (statistics.get("totalRecords") == null) {
            statistics.put("totalRecords", 0);
        }
        if (statistics.get("normalRecords") == null) {
            statistics.put("normalRecords", 0);
        }
        if (statistics.get("specialRecords") == null) {
            statistics.put("specialRecords", 0);
        }
        if (statistics.get("uniqueUsers") == null) {
            statistics.put("uniqueUsers", 0);
        }
        if (statistics.get("faceVerifiedRecords") == null) {
            statistics.put("faceVerifiedRecords", 0);
        }
        
        // 计算百分比
        int totalRecords = ((Number) statistics.get("totalRecords")).intValue();
        int normalRecords = ((Number) statistics.get("normalRecords")).intValue();
        int specialRecords = ((Number) statistics.get("specialRecords")).intValue();
        int faceVerifiedRecords = ((Number) statistics.get("faceVerifiedRecords")).intValue();
        
        if (totalRecords > 0) {
            statistics.put("normalRecordsPercentage", (double) normalRecords / totalRecords * 100);
            statistics.put("specialRecordsPercentage", (double) specialRecords / totalRecords * 100);
            statistics.put("faceVerifiedPercentage", (double) faceVerifiedRecords / totalRecords * 100);
        } else {
            statistics.put("normalRecordsPercentage", 0.0);
            statistics.put("specialRecordsPercentage", 0.0);
            statistics.put("faceVerifiedPercentage", 0.0);
        }
        
        // 获取总用户数
        int totalUsers = attendanceMapper.countTotalActiveUsers();
        statistics.put("totalUsers", totalUsers);
        
        return statistics;
    }
} 