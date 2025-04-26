package com.example.auth.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStats {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalRoles;
    private String systemStatus;
    private Long totalStudents;
    private Long totalTeachers;
    private Long totalClasses;
    private Long totalNotifications;
    private Map<String, Long> roleDistribution;
    private Map<String, Long> classDistribution;
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