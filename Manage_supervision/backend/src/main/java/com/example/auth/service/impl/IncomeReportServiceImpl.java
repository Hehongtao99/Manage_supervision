package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.IncomeRecordMapper;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.model.dto.TeacherCourseIncomeDTO;
import com.example.auth.model.dto.TeacherIncomeStatsDTO;
import com.example.auth.model.dto.TeacherIncomeTrendDTO;
import com.example.auth.model.entity.IncomeRecord;
import com.example.auth.model.entity.Order;
import com.example.auth.service.CourseApplicationService;
import com.example.auth.service.IncomeReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收入报表服务实现类
 */
@Service
public class IncomeReportServiceImpl implements IncomeReportService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private IncomeRecordMapper incomeRecordMapper;

    @Autowired
    private CourseApplicationService courseApplicationService;

    /**
     * 格式化金额，保留两位小数
     * @param amount 原始金额
     * @return 格式化后的金额
     */
    private double formatAmount(double amount) {
        // 保留两位小数
        return Math.round(amount * 100) / 100.0;
    }

    @Override
    public TeacherIncomeTrendDTO getIncomeTrend(Long teacherId, String period) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();
        List<String> dateLabels = new ArrayList<>();
        DateTimeFormatter formatter;

        // 根据周期确定起始日期和格式化器
        switch (period) {
            case "week":
                // 近7天
                startDate = endDate.minusDays(6);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                // 生成日期标签
                for (int i = 0; i < 7; i++) {
                    dateLabels.add(startDate.plusDays(i).format(formatter));
                }
                break;
            case "month":
                // 近30天
                startDate = endDate.minusDays(29);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                // 生成日期标签
                for (int i = 0; i < 30; i++) {
                    dateLabels.add(startDate.plusDays(i).format(formatter));
                }
                break;
            case "year":
                // 近12个月
                startDate = endDate.minusMonths(11).withDayOfMonth(1);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                
                // 生成月份标签
                LocalDateTime iterDate = startDate;
                while (!iterDate.isAfter(endDate)) {
                    dateLabels.add(iterDate.format(formatter));
                    iterDate = iterDate.plusMonths(1);
                }
                break;
            default:
                // 默认近30天
                startDate = endDate.minusDays(29);
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                // 生成日期标签
                for (int i = 0; i < 30; i++) {
                    dateLabels.add(startDate.plusDays(i).format(formatter));
                }
        }

        // 查询指定时间段内的收入记录
        LambdaQueryWrapper<IncomeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IncomeRecord::getTeacherId, teacherId)
                .ge(IncomeRecord::getCreateTime, startDate)
                .le(IncomeRecord::getCreateTime, endDate);

        List<IncomeRecord> records = incomeRecordMapper.selectList(queryWrapper);

        // 统计每日/每月收入
        Map<String, Double> incomeByDate = new HashMap<>();
        
        // 初始化收入为0
        for (String date : dateLabels) {
            incomeByDate.put(date, 0.0);
        }

        // 累计收入
        for (IncomeRecord record : records) {
            String dateKey;
            if ("year".equals(period)) {
                dateKey = record.getCreateTime().format(formatter);
            } else {
                dateKey = record.getCreateTime().format(formatter);
            }

            if (incomeByDate.containsKey(dateKey)) {
                double currentAmount = incomeByDate.get(dateKey);
                double recordAmount = record.getAmount().doubleValue();
                incomeByDate.put(dateKey, currentAmount + recordAmount);
            }
        }

        // 构建返回结果
        TeacherIncomeTrendDTO result = new TeacherIncomeTrendDTO();
        result.setDates(dateLabels);
        
        List<Double> amounts = new ArrayList<>();
        for (String date : dateLabels) {
            amounts.add(formatAmount(incomeByDate.get(date)));
        }
        result.setAmounts(amounts);

        return result;
    }

    @Override
    public List<TeacherCourseIncomeDTO> getCourseIncomeDistribution(Long teacherId, String period) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        // 根据周期确定起始日期
        switch (period) {
            case "month":
                // 当月数据
                startDate = endDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "year":
                // 当年数据
                startDate = endDate.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "all":
                // 所有时间
                startDate = LocalDateTime.of(2000, 1, 1, 0, 0); // 设置一个很早的日期
                break;
            default:
                // 默认当月
                startDate = endDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        // 查询指定时间段内的收入记录
        LambdaQueryWrapper<IncomeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IncomeRecord::getTeacherId, teacherId)
                .ge(IncomeRecord::getCreateTime, startDate)
                .le(IncomeRecord::getCreateTime, endDate);

        List<IncomeRecord> records = incomeRecordMapper.selectList(queryWrapper);

        // 按课程ID分组统计收入
        Map<Long, Double> incomeByCourseName = new HashMap<>();
        Map<Long, String> courseNames = new HashMap<>();

        for (IncomeRecord record : records) {
            // 获取关联的订单信息，以获取课程ID
            Order order = orderMapper.selectById(record.getOrderId());
            if (order == null) continue;
            
            Long courseId = order.getCourseId();
            double recordAmount = record.getAmount().doubleValue();
            
            // 累加课程收入
            incomeByCourseName.put(courseId, incomeByCourseName.getOrDefault(courseId, 0.0) + recordAmount);
            
            // 保存课程名称
            if (!courseNames.containsKey(courseId)) {
                // 如果订单中没有课程标题，则使用"课程ID-X"作为显示名称
                String courseName = order.getCourseTitle();
                if (courseName == null || courseName.isEmpty()) {
                    if (courseId == 3) {
                        courseName = "数学课程";
                    } else if (courseId == 4) {
                        courseName = "物理课程";
                    } else {
                        courseName = "课程ID-" + courseId;
                    }
                }
                courseNames.put(courseId, courseName);
            }
        }

        // 构建返回结果
        List<TeacherCourseIncomeDTO> result = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : incomeByCourseName.entrySet()) {
            TeacherCourseIncomeDTO dto = new TeacherCourseIncomeDTO();
            dto.setCourseName(courseNames.get(entry.getKey()));
            dto.setAmount(formatAmount(entry.getValue()));
            result.add(dto);
        }

        // 按金额降序排序
        result.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        
        return result;
    }

    @Override
    public TeacherIncomeStatsDTO getIncomeStats(Long teacherId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 当月起始日期
        LocalDateTime thisMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // 上月起始和结束日期
        LocalDateTime lastMonthStart = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthEnd = now.withDayOfMonth(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);

        // 1. 计算总收入
        LambdaQueryWrapper<IncomeRecord> totalQueryWrapper = new LambdaQueryWrapper<>();
        totalQueryWrapper.eq(IncomeRecord::getTeacherId, teacherId);

        List<IncomeRecord> allRecords = incomeRecordMapper.selectList(totalQueryWrapper);
        double totalIncome = allRecords.stream()
                .mapToDouble(record -> record.getAmount().doubleValue())
                .sum();

        // 2. 计算当月收入
        LambdaQueryWrapper<IncomeRecord> thisMonthQueryWrapper = new LambdaQueryWrapper<>();
        thisMonthQueryWrapper.eq(IncomeRecord::getTeacherId, teacherId)
                .ge(IncomeRecord::getCreateTime, thisMonthStart)
                .le(IncomeRecord::getCreateTime, now);

        List<IncomeRecord> thisMonthRecords = incomeRecordMapper.selectList(thisMonthQueryWrapper);
        double thisMonthIncome = thisMonthRecords.stream()
                .mapToDouble(record -> record.getAmount().doubleValue())
                .sum();

        // 3. 计算上月收入
        LambdaQueryWrapper<IncomeRecord> lastMonthQueryWrapper = new LambdaQueryWrapper<>();
        lastMonthQueryWrapper.eq(IncomeRecord::getTeacherId, teacherId)
                .ge(IncomeRecord::getCreateTime, lastMonthStart)
                .le(IncomeRecord::getCreateTime, lastMonthEnd);

        List<IncomeRecord> lastMonthRecords = incomeRecordMapper.selectList(lastMonthQueryWrapper);
        double lastMonthIncome = lastMonthRecords.stream()
                .mapToDouble(record -> record.getAmount().doubleValue())
                .sum();

        // 4. 获取课程收入分布（当月）
        List<TeacherCourseIncomeDTO> courseIncomeDistribution = getCourseIncomeDistribution(teacherId, "month");

        // 构建返回结果
        TeacherIncomeStatsDTO result = new TeacherIncomeStatsDTO();
        result.setTotalIncome(formatAmount(totalIncome));
        result.setThisMonthIncome(formatAmount(thisMonthIncome));
        result.setLastMonthIncome(formatAmount(lastMonthIncome));
        result.setCourseIncomeDistribution(courseIncomeDistribution);

        return result;
    }
} 