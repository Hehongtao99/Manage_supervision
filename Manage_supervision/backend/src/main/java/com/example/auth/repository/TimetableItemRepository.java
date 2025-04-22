package com.example.auth.repository;

import com.example.auth.entity.TimetableItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableItemRepository extends JpaRepository<TimetableItem, Long> {
    List<TimetableItem> findByTimetableId(Long timetableId);
    List<TimetableItem> findByTimetableIdAndDayOfWeek(Long timetableId, Integer dayOfWeek);
    void deleteByTimetableId(Long timetableId);
} 