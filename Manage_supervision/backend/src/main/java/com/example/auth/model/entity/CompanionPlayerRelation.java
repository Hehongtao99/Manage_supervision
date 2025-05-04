package com.example.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("companion_player_relations")
public class CompanionPlayerRelation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("companion_id")
    private Long companionId;

    @TableField("player_id")
    private Long playerId;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("status")
    private String status;

    @TableField(exist = false)
    private User companion;

    @TableField(exist = false)
    private User player;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public User getCompanion() {
        return companion;
    }

    public User getPlayer() {
        return player;
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

    public void setCompanionId(Long companionId) {
        this.companionId = companionId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public void setCompanion(User companion) {
        this.companion = companion;
        if (companion != null) {
            this.companionId = companion.getId();
        }
    }

    public void setPlayer(User player) {
        this.player = player;
        if (player != null) {
            this.playerId = player.getId();
        }
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 