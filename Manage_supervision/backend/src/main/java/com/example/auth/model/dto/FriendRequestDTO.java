package com.example.auth.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友请求DTO
 */
@Data
public class FriendRequestDTO {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String message;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 发送请求的用户信息
    private UserDTO fromUser;
    
    // 接收请求的用户信息
    private UserDTO toUser;
} 