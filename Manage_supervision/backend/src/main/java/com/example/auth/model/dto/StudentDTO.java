package com.example.auth.model.dto;

import java.io.Serializable;

/**
 * 学生基本信息的数据传输对象
 */
public class StudentDTO implements Serializable {
    private Long id; // 用户ID
    private String name; // 学生姓名(使用真实姓名或用户名)
    private String studentId; // 学号(暂时与用户名一致)
    private String className; // 班级
    private String email; // 邮箱
    private String phone; // 电话
    private String status; // 状态(active/inactive)
    private String lastLogin; // 最近登录时间
    private int progress; // 总体学习进度(0-100)
    
    // 添加在UserServiceImpl中使用的字段
    private String username;
    private String realName;
    private String userNumber;
    private String createTime;
    private int attendanceRate;
    private int homeworkCompleteRate;
    private int averageScore;

    public StudentDTO() {
    }

    // 主要构造函数
    public StudentDTO(Long id, String name, String studentId, String className, 
                      String email, String phone, String status, String lastLogin, int progress) {
        this.id = id;
        this.name = name;
        this.studentId = studentId;
        this.className = className;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.lastLogin = lastLogin;
        this.progress = progress;
    }

    // Getters and Setters
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    // 添加在UserServiceImpl中使用的getter和setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(int attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public int getHomeworkCompleteRate() {
        return homeworkCompleteRate;
    }

    public void setHomeworkCompleteRate(int homeworkCompleteRate) {
        this.homeworkCompleteRate = homeworkCompleteRate;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(int averageScore) {
        this.averageScore = averageScore;
    }
} 