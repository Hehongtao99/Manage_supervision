package com.example.auth.repository;

import com.example.auth.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByClassId(Long classId);
    List<Timetable> findByClassIdAndWeekNumber(Long classId, Integer weekNumber);
} 