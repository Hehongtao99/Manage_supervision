package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.CompanionPlayerDTO;
import com.example.auth.model.dto.CompanionWithPlayersDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.model.entity.CompanionPlayerRelation;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.CompanionPlayerMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.CompanionPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanionPlayerServiceImpl implements CompanionPlayerService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private CompanionPlayerMapper companionPlayerMapper;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResponse<UserDTO> getAllCompanions(int page, int size, String keyword) {
        // 获取陪玩角色
        Role companionRole = roleMapper.findByName("SUPERVISOR");
        if (companionRole == null) {
            throw new RuntimeException("陪玩角色不存在");
        }
        
        // 创建MyBatis-Plus分页对象，注意MyBatis-Plus是从0开始
        Page<User> pageParam = new Page<>(page - 1, size);
        
        // 使用UserMapper中的方法进行分页查询
        IPage<User> resultPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            resultPage = userMapper.findByConditions(
                    pageParam, 
                    keyword, 
                    "SUPERVISOR", 
                    null);
        } else {
            resultPage = userMapper.findByRoleIdPage(pageParam, companionRole.getId());
        }
        
        List<UserDTO> companionDTOs = resultPage.getRecords().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(companionDTOs, resultPage.getTotal(), page, size);
    }

    @Override
    public PageResponse<UserDTO> getAllPlayers(int page, int size, String keyword) {
        // 获取玩家角色
        Role playerRole = roleMapper.findByName("USER");
        if (playerRole == null) {
            throw new RuntimeException("玩家角色不存在");
        }
        
        // 创建MyBatis-Plus分页对象
        Page<User> pageParam = new Page<>(page - 1, size);
        
        // 使用UserMapper中的方法进行分页查询
        IPage<User> resultPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            resultPage = userMapper.findByConditions(
                    pageParam, 
                    keyword, 
                    "USER", 
                    null);
        } else {
            resultPage = userMapper.findByRoleIdPage(pageParam, playerRole.getId());
        }
        
        List<UserDTO> playerDTOs = resultPage.getRecords().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(playerDTOs, resultPage.getTotal(), page, size);
    }

    @Override
    public List<UserDTO> getUnassignedPlayers() {
        // 获取玩家角色ID
        Role playerRole = roleMapper.findByName("USER");
        if (playerRole == null) {
            throw new RuntimeException("玩家角色不存在");
        }
        
        // 查询未分配给任何陪玩的玩家ID
        List<Long> unassignedPlayerIds = companionPlayerMapper.findUnassignedPlayerIdsByRoleId(playerRole.getId());
        
        if (unassignedPlayerIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据ID查询玩家详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, unassignedPlayerIds);
        List<User> unassignedPlayers = userMapper.selectList(queryWrapper);
        
        return unassignedPlayers.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getPlayersByCompanion(Long companionId) {
        User companion = userMapper.selectById(companionId);
        if (companion == null) {
            return new ArrayList<>();
        }
        
        // 获取该陪玩的所有活跃玩家ID
        List<Long> playerIds = companionPlayerMapper.findActivePlayerIdsByCompanionId(companionId);
        
        if (playerIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询玩家详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, playerIds);
        List<User> players = userMapper.selectList(queryWrapper);
        
        return players.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean assignPlayersToCompanion(Long companionId, List<Long> playerIds) {
        User companion = userMapper.selectById(companionId);
        if (companion == null) {
            return false;
        }
        
        // 验证所有玩家ID是否有效
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, playerIds);
        long count = userMapper.selectCount(queryWrapper);
        
        if (count != playerIds.size()) {
            return false;
        }
        
        for (Long playerId : playerIds) {
            // 检查该玩家是否已分配给陪玩
            boolean exists = companionPlayerMapper.existsByCompanionIdAndPlayerId(companionId, playerId);
            if (!exists) {
                CompanionPlayerRelation relation = new CompanionPlayerRelation();
                relation.setCompanionId(companionId);
                relation.setPlayerId(playerId);
                relation.setStatus("active");
                relation.setAssignTime(LocalDateTime.now());
                companionPlayerMapper.insert(relation);
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean unassignPlayer(Long companionId, Long playerId) {
        // 检查陪玩和玩家是否存在
        User companion = userMapper.selectById(companionId);
        User player = userMapper.selectById(playerId);
        
        if (companion == null || player == null) {
            return false;
        }
        
        // 查询关系记录
        CompanionPlayerRelation relation = companionPlayerMapper.findByCompanionIdAndPlayerId(companionId, playerId);
        
        if (relation != null) {
            relation.setStatus("inactive");
            companionPlayerMapper.updateById(relation);
            return true;
        }
        
        return false;
    }

    @Override
    public CompanionWithPlayersDTO getCompanionWithPlayers(Long companionId) {
        User companion = userMapper.selectById(companionId);
        if (companion == null) {
            return null;
        }
        
        // 获取该陪玩的所有活跃玩家ID
        List<Long> playerIds = companionPlayerMapper.findActivePlayerIdsByCompanionId(companionId);
        
        // 查询玩家详细信息
        List<User> players = new ArrayList<>();
        if (!playerIds.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(User::getId, playerIds);
            players = userMapper.selectList(queryWrapper);
        }
        
        CompanionWithPlayersDTO dto = new CompanionWithPlayersDTO();
        dto.setId(companion.getId());
        dto.setUsername(companion.getUsername());
        dto.setRealName(companion.getRealName());
        dto.setUserNumber(companion.getUserNumber());
        dto.setEmail(companion.getEmail());
        dto.setPhone(companion.getPhone());
        dto.setPlayers(players.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    public CompanionPlayerDTO getRelationDetail(Long relationId) {
        CompanionPlayerRelation relation = companionPlayerMapper.selectById(relationId);
        if (relation == null) {
            return null;
        }
        
        // 补充陪玩和玩家信息
        User companion = userMapper.selectById(relation.getCompanionId());
        User player = userMapper.selectById(relation.getPlayerId());
        
        relation.setCompanion(companion);
        relation.setPlayer(player);
        
        return convertToCompanionPlayerDTO(relation);
    }

    @Override
    public boolean isCompanionAssignedToPlayer(Long companionId, Long playerId) {
        return companionPlayerMapper.existsByCompanionIdAndPlayerId(companionId, playerId);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setUserNumber(user.getUserNumber());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().format(formatter) : null);
        return dto;
    }

    private CompanionPlayerDTO convertToCompanionPlayerDTO(CompanionPlayerRelation relation) {
        CompanionPlayerDTO dto = new CompanionPlayerDTO();
        dto.setId(relation.getId());
        dto.setCompanionId(relation.getCompanionId());
        dto.setPlayerId(relation.getPlayerId());
        
        if (relation.getCompanion() != null) {
            dto.setCompanionName(relation.getCompanion().getRealName());
            dto.setCompanionUserNumber(relation.getCompanion().getUserNumber());
        }
        
        if (relation.getPlayer() != null) {
            dto.setPlayerName(relation.getPlayer().getRealName());
            dto.setPlayerUserNumber(relation.getPlayer().getUserNumber());
        }
        
        dto.setAssignTime(relation.getAssignTime() != null ? relation.getAssignTime().format(formatter) : null);
        dto.setStatus(relation.getStatus());
        
        return dto;
    }
} 