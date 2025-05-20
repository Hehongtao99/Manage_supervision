package com.example.auth.model.dto;

import java.util.List;

public class RunnerAssignmentDTO {
    private Long supervisorId;
    private List<Long> runnerIds;

    // Getters
    public Long getSupervisorId() {
        return supervisorId;
    }

    public List<Long> getRunnerIds() {
        return runnerIds;
    }

    // Setters
    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public void setRunnerIds(List<Long> runnerIds) {
        this.runnerIds = runnerIds;
    }
} 