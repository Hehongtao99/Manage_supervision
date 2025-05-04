package com.example.auth.model.enums;

public enum OperatorType {
    PLAYER("玩家"),
    COMPANION("陪玩"),
    ADMIN("管理员"),
    SYSTEM("系统");
    
    private final String description;
    
    OperatorType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 