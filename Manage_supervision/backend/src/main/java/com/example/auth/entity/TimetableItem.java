package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "timetable_items")
public class TimetableItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timetable_id", nullable = false)
    private Long timetableId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;  // 1-7 对应星期一到星期日

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "period_type", nullable = false)
    private String periodType;  // 'morning', 'afternoon', 'evening'

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;  // 1-4 对应每个时间段的第几节课

    @Column(name = "classroom")
    private String classroom;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getTimetableId() {
        return timetableId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getCourseName() {
        return courseName;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getPeriodType() {
        return periodType;
    }

    public Integer getPeriodNumber() {
        return periodNumber;
    }

    public String getClassroom() {
        return classroom;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTimetableId(Long timetableId) {
        this.timetableId = timetableId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public void setPeriodNumber(Integer periodNumber) {
        this.periodNumber = periodNumber;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
} 