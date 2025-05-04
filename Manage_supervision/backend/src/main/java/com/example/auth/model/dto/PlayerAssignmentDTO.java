package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlayerAssignmentDTO {
    private Long companionId;
    private List<Long> playerIds;
} 