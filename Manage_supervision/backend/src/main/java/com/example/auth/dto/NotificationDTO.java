package com.example.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private Long senderId;
    private String senderName;
    private String createTime;
    private List<Long> recipientIds;
} 