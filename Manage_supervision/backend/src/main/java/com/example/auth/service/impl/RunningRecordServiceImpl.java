package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.RunningRecordMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.request.RunningRecordRequest;
import com.example.auth.model.dto.response.RunningRecordResponse;
import com.example.auth.model.dto.response.RunningStatsResponse;
import com.example.auth.model.entity.RunningRecord;
import com.example.auth.model.entity.User;
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
import java.time.temporal.ChronoUnit;
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
    
    @Autowired
    private UserMapper userMapper;
    
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
    
    @Override
    public PageResponse<Map<String, Object>> getAllUserRunningRecords(
            int page, int size, String username, String startDate, String endDate) {
        // 构建查询条件
        Page<RunningRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RunningRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加日期过滤条件
        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            queryWrapper.ge(RunningRecord::getRecordDate, start);
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            queryWrapper.le(RunningRecord::getRecordDate, end);
        }
        
        // 按日期倒序排序
        queryWrapper.orderByDesc(RunningRecord::getRecordDate);
        
        // 执行分页查询
        Page<RunningRecord> recordPage = runningRecordMapper.selectPage(pageParam, queryWrapper);
        
        System.out.println("查询到的跑步记录总数: " + recordPage.getTotal());
        System.out.println("当前页记录数: " + recordPage.getRecords().size());
        
        // 获取用户ID列表
        Set<Long> userIds = recordPage.getRecords().stream()
                .map(RunningRecord::getUserId)
                .collect(Collectors.toSet());
        
        System.out.println("涉及用户数: " + userIds.size());
        
        // 批量查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.in(User::getId, userIds);
            
            // 如果有用户名过滤条件，添加过滤
            if (username != null && !username.isEmpty()) {
                userQueryWrapper.like(User::getUsername, username);
            }
            
            List<User> users = userMapper.selectList(userQueryWrapper);
            System.out.println("找到的用户数: " + users.size());
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        }
        
        // 构建响应数据
        List<Map<String, Object>> records = new ArrayList<>();
        for (RunningRecord record : recordPage.getRecords()) {
            User user = userMap.get(record.getUserId());
            // 如果有用户名过滤且该记录的用户不在过滤结果中，跳过
            if (username != null && !username.isEmpty() && user == null) {
                continue;
            }
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("userId", record.getUserId());
            map.put("username", user != null ? user.getUsername() : "未知用户");
            map.put("nickname", user != null ? user.getNickname() : "未知用户");
            map.put("realName", user != null ? user.getRealName() : "未知用户");
            map.put("distance", record.getDistance());
            map.put("duration", record.getDuration());
            map.put("pace", record.getPace());
            map.put("recordDate", record.getRecordDate().format(DATE_FORMATTER));
            map.put("createTime", record.getCreateTime().format(FORMATTER));
            
            records.add(map);
        }
        
        System.out.println("最终构建的记录数: " + records.size());
        
        // 创建分页响应对象
        PageResponse<Map<String, Object>> response = new PageResponse<>(
            records,
            recordPage.getTotal(),
            (int)recordPage.getCurrent(), 
            (int)recordPage.getSize()
        );
        
        return response;
    }
    
    @Override
    public Map<String, Object> getRunningStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取所有跑步记录总数
        LambdaQueryWrapper<RunningRecord> countWrapper = new LambdaQueryWrapper<>();
        long totalRecords = runningRecordMapper.selectCount(countWrapper);
        statistics.put("totalRecords", totalRecords);
        
        // 获取有跑步记录的用户数量
        LambdaQueryWrapper<RunningRecord> distinctUserWrapper = new LambdaQueryWrapper<>();
        List<RunningRecord> allRecords = runningRecordMapper.selectList(distinctUserWrapper);
        long activeUsers = allRecords.stream()
                .map(RunningRecord::getUserId)
                .distinct()
                .count();
        statistics.put("activeUsers", activeUsers);
        
        // 获取本周新增的跑步记录数
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
        LambdaQueryWrapper<RunningRecord> thisWeekWrapper = new LambdaQueryWrapper<>();
        thisWeekWrapper.ge(RunningRecord::getRecordDate, startOfWeek);
        long thisWeekRecords = runningRecordMapper.selectCount(thisWeekWrapper);
        statistics.put("thisWeekRecords", thisWeekRecords);
        
        // 获取本月新增的跑步记录数
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LambdaQueryWrapper<RunningRecord> thisMonthWrapper = new LambdaQueryWrapper<>();
        thisMonthWrapper.ge(RunningRecord::getRecordDate, startOfMonth);
        long thisMonthRecords = runningRecordMapper.selectCount(thisMonthWrapper);
        statistics.put("thisMonthRecords", thisMonthRecords);
        
        // 计算总跑步距离
        BigDecimal totalDistance = allRecords.stream()
                .map(RunningRecord::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("totalDistance", totalDistance);
        
        // 返回最近跑步记录的日期
        List<RunningRecord> latestRecords = allRecords.stream()
                .sorted(Comparator.comparing(RunningRecord::getRecordDate).reversed())
                .limit(1)
                .collect(Collectors.toList());
        
        if (!latestRecords.isEmpty()) {
            LocalDate latestDate = latestRecords.get(0).getRecordDate();
            statistics.put("latestRecordDate", latestDate.format(DATE_FORMATTER));
            
            // 添加距离最新记录的天数
            long daysSinceLatestRecord = ChronoUnit.DAYS.between(latestDate, now);
            statistics.put("daysSinceLatestRecord", daysSinceLatestRecord);
        }
        
        return statistics;
    }
    
    @Override
    public boolean deleteRecord(Long recordId) {
        int result = runningRecordMapper.deleteById(recordId);
        return result > 0;
    }
} 