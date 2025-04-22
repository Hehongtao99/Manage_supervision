package com.example.auth.service;

import com.example.auth.dto.TimetableDTO;
import com.example.auth.dto.TimetableItemDTO;

import java.util.List;
import java.util.Map;

public interface TimetableService {
    TimetableDTO createTimetable(TimetableDTO timetableDTO);
    TimetableDTO updateTimetable(Long id, TimetableDTO timetableDTO);
    void deleteTimetable(Long id);
    TimetableDTO getTimetableById(Long id);
    List<TimetableDTO> getTimetablesByClassId(Long classId);
    List<TimetableDTO> getTimetablesByClassIdAndWeekNumber(Long classId, Integer weekNumber);
    
    TimetableItemDTO createTimetableItem(TimetableItemDTO timetableItemDTO);
    TimetableItemDTO updateTimetableItem(Long id, TimetableItemDTO timetableItemDTO);
    void deleteTimetableItem(Long id);
    List<TimetableItemDTO> getTimetableItemsByTimetableId(Long timetableId);
    
    Map<String, Object> calculateTimeSlot(String periodType, Integer periodNumber);
} 