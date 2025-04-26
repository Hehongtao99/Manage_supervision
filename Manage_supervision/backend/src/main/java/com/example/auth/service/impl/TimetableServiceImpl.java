package com.example.auth.service.impl;

import com.example.auth.dto.TimetableDTO;
import com.example.auth.dto.TimetableItemDTO;
import com.example.auth.entity.Timetable;
import com.example.auth.entity.TimetableItem;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.TimetableItemRepository;
import com.example.auth.repository.TimetableRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final TimetableItemRepository timetableItemRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    @Autowired
    public TimetableServiceImpl(
            TimetableRepository timetableRepository,
            TimetableItemRepository timetableItemRepository,
            ClassRepository classRepository,
            UserRepository userRepository) {
        this.timetableRepository = timetableRepository;
        this.timetableItemRepository = timetableItemRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TimetableDTO createTimetable(TimetableDTO timetableDTO) {
        // 检查班级是否存在
        classRepository.findById(timetableDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("班级不存在"));

        Timetable timetable = new Timetable();
        timetable.setClassId(timetableDTO.getClassId());
        timetable.setWeekNumber(timetableDTO.getWeekNumber());
        timetable.setName(timetableDTO.getName());

        timetable = timetableRepository.save(timetable);
        return convertToDTO(timetable);
    }

    @Override
    public TimetableDTO updateTimetable(Long id, TimetableDTO timetableDTO) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程表不存在"));

        timetable.setName(timetableDTO.getName());
        timetable.setWeekNumber(timetableDTO.getWeekNumber());

        timetable = timetableRepository.save(timetable);
        return convertToDTO(timetable);
    }

    @Override
    @Transactional
    public void deleteTimetable(Long id) {
        // 首先删除相关的课程表项目
        timetableItemRepository.deleteByTimetableId(id);
        // 然后删除课程表
        timetableRepository.deleteById(id);
    }

    @Override
    public TimetableDTO getTimetableById(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程表不存在"));
        return convertToDTO(timetable);
    }

    @Override
    public List<TimetableDTO> getTimetablesByClassId(Long classId) {
        List<Timetable> timetables = timetableRepository.findByClassId(classId);
        return timetables.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimetableDTO> getTimetablesByClassIdAndWeekNumber(Long classId, Integer weekNumber) {
        List<Timetable> timetables = timetableRepository.findByClassIdAndWeekNumber(classId, weekNumber);
        return timetables.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TimetableItemDTO createTimetableItem(TimetableItemDTO itemDTO) {
        // 检查课程表是否存在
        timetableRepository.findById(itemDTO.getTimetableId())
                .orElseThrow(() -> new RuntimeException("课程表不存在"));

        // 计算开始时间和结束时间
        Map<String, Object> timeSlot = calculateTimeSlot(itemDTO.getPeriodType(), itemDTO.getPeriodNumber());
        LocalTime startTime = (LocalTime) timeSlot.get("startTime");
        LocalTime endTime = (LocalTime) timeSlot.get("endTime");

        TimetableItem item = new TimetableItem();
        item.setTimetableId(itemDTO.getTimetableId());
        item.setTeacherId(itemDTO.getTeacherId());
        item.setCourseName(itemDTO.getCourseName());
        item.setDayOfWeek(itemDTO.getDayOfWeek());
        item.setStartTime(startTime);
        item.setEndTime(endTime);
        item.setPeriodType(itemDTO.getPeriodType());
        item.setPeriodNumber(itemDTO.getPeriodNumber());
        item.setClassroom(itemDTO.getClassroom());

        item = timetableItemRepository.save(item);
        return convertToItemDTO(item);
    }

    @Override
    public TimetableItemDTO updateTimetableItem(Long id, TimetableItemDTO itemDTO) {
        TimetableItem item = timetableItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程表项目不存在"));

        // 计算开始时间和结束时间，如果周期类型或周期号码已更改
        if (!item.getPeriodType().equals(itemDTO.getPeriodType()) || 
            !item.getPeriodNumber().equals(itemDTO.getPeriodNumber())) {
            Map<String, Object> timeSlot = calculateTimeSlot(itemDTO.getPeriodType(), itemDTO.getPeriodNumber());
            LocalTime startTime = (LocalTime) timeSlot.get("startTime");
            LocalTime endTime = (LocalTime) timeSlot.get("endTime");
            
            item.setStartTime(startTime);
            item.setEndTime(endTime);
        }

        item.setTeacherId(itemDTO.getTeacherId());
        item.setCourseName(itemDTO.getCourseName());
        item.setDayOfWeek(itemDTO.getDayOfWeek());
        item.setPeriodType(itemDTO.getPeriodType());
        item.setPeriodNumber(itemDTO.getPeriodNumber());
        item.setClassroom(itemDTO.getClassroom());

        item = timetableItemRepository.save(item);
        return convertToItemDTO(item);
    }

    @Override
    public void deleteTimetableItem(Long id) {
        timetableItemRepository.deleteById(id);
    }

    @Override
    public List<TimetableItemDTO> getTimetableItemsByTimetableId(Long timetableId) {
        List<TimetableItem> items = timetableItemRepository.findByTimetableId(timetableId);
        return items.stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> calculateTimeSlot(String periodType, Integer periodNumber) {
        LocalTime startTime = null;
        LocalTime endTime = null;

        // 每节课45分钟，休息10分钟
        switch (periodType) {
            case "morning":
                // 上午从8:00开始
                startTime = LocalTime.of(8, 0).plusMinutes((periodNumber - 1) * 55);
                endTime = startTime.plusMinutes(45);
                break;
            case "afternoon":
                // 下午从13:30开始
                startTime = LocalTime.of(13, 30).plusMinutes((periodNumber - 1) * 55);
                endTime = startTime.plusMinutes(45);
                break;
            case "evening":
                // 晚上从18:30开始
                startTime = LocalTime.of(18, 30).plusMinutes((periodNumber - 1) * 55);
                endTime = startTime.plusMinutes(45);
                break;
            default:
                throw new RuntimeException("无效的时间段类型");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        return result;
    }

    /**
     * 获取班级最新课表及其课程项
     */
    @Override
    public List<Map<String, Object>> getLatestTimetableWithItemsByClassId(Long classId) {
        // 获取班级所有课表
        List<Timetable> timetables = timetableRepository.findByClassId(classId);
        
        if (timetables == null || timetables.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 按周次降序排序，获取最新的课表
        timetables.sort((t1, t2) -> {
            Integer w1 = t1.getWeekNumber();
            Integer w2 = t2.getWeekNumber();
            return w2.compareTo(w1); // 降序排序
        });
        
        Timetable latestTimetable = timetables.get(0);
        List<TimetableItem> items = timetableItemRepository.findByTimetableId(latestTimetable.getId());
        
        if (items == null || items.isEmpty()) {
            // 如果没有课程项，则返回课表基本信息作为课程
            // 这样前端至少能显示课表名称
            List<Map<String, Object>> timetableInfo = new ArrayList<>();
            for (Timetable timetable : timetables) {
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("id", timetable.getId());
                courseInfo.put("name", timetable.getName());
                courseInfo.put("courseName", timetable.getName());
                courseInfo.put("weekNumber", timetable.getWeekNumber());
                courseInfo.put("time", "未设置时间");
                courseInfo.put("location", "未设置地点");
                courseInfo.put("teacherName", "未知教师");
                timetableInfo.add(courseInfo);
            }
            return timetableInfo;
        }
        
        return items.stream()
            .map(item -> {
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("id", item.getId());
                courseInfo.put("courseName", item.getCourseName());
                // 确保前端能正确识别的字段
                courseInfo.put("name", item.getCourseName());
                courseInfo.put("dayOfWeek", getDayOfWeekName(item.getDayOfWeek()));
                courseInfo.put("startTime", item.getStartTime().toString());
                courseInfo.put("endTime", item.getEndTime().toString());
                courseInfo.put("time", getDayOfWeekName(item.getDayOfWeek()) + " " + 
                               item.getStartTime().toString() + "-" + item.getEndTime().toString());
                courseInfo.put("classroom", item.getClassroom());
                courseInfo.put("location", item.getClassroom() != null ? item.getClassroom() : "未设置地点");
                
                // 如果有教师信息，也添加
                if (item.getTeacherId() != null) {
                    userRepository.findById(item.getTeacherId())
                        .ifPresent(teacher -> {
                            String teacherName = teacher.getRealName() != null ? 
                                teacher.getRealName() : teacher.getUsername();
                            courseInfo.put("teacherName", teacherName);
                            courseInfo.put("teacherId", teacher.getId());
                        });
                } else {
                    courseInfo.put("teacherName", "未知教师");
                }
                
                return courseInfo;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 将星期几数字转换为名称
     */
    private String getDayOfWeekName(Integer dayOfWeek) {
        if (dayOfWeek == null) return "";
        
        switch (dayOfWeek) {
            case 1: return "周一";
            case 2: return "周二";
            case 3: return "周三";
            case 4: return "周四";
            case 5: return "周五";
            case 6: return "周六";
            case 7: return "周日";
            default: return "未知";
        }
    }

    private TimetableDTO convertToDTO(Timetable timetable) {
        TimetableDTO dto = new TimetableDTO();
        dto.setId(timetable.getId());
        dto.setClassId(timetable.getClassId());
        dto.setWeekNumber(timetable.getWeekNumber());
        dto.setName(timetable.getName());
        dto.setCreateTime(timetable.getCreateTime());
        dto.setUpdateTime(timetable.getUpdateTime());

        // 获取班级名称
        classRepository.findById(timetable.getClassId())
                .ifPresent(clazz -> dto.setClassName(clazz.getClassName()));

        return dto;
    }

    private TimetableItemDTO convertToItemDTO(TimetableItem item) {
        TimetableItemDTO dto = new TimetableItemDTO();
        dto.setId(item.getId());
        dto.setTimetableId(item.getTimetableId());
        dto.setTeacherId(item.getTeacherId());
        dto.setCourseName(item.getCourseName());
        dto.setDayOfWeek(item.getDayOfWeek());
        dto.setStartTime(item.getStartTime().toString());
        dto.setEndTime(item.getEndTime().toString());
        dto.setPeriodType(item.getPeriodType());
        dto.setPeriodNumber(item.getPeriodNumber());
        dto.setClassroom(item.getClassroom());

        // 获取教师姓名
        if (item.getTeacherId() != null) {
            userRepository.findById(item.getTeacherId())
                    .ifPresent(user -> dto.setTeacherName(user.getRealName()));
        }

        return dto;
    }
} 