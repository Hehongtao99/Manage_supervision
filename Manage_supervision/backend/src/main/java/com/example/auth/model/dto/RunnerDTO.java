package com.example.auth.model.dto;

public class RunnerDTO {
    private Long id;
    private String username;
    private String realName;
    private String name;
    private String userNumber;
    private String runnerId;
    private String email;
    private String phone;
    private String status;
    private String createTime;

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getName() {
        return name;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public String getRunnerId() {
        return runnerId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public String getCreateTime() {
        return createTime;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public void setRunnerId(String runnerId) {
        this.runnerId = runnerId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
} 