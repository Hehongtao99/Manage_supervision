package com.example.auth.model.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果类
 * @param <T> 数据类型
 */
public class PageResult<T> implements Serializable {
    
    private List<T> records = Collections.emptyList(); // 记录列表
    private long total = 0;                           // 总记录数
    private long size = 10;                           // 每页显示条数
    private long current = 1;                         // 当前页
    private long pages = 0;                           // 总页数
    
    public PageResult() {
    }
    
    public List<T> getRecords() {
        return records;
    }
    
    public void setRecords(List<T> records) {
        this.records = records;
    }
    
    public long getTotal() {
        return total;
    }
    
    public void setTotal(long total) {
        this.total = total;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public long getCurrent() {
        return current;
    }
    
    public void setCurrent(long current) {
        this.current = current;
    }
    
    public long getPages() {
        return pages;
    }
    
    public void setPages(long pages) {
        this.pages = pages;
    }
    
    /**
     * 计算总页数
     */
    public void calculatePages() {
        if (this.size > 0) {
            this.pages = (this.total + this.size - 1) / this.size;
        } else {
            this.pages = 0;
        }
    }
} 