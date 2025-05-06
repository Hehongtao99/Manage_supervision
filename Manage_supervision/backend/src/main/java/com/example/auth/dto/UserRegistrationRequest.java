package com.example.auth.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private String nickname;
} 