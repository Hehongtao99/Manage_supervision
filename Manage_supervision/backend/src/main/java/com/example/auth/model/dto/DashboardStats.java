package com.example.auth.model.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStats {
    private Long totalUsers;
    private Map<String, Long> roleDistribution;
    private SystemInfo systemInfo;
    private List<UserInfo> userList;

    @Data
    public static class SystemInfo {
        private String javaVersion;
        private String osName;
        private String osVersion;
        private Long totalMemory;
        private Long freeMemory;
        private Integer availableProcessors;
    }

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private List<String> roles;
        private String createTime;
    }
} 