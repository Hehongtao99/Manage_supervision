package com.example.auth.controller;

import com.example.auth.dto.TimetableDTO;
import com.example.auth.dto.TimetableItemDTO;
import com.example.auth.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timetables")
public class TimetableController {

    private final TimetableService timetableService;

    @Autowired
    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @PostMapping
    public ResponseEntity<TimetableDTO> createTimetable(@RequestBody TimetableDTO timetableDTO) {
        TimetableDTO createdTimetable = timetableService.createTimetable(timetableDTO);
        return new ResponseEntity<>(createdTimetable, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimetableDTO> updateTimetable(@PathVariable Long id, @RequestBody TimetableDTO timetableDTO) {
        TimetableDTO updatedTimetable = timetableService.updateTimetable(id, timetableDTO);
        return ResponseEntity.ok(updatedTimetable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimetable(@PathVariable Long id) {
        timetableService.deleteTimetable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimetableDTO> getTimetableById(@PathVariable Long id) {
        TimetableDTO timetableDTO = timetableService.getTimetableById(id);
        return ResponseEntity.ok(timetableDTO);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<TimetableDTO>> getTimetablesByClassId(@PathVariable Long classId) {
        List<TimetableDTO> timetables = timetableService.getTimetablesByClassId(classId);
        return ResponseEntity.ok(timetables);
    }

    @GetMapping("/class/{classId}/week/{weekNumber}")
    public ResponseEntity<List<TimetableDTO>> getTimetablesByClassIdAndWeekNumber(
            @PathVariable Long classId,
            @PathVariable Integer weekNumber) {
        List<TimetableDTO> timetables = timetableService.getTimetablesByClassIdAndWeekNumber(classId, weekNumber);
        return ResponseEntity.ok(timetables);
    }

    @PostMapping("/items")
    public ResponseEntity<TimetableItemDTO> createTimetableItem(@RequestBody TimetableItemDTO itemDTO) {
        TimetableItemDTO createdItem = timetableService.createTimetableItem(itemDTO);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<TimetableItemDTO> updateTimetableItem(@PathVariable Long id, @RequestBody TimetableItemDTO itemDTO) {
        TimetableItemDTO updatedItem = timetableService.updateTimetableItem(id, itemDTO);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteTimetableItem(@PathVariable Long id) {
        timetableService.deleteTimetableItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{timetableId}/items")
    public ResponseEntity<List<TimetableItemDTO>> getTimetableItemsByTimetableId(@PathVariable Long timetableId) {
        List<TimetableItemDTO> items = timetableService.getTimetableItemsByTimetableId(timetableId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/calculate-time")
    public ResponseEntity<Map<String, Object>> calculateTimeSlot(
            @RequestParam String periodType,
            @RequestParam Integer periodNumber) {
        Map<String, Object> timeSlot = timetableService.calculateTimeSlot(periodType, periodNumber);
        return ResponseEntity.ok(timeSlot);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 