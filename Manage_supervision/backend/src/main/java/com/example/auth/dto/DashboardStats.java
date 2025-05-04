package com.example.auth.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStats {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalAnalysts;
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
        private String systemStatus;
    }

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private List<String> roles;
        private String createTime;
    }
} 