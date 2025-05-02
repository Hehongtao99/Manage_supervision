package com.example.auth.model.dto;

import java.io.Serializable;

/**
 * 学生活动的数据传输对象
 */
public class ActivityDTO implements Serializable {
    private String type; // 活动类型: success, info, warning, error
    private String title; // 活动标题
    private String content; // 活动内容
    private String time; // 活动时间
    
    // 添加在UserServiceImpl中使用的字段
    private Long id;
    private String name;
    private int duration;
    private int points;

    public ActivityDTO() {
    }

    public ActivityDTO(String type, String title, String content, String time) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    // 添加在UserServiceImpl中使用的getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
} 