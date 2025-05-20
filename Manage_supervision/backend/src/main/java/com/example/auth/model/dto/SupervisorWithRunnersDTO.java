package com.example.auth.model.dto;

import java.util.List;

public class SupervisorWithRunnersDTO {
    private Long id;
    private String username;
    private String realName;
    private String userNumber;
    private String email;
    private String phone;
    private List<UserDTO> runners;

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

    public String getUserNumber() {
        return userNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public List<UserDTO> getRunners() {
        return runners;
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

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRunners(List<UserDTO> runners) {
        this.runners = runners;
    }
} 