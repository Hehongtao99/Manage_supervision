package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.CompanionPlayerDTO;
import com.example.auth.model.dto.CompanionWithPlayersDTO;
import com.example.auth.model.dto.UserDTO;

import java.util.List;

public interface CompanionPlayerService {
    
    // 获取所有陪玩列表（带分页）
    PageResponse<UserDTO> getAllCompanions(int page, int size, String keyword);
    
    // 获取所有玩家列表（带分页）
    PageResponse<UserDTO> getAllPlayers(int page, int size, String keyword);
    
    // 获取未分配给任何陪玩的玩家列表
    List<UserDTO> getUnassignedPlayers();
    
    // 获取特定陪玩的玩家列表
    List<UserDTO> getPlayersByCompanion(Long companionId);
    
    // 批量分配玩家给陪玩
    boolean assignPlayersToCompanion(Long companionId, List<Long> playerIds);
    
    // 取消分配玩家给陪玩
    boolean unassignPlayer(Long companionId, Long playerId);
    
    // 获取特定陪玩（包含其玩家列表）
    CompanionWithPlayersDTO getCompanionWithPlayers(Long companionId);
    
    // 获取陪玩-玩家关系详情
    CompanionPlayerDTO getRelationDetail(Long relationId);
    
    // 判断陪玩是否分配给了指定玩家
    boolean isCompanionAssignedToPlayer(Long companionId, Long playerId);
} 