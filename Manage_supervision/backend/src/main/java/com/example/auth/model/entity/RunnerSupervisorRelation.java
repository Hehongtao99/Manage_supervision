package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("runner_supervisor_relations")
public class RunnerSupervisorRelation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("supervisor_id")
    private Long supervisorId;

    @TableField("runner_id")
    private Long runnerId;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("status")
    private String status;

    @TableField(exist = false)
    private User supervisor;

    @TableField(exist = false)
    private User runner;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public Long getRunnerId() {
        return runnerId;
    }

    public User getSupervisor() {
        return supervisor;
    }

    public User getRunner() {
        return runner;
    }

    public LocalDateTime getAssignTime() {
        return assignTime;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public void setRunnerId(Long runnerId) {
        this.runnerId = runnerId;
    }

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
        if (supervisor != null) {
            this.supervisorId = supervisor.getId();
        }
    }

    public void setRunner(User runner) {
        this.runner = runner;
        if (runner != null) {
            this.runnerId = runner.getId();
        }
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 