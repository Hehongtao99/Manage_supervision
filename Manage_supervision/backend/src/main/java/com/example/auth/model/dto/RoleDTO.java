package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private List<String> permissions;
    private String createTime;
} 