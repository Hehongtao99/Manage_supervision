package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("attendance_records")
public class AttendanceRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("attendance_id")
    private Long attendanceId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("check_in_time")
    private LocalDateTime checkInTime;
    
    private String status;
    
    private String location;
    
    private String notes;
    
    @TableField("face_verified")
    private Boolean faceVerified;
    
    @TableField("similarity")
    private Float similarity;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public String getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }

    public Boolean getFaceVerified() {
        return faceVerified;
    }
    
    public Float getSimilarity() {
        return similarity;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setFaceVerified(Boolean faceVerified) {
        this.faceVerified = faceVerified;
    }
    
    public void setSimilarity(Float similarity) {
        this.similarity = similarity;
    }
} 