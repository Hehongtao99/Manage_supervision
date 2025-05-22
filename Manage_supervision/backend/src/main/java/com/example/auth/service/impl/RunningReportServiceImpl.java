package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.RunningRecordMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.response.RunningRankingResponse;
import com.example.auth.model.dto.response.RunningReportResponse;
import com.example.auth.model.entity.RunningRecord;
import com.example.auth.model.entity.User;
import com.example.auth.service.RunningReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 跑步报表服务实现类
 */
@Service
@RequiredArgsConstructor
public class RunningReportServiceImpl implements RunningReportService {

    private final RunningRecordMapper runningRecordMapper;
    private final UserMapper userMapper;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<RunningRankingResponse> getDistanceRanking(LocalDate startDate, LocalDate endDate, Integer limit) {
        // 查询该时间段内的所有跑步记录
        List<RunningRecord> records = getRunningRecordsBetween(startDate, endDate);
        
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按用户分组并计算总距离
        Map<Long, BigDecimal> userDistanceMap = new HashMap<>();
        Map<Long, Integer> userRunCountMap = new HashMap<>();
        
        for (RunningRecord record : records) {
            Long userId = record.getUserId();
            BigDecimal distance = record.getDistance();
            
            userDistanceMap.put(userId, userDistanceMap.getOrDefault(userId, BigDecimal.ZERO).add(distance));
            userRunCountMap.put(userId, userRunCountMap.getOrDefault(userId, 0) + 1);
        }
        
        // 转换为排名列表
        List<Map.Entry<Long, BigDecimal>> sortedEntries = userDistanceMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
        
        // 查询用户信息并构建响应
        return buildRankingResponse(sortedEntries, userRunCountMap, true);
    }

    @Override
    public List<RunningRankingResponse> getFrequencyRanking(LocalDate startDate, LocalDate endDate, Integer limit) {
        // 查询该时间段内的所有跑步记录
        List<RunningRecord> records = getRunningRecordsBetween(startDate, endDate);
        
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按用户分组并计算跑步次数
        Map<Long, Integer> userRunCountMap = new HashMap<>();
        Map<Long, BigDecimal> userDistanceMap = new HashMap<>();
        
        for (RunningRecord record : records) {
            Long userId = record.getUserId();
            BigDecimal distance = record.getDistance();
            
            userRunCountMap.put(userId, userRunCountMap.getOrDefault(userId, 0) + 1);
            userDistanceMap.put(userId, userDistanceMap.getOrDefault(userId, BigDecimal.ZERO).add(distance));
        }
        
        // 转换为排名列表
        List<Map.Entry<Long, Integer>> sortedEntries = userRunCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
        
        // 查询用户信息并构建响应
        return buildRankingResponseForFrequency(sortedEntries, userDistanceMap);
    }

    @Override
    public List<RunningRankingResponse> getBestPaceRanking(LocalDate startDate, LocalDate endDate, Integer limit) {
        // 查询该时间段内的所有跑步记录
        List<RunningRecord> records = getRunningRecordsBetween(startDate, endDate);
        
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按用户分组并查找最佳配速
        Map<Long, String> userBestPaceMap = new HashMap<>();
        Map<Long, BigDecimal> userDistanceMap = new HashMap<>();
        Map<Long, Integer> userRunCountMap = new HashMap<>();
        
        for (RunningRecord record : records) {
            Long userId = record.getUserId();
            String pace = record.getPace();
            BigDecimal distance = record.getDistance();
            
            // 记录距离和次数
            userDistanceMap.put(userId, userDistanceMap.getOrDefault(userId, BigDecimal.ZERO).add(distance));
            userRunCountMap.put(userId, userRunCountMap.getOrDefault(userId, 0) + 1);
            
            // 记录最佳配速
            if (pace != null) {
                String currentBestPace = userBestPaceMap.get(userId);
                if (currentBestPace == null || comparePace(pace, currentBestPace) < 0) {
                    userBestPaceMap.put(userId, pace);
                }
            }
        }
        
        // 转换为排名列表（按配速从快到慢排序）
        List<Map.Entry<Long, String>> sortedEntries = userBestPaceMap.entrySet().stream()
                .sorted((e1, e2) -> comparePace(e1.getValue(), e2.getValue()))
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
        
        // 查询用户信息并构建响应
        return buildRankingResponseForPace(sortedEntries, userDistanceMap, userRunCountMap);
    }

