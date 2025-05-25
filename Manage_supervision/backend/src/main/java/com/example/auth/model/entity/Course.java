package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("courses")
public class Course {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("course_name")
    private String courseName;
    
    private String description;
    
    @TableField("teacher_id")
    private Long teacherId;
    
    private Integer duration;
    
    private String category;
    
    @TableField("cover_image")
    private String coverImage;
    
    private String status;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private User teacher;
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public User getTeacher() {
        return teacher;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
} 