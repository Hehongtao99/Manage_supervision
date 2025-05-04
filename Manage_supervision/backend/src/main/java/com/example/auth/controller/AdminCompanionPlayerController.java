package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.PlayerAssignmentDTO;
import com.example.auth.model.dto.CompanionWithPlayersDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.CompanionPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminCompanionPlayerController {

    @Autowired
    private CompanionPlayerService companionPlayerService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    // 获取所有陪玩列表（带分页）
    @GetMapping("/companions")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<UserDTO>> getAllCompanions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<UserDTO> response = companionPlayerService.getAllCompanions(page, size, keyword);
        return ResponseEntity.ok(response);
    }

    // 获取所有玩家列表（带分页）
    @GetMapping("/players")
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<UserDTO>> getAllPlayers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<UserDTO> response = companionPlayerService.getAllPlayers(page, size, keyword);
        return ResponseEntity.ok(response);
    }

    // 获取未分配玩家列表
    @GetMapping("/players/unassigned")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getUnassignedPlayers() {
        List<UserDTO> players = companionPlayerService.getUnassignedPlayers();
        return ResponseEntity.ok(players);
    }

    // 获取特定陪玩的玩家列表
    @GetMapping("/companions/{companionId}/players")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getPlayersByCompanion(@PathVariable Long companionId) {
        List<UserDTO> players = companionPlayerService.getPlayersByCompanion(companionId);
        return ResponseEntity.ok(players);
    }
    
    // 获取特定玩家的陪玩列表
    @GetMapping("/companions/player/{playerId}")
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getCompanionsByPlayer(@PathVariable Long playerId) {
        User player = userMapper.selectById(playerId);
        if (player == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        Role companionRole = roleMapper.findByName("SUPERVISOR");
        if (companionRole == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        // 获取与该玩家相关的陪玩
        List<User> companions = userMapper.findByRoleId(companionRole.getId());
        
        // 转换为DTO并过滤出已经分配给该玩家的陪玩
        List<UserDTO> companionDTOs = new ArrayList<>();
        for (User companion : companions) {
            if (companionPlayerService.isCompanionAssignedToPlayer(companion.getId(), playerId)) {
                UserDTO companionDTO = new UserDTO();
                companionDTO.setId(companion.getId());
                companionDTO.setUsername(companion.getUsername());
                companionDTO.setRealName(companion.getRealName());
                companionDTO.setUserNumber(companion.getUserNumber());
                companionDTO.setEmail(companion.getEmail());
                companionDTO.setPhone(companion.getPhone());
                companionDTOs.add(companionDTO);
            }
        }
        
        return ResponseEntity.ok(companionDTOs);
    }

    // 分配玩家给陪玩
    @PostMapping("/companions/assign-players")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> assignPlayersToCompanion(@RequestBody PlayerAssignmentDTO assignmentDTO) {
        boolean success = companionPlayerService.assignPlayersToCompanion(
                assignmentDTO.getCompanionId(), assignmentDTO.getPlayerIds());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "玩家分配成功" : "玩家分配失败");
        
        return ResponseEntity.ok(response);
    }

    // 取消分配玩家
    @PostMapping("/companions/{companionId}/unassign/{playerId}")
    @RequireRole("ADMIN")
    public ResponseEntity<Map<String, Object>> unassignPlayer(
            @PathVariable Long companionId, @PathVariable Long playerId) {
        
        boolean success = companionPlayerService.unassignPlayer(companionId, playerId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "取消分配成功" : "取消分配失败");
        
        return ResponseEntity.ok(response);
    }

    // 获取陪玩详情（包含玩家列表）
    @GetMapping("/companions/{companionId}/details")
    @RequireRole("ADMIN")
    public ResponseEntity<CompanionWithPlayersDTO> getCompanionDetails(@PathVariable Long companionId) {
        CompanionWithPlayersDTO companion = companionPlayerService.getCompanionWithPlayers(companionId);
        return ResponseEntity.ok(companion);
    }
} 