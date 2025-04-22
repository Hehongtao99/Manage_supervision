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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        dto.setStartTime(item.getStartTime());
        dto.setEndTime(item.getEndTime());
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