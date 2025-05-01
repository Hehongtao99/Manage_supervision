package com.example.auth.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页响应DTO
 */
@Data
public class PageResponseDTO<T> {
    private List<T> data;
    private Long total;
    private Integer page;
    private Integer pageSize;
    
    public PageResponseDTO(List<T> data, Long total, Integer page, Integer pageSize) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
} 