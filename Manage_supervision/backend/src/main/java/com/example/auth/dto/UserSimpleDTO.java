package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 简单的用户数据传输对象，用于避免序列化问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleDTO {
    private Long id;
    private String username;
    private String realName; // 真实姓名
    private String nickname; // 昵称
    private String userNumber; // 学号/工号
} 