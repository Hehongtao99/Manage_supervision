package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页结果DTO
 */
@Data
public class PageResult<T> {
    
    /**
     * 当前页数据
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 总页数
     */
    private Long pages;
    
    /**
     * 默认构造函数
     */
    public PageResult() {
    }
    
    /**
     * 构造函数
     * 
     * @param records 当前页数据
     * @param total 总记录数
     * @param size 每页大小
     * @param current 当前页码
     * @param pages 总页数
     */
    public PageResult(List<T> records, Long total, Long size, Long current, Long pages) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = pages;
    }
} 