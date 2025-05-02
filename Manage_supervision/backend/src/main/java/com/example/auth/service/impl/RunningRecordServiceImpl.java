package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.RunningRecordMapper;
import com.example.auth.model.dto.request.RunningRecordRequest;
import com.example.auth.model.dto.response.RunningRecordResponse;
import com.example.auth.model.dto.response.RunningStatsResponse;
import com.example.auth.model.entity.RunningRecord;
import com.example.auth.service.RunningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 跑步记录服务实现类
 */
@Service
public class RunningRecordServiceImpl implements RunningRecordService {

    @Autowired
    private RunningRecordMapper runningRecordMapper;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-ww");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final Locale LOCALE = Locale.CHINA;
    private static final WeekFields WEEK_FIELDS = WeekFields.of(LOCALE);

    @Override
    public RunningRecordResponse addRecord(Long userId, RunningRecordRequest request) {
        RunningRecord record = new RunningRecord();
        record.setUserId(userId);
        record.setDistance(request.getDistance());
        record.setDuration(request.getDuration());
        record.setPace(request.getPace());
        record.setRecordDate(request.getRecordDate() != null ? request.getRecordDate() : java.time.LocalDate.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        runningRecordMapper.insert(record);
        
        return convertToResponse(record);
    }

    @Override
    public List<RunningRecordResponse> getRecordsByUserId(Long userId) {
        LambdaQueryWrapper<RunningRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RunningRecord::getUserId, userId);
        queryWrapper.orderByDesc(RunningRecord::getRecordDate);
        
        List<RunningRecord> records = runningRecordMapper.selectList(queryWrapper);
        
        return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RunningRecordResponse getRecordById(Long recordId) {
        RunningRecord record = runningRecordMapper.selectById(recordId);
        if (record == null) {
            return null;
        }
        
        return convertToResponse(record);
    }
    
    @Override
    public RunningStatsResponse getUserRunningStats(Long userId) {
        // 查询该用户所有跑步记录
        LambdaQueryWrapper<RunningRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RunningRecord::getUserId, userId);
        queryWrapper.orderByAsc(RunningRecord::getRecordDate);
        List<RunningRecord> records = runningRecordMapper.selectList(queryWrapper);
        
        if (records.isEmpty()) {
            return new RunningStatsResponse();
        }
        
        // 初始化响应对象
        RunningStatsResponse response = new RunningStatsResponse();
        
        // 计算总跑量和总次数
        BigDecimal totalDistance = BigDecimal.ZERO;
        for (RunningRecord record : records) {
            totalDistance = totalDistance.add(record.getDistance());
        }
        response.setTotalDistance(totalDistance);
        response.setTotalRunCount(records.size());
        
        // 计算本周和本月跑量
        LocalDate today = LocalDate.now();
        int currentWeekOfYear = today.get(WEEK_FIELDS.weekOfYear());
        int currentYear = today.getYear();
        YearMonth currentYearMonth = YearMonth.from(today);
        
        BigDecimal currentWeekDistance = BigDecimal.ZERO;
        BigDecimal currentMonthDistance = BigDecimal.ZERO;
        
        // 计算周维度跑量统计
        Map<String, BigDecimal> weeklyStats = new LinkedHashMap<>();
        
        // 计算月维度配速统计（最近30天）
        LocalDate thirtyDaysAgo = today.minusDays(30);
        List<Map<String, Object>> monthlyPaceStats = new ArrayList<>();
        
        // 计算平均配速和最好配速
        int totalPaceMinutes = 0;
        int totalPaceSeconds = 0;
        String bestPace = null;
        
        for (RunningRecord record : records) {
            LocalDate recordDate = record.getRecordDate();
            
            // 计算周维度跑量
            int weekOfYear = recordDate.get(WEEK_FIELDS.weekOfYear());
            int year = recordDate.getYear();
            String weekKey = year + "-" + String.format("%02d", weekOfYear);
            
            weeklyStats.compute(weekKey, (k, v) -> {
                if (v == null) {
                    return record.getDistance();
                }
                return v.add(record.getDistance());
            });
            
            // 计算本周和本月跑量
            if (recordDate.get(WEEK_FIELDS.weekOfYear()) == currentWeekOfYear && 
                recordDate.getYear() == currentYear) {
                currentWeekDistance = currentWeekDistance.add(record.getDistance());
            }
            
            if (recordDate.getYear() == currentYearMonth.getYear() && 
                recordDate.getMonthValue() == currentYearMonth.getMonthValue()) {
                currentMonthDistance = currentMonthDistance.add(record.getDistance());
            }
            
            // 计算月维度配速（最近30天）
            if (recordDate.isAfter(thirtyDaysAgo) || recordDate.isEqual(thirtyDaysAgo)) {
                Map<String, Object> paceData = new HashMap<>();
                paceData.put("date", recordDate.format(DATE_FORMATTER));
                
                // 将配速转换为秒数以便计算
                String[] paceParts = record.getPace().split(":");
                int paceMinutes = Integer.parseInt(paceParts[0]);
                int paceSeconds = Integer.parseInt(paceParts[1]);
                int totalSeconds = paceMinutes * 60 + paceSeconds;
                
                paceData.put("pace", totalSeconds); // 保存为秒数以便前端处理
                paceData.put("paceStr", record.getPace()); // 原格式配速
                
                monthlyPaceStats.add(paceData);
                
                // 累计总配速用于计算平均值
                totalPaceMinutes += paceMinutes;
                totalPaceSeconds += paceSeconds;
                
                // 更新最好配速
                if (bestPace == null) {
                    bestPace = record.getPace();
                } else {
                    String[] bestPaceParts = bestPace.split(":");
                    int bestPaceMinutes = Integer.parseInt(bestPaceParts[0]);
                    int bestPaceSeconds = Integer.parseInt(bestPaceParts[1]);
                    int bestTotalSeconds = bestPaceMinutes * 60 + bestPaceSeconds;
                    
                    if (totalSeconds < bestTotalSeconds) {
                        bestPace = record.getPace();
                    }
                }
            }
        }
        
        // 计算平均配速
        int totalRecords = records.size();
        if (totalRecords > 0) {
            int averageTotalSeconds = (totalPaceMinutes * 60 + totalPaceSeconds) / totalRecords;
            int avgMinutes = averageTotalSeconds / 60;
            int avgSeconds = averageTotalSeconds % 60;
            String averagePace = String.format("%d:%02d", avgMinutes, avgSeconds);
            response.setAveragePace(averagePace);
        }
        
        response.setCurrentWeekDistance(currentWeekDistance);
        response.setCurrentMonthDistance(currentMonthDistance);
        response.setWeeklyDistanceStats(weeklyStats);
        response.setMonthlyPaceStats(monthlyPaceStats);
        response.setBestPace(bestPace);
        
        return response;
    }
    
    /**
     * 将实体对象转换为响应DTO
     *
     * @param record 跑步记录实体
     * @return 跑步记录响应DTO
     */
    private RunningRecordResponse convertToResponse(RunningRecord record) {
        if (record == null) {
            return null;
        }
        
        RunningRecordResponse response = new RunningRecordResponse();
        response.setId(record.getId());
        response.setDistance(record.getDistance());
        response.setDuration(record.getDuration());
        response.setPace(record.getPace());
        response.setRecordDate(record.getRecordDate());
        
        if (record.getCreateTime() != null) {
            response.setCreateTime(record.getCreateTime().format(FORMATTER));
        }
        
        return response;
    }
} 