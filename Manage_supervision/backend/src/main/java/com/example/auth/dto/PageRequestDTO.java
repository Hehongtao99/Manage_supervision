package com.example.auth.dto;

import lombok.Data;

/**
 * 分页请求DTO
 */
@Data
public class PageRequestDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String query;
    private String sortBy;
    private String sortOrder = "asc";
} 