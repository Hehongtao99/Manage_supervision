package com.example.auth.dto;

import java.time.LocalTime;

public class TimetableItemDTO {
    private Long id;
    private Long timetableId;
    private Long teacherId;
    private String teacherName;
    private String courseName;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String periodType;
    private Integer periodNumber;
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

    public String getTeacherName() {
        return teacherName;
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

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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