    @Override
    public RunningReportResponse getRunningReport(LocalDate startDate, LocalDate endDate) {
        // 查询该时间段内的所有跑步记录
        List<RunningRecord> records = getRunningRecordsBetween(startDate, endDate);
        
        RunningReportResponse response = new RunningReportResponse();
        response.setStartDate(startDate.format(FORMATTER));
        response.setEndDate(endDate.format(FORMATTER));
        
        if (records.isEmpty()) {
            initializeEmptyReport(response);
            return response;
        }
        
        // 计算总跑步人数（去重）
        Set<Long> uniqueRunners = records.stream()
                .map(RunningRecord::getUserId)
                .collect(Collectors.toSet());
        response.setTotalRunners(uniqueRunners.size());
        
        // 计算总跑步次数
        response.setTotalRunCount(records.size());
        
        // 计算总跑步距离
        BigDecimal totalDistance = records.stream()
                .map(RunningRecord::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalDistance(totalDistance);
        
        // 计算平均跑步距离
        response.setAvgDistance(totalDistance.divide(new BigDecimal(records.size()), 2, RoundingMode.HALF_UP));
        
        // 计算平均每人跑步次数
        response.setAvgRunCountPerPerson(new BigDecimal(records.size()).divide(new BigDecimal(uniqueRunners.size()), 2, RoundingMode.HALF_UP));
        
        // 计算日均跑步人数
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));
        response.setAvgDailyRunners(new BigDecimal(uniqueRunners.size()).divide(new BigDecimal(daysBetween), 2, RoundingMode.HALF_UP));
        
        // 获取每日跑步人数统计
        response.setDailyRunnerCount(getDailyRunningCount(startDate, endDate));
        
        // 获取距离分布统计
        response.setDistanceDistribution(getDistanceDistribution(startDate, endDate));
        
        // 获取排行榜数据
        response.setDistanceRanking(getDistanceRanking(startDate, endDate, 10));
        response.setFrequencyRanking(getFrequencyRanking(startDate, endDate, 10));
        response.setPaceRanking(getBestPaceRanking(startDate, endDate, 10));
        
