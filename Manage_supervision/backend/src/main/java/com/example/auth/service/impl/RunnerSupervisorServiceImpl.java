package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RunnerSupervisorDTO;
import com.example.auth.model.dto.SupervisorWithRunnersDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.RunnerSupervisorRelation;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.RunnerSupervisorMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.RunnerSupervisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RunnerSupervisorServiceImpl implements RunnerSupervisorService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RunnerSupervisorMapper runnerSupervisorMapper;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResponse<UserDTO> getAllSupervisors(int page, int size, String keyword) {
        // 获取管理员角色
        Role supervisorRole = roleMapper.findByName("SUPERVISOR");
        if (supervisorRole == null) {
            throw new RuntimeException("管理员角色不存在");
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
            resultPage = userMapper.findByRoleIdPage(pageParam, supervisorRole.getId());
        }
        
        List<UserDTO> supervisorDTOs = resultPage.getRecords().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(supervisorDTOs, resultPage.getTotal(), page, size);
    }

    @Override
    public PageResponse<UserDTO> getAllRunners(int page, int size, String keyword) {
        // 获取跑步爱好者角色
        Role runnerRole = roleMapper.findByName("USER");
        if (runnerRole == null) {
            throw new RuntimeException("跑步爱好者角色不存在");
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
            resultPage = userMapper.findByRoleIdPage(pageParam, runnerRole.getId());
        }
        
        List<UserDTO> runnerDTOs = resultPage.getRecords().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(runnerDTOs, resultPage.getTotal(), page, size);
    }

    @Override
    public List<UserDTO> getUnassignedRunners() {
        // 获取跑步爱好者角色ID
        Role runnerRole = roleMapper.findByName("USER");
        if (runnerRole == null) {
            throw new RuntimeException("跑步爱好者角色不存在");
        }
        
        // 查询未分配给任何管理员的跑步爱好者ID
        List<Long> unassignedRunnerIds = runnerSupervisorMapper.findUnassignedRunnerIdsByRoleId(runnerRole.getId());
        
        if (unassignedRunnerIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据ID查询跑步爱好者详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, unassignedRunnerIds);
        List<User> unassignedRunners = userMapper.selectList(queryWrapper);
        
        return unassignedRunners.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getRunnersBySupervisor(Long supervisorId) {
        User supervisor = userMapper.selectById(supervisorId);
        if (supervisor == null) {
            return new ArrayList<>();
        }
        
        // 获取该管理员的所有活跃跑步爱好者ID
        List<Long> runnerIds = runnerSupervisorMapper.findActiveRunnerIdsBySupervisorId(supervisorId);
        
        if (runnerIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询跑步爱好者详细信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, runnerIds);
        List<User> runners = userMapper.selectList(queryWrapper);
        
        return runners.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean assignRunnersToSupervisor(Long supervisorId, List<Long> runnerIds) {
        User supervisor = userMapper.selectById(supervisorId);
        if (supervisor == null) {
            return false;
        }
        
        // 验证所有跑步爱好者ID是否有效
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, runnerIds);
        long count = userMapper.selectCount(queryWrapper);
        
        if (count != runnerIds.size()) {
            return false;
        }
        
        for (Long runnerId : runnerIds) {
            // 检查该跑步爱好者是否已分配给管理员
            boolean exists = runnerSupervisorMapper.existsBySupervisorIdAndRunnerId(supervisorId, runnerId);
            if (!exists) {
                RunnerSupervisorRelation relation = new RunnerSupervisorRelation();
                relation.setSupervisorId(supervisorId);
                relation.setRunnerId(runnerId);
                relation.setStatus("active");
                relation.setAssignTime(LocalDateTime.now());
                runnerSupervisorMapper.insert(relation);
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean unassignRunner(Long supervisorId, Long runnerId) {
        // 检查管理员和跑步爱好者是否存在
        User supervisor = userMapper.selectById(supervisorId);
        User runner = userMapper.selectById(runnerId);
        
        if (supervisor == null || runner == null) {
            return false;
        }
        
        // 查询关系记录
        RunnerSupervisorRelation relation = runnerSupervisorMapper.findBySupervisorIdAndRunnerId(supervisorId, runnerId);
        
        if (relation != null) {
            relation.setStatus("inactive");
            runnerSupervisorMapper.updateById(relation);
            return true;
        }
        
        return false;
    }

    @Override
    public SupervisorWithRunnersDTO getSupervisorWithRunners(Long supervisorId) {
        User supervisor = userMapper.selectById(supervisorId);
        if (supervisor == null) {
            return null;
        }
        
        // 获取该管理员的所有活跃跑步爱好者ID
        List<Long> runnerIds = runnerSupervisorMapper.findActiveRunnerIdsBySupervisorId(supervisorId);
        
        // 查询跑步爱好者详细信息
        List<User> runners = new ArrayList<>();
        if (!runnerIds.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(User::getId, runnerIds);
            runners = userMapper.selectList(queryWrapper);
        }
        
        SupervisorWithRunnersDTO dto = new SupervisorWithRunnersDTO();
        dto.setId(supervisor.getId());
        dto.setUsername(supervisor.getUsername());
        dto.setRealName(supervisor.getRealName());
        dto.setUserNumber(supervisor.getUserNumber());
        dto.setEmail(supervisor.getEmail());
        dto.setPhone(supervisor.getPhone());
        dto.setRunners(runners.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    public RunnerSupervisorDTO getRelationDetail(Long relationId) {
        RunnerSupervisorRelation relation = runnerSupervisorMapper.selectById(relationId);
        if (relation == null) {
            return null;
        }
        
        // 补充管理员和跑步爱好者信息
        User supervisor = userMapper.selectById(relation.getSupervisorId());
        User runner = userMapper.selectById(relation.getRunnerId());
        
        relation.setSupervisor(supervisor);
        relation.setRunner(runner);
        
        return convertToRunnerSupervisorDTO(relation);
    }

    @Override
    public boolean isSupervisorAssignedToRunner(Long supervisorId, Long runnerId) {
        // 检查管理员和跑步爱好者是否存在
        User supervisor = userMapper.selectById(supervisorId);
        User runner = userMapper.selectById(runnerId);
        
        if (supervisor == null || runner == null) {
            return false;
        }
        
        // 查询是否存在活跃的管理员-跑步爱好者关系
        return runnerSupervisorMapper.existsBySupervisorIdAndRunnerId(supervisorId, runnerId);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setUserNumber(user.getUserNumber());
        dto.setAvatar(user.getAvatar());
        return dto;
    }

    private RunnerSupervisorDTO convertToRunnerSupervisorDTO(RunnerSupervisorRelation relation) {
        RunnerSupervisorDTO dto = new RunnerSupervisorDTO();
        dto.setId(relation.getId());
        
        if (relation.getSupervisor() != null) {
            dto.setSupervisorId(relation.getSupervisor().getId());
            dto.setSupervisorName(relation.getSupervisor().getRealName() != null 
                    ? relation.getSupervisor().getRealName() 
                    : relation.getSupervisor().getUsername());
        } else {
            dto.setSupervisorId(relation.getSupervisorId());
        }
        
        if (relation.getRunner() != null) {
            dto.setRunnerId(relation.getRunner().getId());
            dto.setRunnerName(relation.getRunner().getRealName() != null 
                    ? relation.getRunner().getRealName() 
                    : relation.getRunner().getUsername());
        } else {
            dto.setRunnerId(relation.getRunnerId());
        }
        
        dto.setStatus(relation.getStatus());
        dto.setAssignTime(relation.getAssignTime() != null 
                ? relation.getAssignTime().format(formatter) 
                : null);
        
        return dto;
    }
} 