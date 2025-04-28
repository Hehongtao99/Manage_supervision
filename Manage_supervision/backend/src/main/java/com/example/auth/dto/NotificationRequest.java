package com.example.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationRequest {
    private String title;
    private String content;
    private List<Long> recipientIds;
} 