package com.example.auth.service;

import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RunnerSupervisorDTO;
import com.example.auth.model.dto.SupervisorWithRunnersDTO;
import com.example.auth.model.dto.UserDTO;

import java.util.List;

public interface RunnerSupervisorService {
    
    // 获取所有管理员列表（带分页）
    PageResponse<UserDTO> getAllSupervisors(int page, int size, String keyword);
    
    // 获取所有跑步爱好者列表（带分页）
    PageResponse<UserDTO> getAllRunners(int page, int size, String keyword);
    
    // 获取未分配给任何管理员的跑步爱好者列表
    List<UserDTO> getUnassignedRunners();
    
    // 获取特定管理员的跑步爱好者列表
    List<UserDTO> getRunnersBySupervisor(Long supervisorId);
    
    // 批量分配跑步爱好者给管理员
    boolean assignRunnersToSupervisor(Long supervisorId, List<Long> runnerIds);
    
    // 取消分配跑步爱好者给管理员
    boolean unassignRunner(Long supervisorId, Long runnerId);
    
    // 获取特定管理员（包含其跑步爱好者列表）
    SupervisorWithRunnersDTO getSupervisorWithRunners(Long supervisorId);
    
    // 获取管理员-跑步爱好者关系详情
    RunnerSupervisorDTO getRelationDetail(Long relationId);
    
    // 判断管理员是否分配给了指定跑步爱好者
    boolean isSupervisorAssignedToRunner(Long supervisorId, Long runnerId);
} 