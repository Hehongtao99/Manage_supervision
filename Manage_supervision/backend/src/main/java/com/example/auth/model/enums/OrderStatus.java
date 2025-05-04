package com.example.auth.model.enums;

public enum OrderStatus {
    PENDING("待接单"),
    ACCEPTED("已接单"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    REFUND_PENDING("退款待审核"),
    REFUNDED("已退款");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 