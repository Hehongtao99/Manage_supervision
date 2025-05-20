package com.example.auth.model.dto;

public class RunnerSupervisorDTO {
    private Long id;
    private Long supervisorId;
    private String supervisorName;
    private Long runnerId;
    private String runnerName;
    private String status;
    private String assignTime;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public Long getRunnerId() {
        return runnerId;
    }

    public String getRunnerName() {
        return runnerName;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignTime() {
        return assignTime;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public void setRunnerId(Long runnerId) {
        this.runnerId = runnerId;
    }

    public void setRunnerName(String runnerName) {
        this.runnerName = runnerName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAssignTime(String assignTime) {
        this.assignTime = assignTime;
    }
} 