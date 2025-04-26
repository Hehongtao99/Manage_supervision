package com.example.auth.service;

import com.example.auth.dto.TimetableDTO;
import com.example.auth.dto.TimetableItemDTO;

import java.util.List;
import java.util.Map;

/**
 * 课表服务接口
 */
public interface TimetableService {
    
    /**
     * 创建课表
     */
    TimetableDTO createTimetable(TimetableDTO timetableDTO);
    
    /**
     * 更新课表
     */
    TimetableDTO updateTimetable(Long id, TimetableDTO timetableDTO);
    
    /**
     * 删除课表
     */
    void deleteTimetable(Long id);
    
    /**
     * 根据ID获取课表
     */
    TimetableDTO getTimetableById(Long id);
    
    /**
     * 根据班级ID获取课表
     */
    List<TimetableDTO> getTimetablesByClassId(Long classId);
    
    /**
     * 根据班级ID和周次获取课表
     */
    List<TimetableDTO> getTimetablesByClassIdAndWeekNumber(Long classId, Integer weekNumber);
    
    /**
     * 创建课表项目
     */
    TimetableItemDTO createTimetableItem(TimetableItemDTO itemDTO);
    
    /**
     * 更新课表项目
     */
    TimetableItemDTO updateTimetableItem(Long id, TimetableItemDTO itemDTO);
    
    /**
     * 删除课表项目
     */
    void deleteTimetableItem(Long id);
    
    /**
     * 根据课表ID获取所有课表项目
     */
    List<TimetableItemDTO> getTimetableItemsByTimetableId(Long timetableId);
    
    /**
     * 计算时间段
     */
    Map<String, Object> calculateTimeSlot(String periodType, Integer periodNumber);
    
    /**
     * 获取班级最新课表及其课程项
     * @param classId 班级ID
     * @return 课表及课程项列表
     */
    List<Map<String, Object>> getLatestTimetableWithItemsByClassId(Long classId);
} 