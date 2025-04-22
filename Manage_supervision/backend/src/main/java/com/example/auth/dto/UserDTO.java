package com.example.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String email;
    private String phone;
    private List<String> roles;
    private String status;
    private String createTime;
    private String userNumber;
}