package com.example.auth.dto;

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
} 