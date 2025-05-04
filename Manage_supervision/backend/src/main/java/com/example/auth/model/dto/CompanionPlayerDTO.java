package com.example.auth.model.dto;

import lombok.Data;

@Data
public class CompanionPlayerDTO {
    private Long id;
    private Long companionId;
    private String companionName;
    private String companionUserNumber;
    private Long playerId;
    private String playerName;
    private String playerUserNumber;
    private String assignTime;
    private String status;
} 