        return response;
    }

    @Override
    public Map<String, Integer> getDailyRunningCount(LocalDate startDate, LocalDate endDate) {
        List<RunningRecord> records = getRunningRecordsBetween(startDate, endDate);
        
        Map<String, Integer> result = new LinkedHashMap<>();
        
        // 初始化日期范围内所有日期的计数为0
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            result.put(currentDate.format(FORMATTER), 0);
            currentDate = currentDate.plusDays(1);
        }
        
        // 统计每日跑步人数
        Map<String, Set<Long>> dailyRunners = new HashMap<>();
        
        for (RunningRecord record : records) {
            String dateStr = record.getRecordDate().format(FORMATTER);
            dailyRunners.computeIfAbsent(dateStr, k -> new HashSet<>())
                    .add(record.getUserId());
        }
        
        // 更新结果
        for (Map.Entry<String, Set<Long>> entry : dailyRunners.entrySet()) {
            result.put(entry.getKey(), entry.getValue().size());
        }
        
        return result;
    }

    @Override
    public Map<String, Integer> getDistanceDistribution(LocalDate startDate, LocalDate endDate) {
        List<RunningRecord> records = getRunningRecordsBetween(startDate, endDate);
        
        // 定义距离区间
        String[] ranges = {"0-1km", "1-3km", "3-5km", "5-10km", "10km+"};
        Map<String, Integer> result = new LinkedHashMap<>();
        
        // 初始化计数为0
        for (String range : ranges) {
            result.put(range, 0);
        }
        
        // 统计每个距离区间的记录数
        for (RunningRecord record : records) {
            BigDecimal distance = record.getDistance();
            
            if (distance.compareTo(BigDecimal.ONE) <= 0) {
                result.put("0-1km", result.get("0-1km") + 1);
            } else if (distance.compareTo(new BigDecimal("3")) <= 0) {
                result.put("1-3km", result.get("1-3km") + 1);
            } else if (distance.compareTo(new BigDecimal("5")) <= 0) {
                result.put("3-5km", result.get("3-5km") + 1);
            } else if (distance.compareTo(new BigDecimal("10")) <= 0) {
                result.put("5-10km", result.get("5-10km") + 1);
            } else {
                result.put("10km+", result.get("10km+") + 1);
            }
        }
        
        return result;
    }
    
    /**
     * 查询指定日期范围内的跑步记录
     */
    private List<RunningRecord> getRunningRecordsBetween(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<RunningRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(RunningRecord::getRecordDate, startDate);
        queryWrapper.le(RunningRecord::getRecordDate, endDate);
        
        return runningRecordMapper.selectList(queryWrapper);
    }
    
    /**
     * 构建排名响应
     */
    private List<RunningRankingResponse> buildRankingResponse(List<Map.Entry<Long, BigDecimal>> entries, Map<Long, Integer> runCountMap, boolean isDistance) {
        // 收集所有用户ID
        Set<Long> userIds = entries.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        
        // 查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        }
        
        // 构建排名响应
        List<RunningRankingResponse> result = new ArrayList<>();
        
        int rank = 1;
        for (Map.Entry<Long, BigDecimal> entry : entries) {
            Long userId = entry.getKey();
            User user = userMap.get(userId);
            
            if (user == null) continue;
            
            RunningRankingResponse ranking = RunningRankingResponse.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .avatar(user.getAvatar())
                    .totalDistance(entry.getValue().setScale(2, RoundingMode.HALF_UP))
                    .runCount(runCountMap.getOrDefault(userId, 0))
                    .build();
            
            result.add(ranking);
        }
        
        return result;
    }
    
    /**
     * 构建次数排名响应
     */
    private List<RunningRankingResponse> buildRankingResponseForFrequency(List<Map.Entry<Long, Integer>> entries, Map<Long, BigDecimal> distanceMap) {
        // 收集所有用户ID
        Set<Long> userIds = entries.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        
        // 查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        }
        
        // 构建排名响应
        List<RunningRankingResponse> result = new ArrayList<>();
        
        int rank = 1;
        for (Map.Entry<Long, Integer> entry : entries) {
            Long userId = entry.getKey();
            User user = userMap.get(userId);
            
            if (user == null) continue;
            
            RunningRankingResponse ranking = RunningRankingResponse.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .avatar(user.getAvatar())
                    .totalDistance(distanceMap.getOrDefault(userId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                    .runCount(entry.getValue())
                    .build();
            
            result.add(ranking);
        }
        
        return result;
    }
    
    /**
     * 构建配速排名响应
     */
    private List<RunningRankingResponse> buildRankingResponseForPace(List<Map.Entry<Long, String>> entries, Map<Long, BigDecimal> distanceMap, Map<Long, Integer> runCountMap) {
        // 收集所有用户ID
        Set<Long> userIds = entries.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        
        // 查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        }
        
        // 构建排名响应
        List<RunningRankingResponse> result = new ArrayList<>();
        
        int rank = 1;
        for (Map.Entry<Long, String> entry : entries) {
            Long userId = entry.getKey();
            User user = userMap.get(userId);
            
            if (user == null) continue;
            
            RunningRankingResponse ranking = RunningRankingResponse.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .avatar(user.getAvatar())
                    .totalDistance(distanceMap.getOrDefault(userId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                    .runCount(runCountMap.getOrDefault(userId, 0))
                    .bestPace(entry.getValue())
                    .build();
            
            result.add(ranking);
        }
        
        return result;
    }
    
    /**
     * 比较两个配速字符串（格式：分钟:秒/公里）
     * 返回负数表示pace1更快，返回正数表示pace2更快，返回0表示相等
     */
    private int comparePace(String pace1, String pace2) {
        // 解析配速格式
        String[] parts1 = pace1.split(":");
        String[] parts2 = pace2.split(":");
        
        // 获取分钟部分
        int minutes1 = Integer.parseInt(parts1[0]);
        int minutes2 = Integer.parseInt(parts2[0]);
        
        if (minutes1 != minutes2) {
            return minutes1 - minutes2;
        }
        
        // 如果分钟相同，比较秒数
        int seconds1 = Integer.parseInt(parts1[1].split("/")[0]);
        int seconds2 = Integer.parseInt(parts2[1].split("/")[0]);
        
        return seconds1 - seconds2;
    }
    
    /**
     * 初始化空报表
     */
    private void initializeEmptyReport(RunningReportResponse response) {
        response.setTotalRunners(0);
        response.setTotalRunCount(0);
        response.setTotalDistance(BigDecimal.ZERO);
        response.setAvgDistance(BigDecimal.ZERO);
        response.setAvgRunCountPerPerson(BigDecimal.ZERO);
        response.setAvgDailyRunners(BigDecimal.ZERO);
        response.setDailyRunnerCount(new HashMap<>());
        response.setDistanceDistribution(new HashMap<>());
        response.setDistanceRanking(new ArrayList<>());
        response.setFrequencyRanking(new ArrayList<>());
        response.setPaceRanking(new ArrayList<>());
    }
} 