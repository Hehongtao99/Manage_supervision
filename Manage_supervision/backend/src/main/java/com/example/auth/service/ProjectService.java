package com.example.auth.service;

import com.example.auth.dto.ProjectDTO;
import com.example.auth.entity.Project;

import java.util.List;

/**
 * 课题服务接口
 */
public interface ProjectService {
    
    /**
     * 创建课题
     */
    ProjectDTO createProject(ProjectDTO projectDTO);
    
    /**
     * 更新课题
     */
    ProjectDTO updateProject(Long id, ProjectDTO projectDTO);
    
    /**
     * 获取课题详情
     */
    ProjectDTO getProjectById(Long id);
    
    /**
     * 删除课题
     */
    boolean deleteProject(Long id);
    
    /**
     * 获取督导创建的所有课题
     */
    List<ProjectDTO> getProjectsBySupervisor(Long supervisorId);
    
    /**
     * 获取督导创建的指定状态的课题
     */
    List<ProjectDTO> getProjectsBySupervisorAndStatus(Long supervisorId, String status);
    
    /**
     * 获取分配给学生的所有课题
     */
    List<ProjectDTO> getProjectsByAssignee(Long assigneeId);
    
    /**
     * 获取分配给学生的指定状态的课题
     */
    List<ProjectDTO> getProjectsByAssigneeAndStatus(Long assigneeId, String status);
    
    /**
     * 根据类别获取课题
     */
    List<ProjectDTO> getProjectsByCategory(String category);
    
    /**
     * 将课题分配给学生
     */
    ProjectDTO assignProjectToStudent(Long projectId, Long studentId);
    
    /**
     * 更新课题状态
     */
    ProjectDTO updateProjectStatus(Long projectId, String status);
    
    /**
     * 根据任务完成情况更新项目状态
     * 如果项目下没有任何任务有文件提交，则项目状态变为"未开始"
     * 如果项目下有部分任务有文件提交，则项目状态为"进行中"
     * 如果项目下所有任务都有文件提交，则项目状态变为"已完成"
     * 
     * @param projectId 项目ID
     * @return 更新后的项目DTO
     */
    ProjectDTO updateProjectStatusBasedOnTasks(Long projectId);
    
    /**
     * 学生申请课题
     * 学生可以自主申请课题，申请后的课题状态为"未审核"
     * 
     * @param projectDTO 课题申请信息
     * @return 申请的课题DTO
     */
    ProjectDTO applyForProject(ProjectDTO projectDTO);
    
    /**
     * 督导员审核课题申请
     * 督导员审核学生的课题申请，可以通过或拒绝
     * 通过后状态变为"进行中"，拒绝后状态变为"审核未通过"
     * 
     * @param projectId 课题ID
     * @param approved 是否通过审核
     * @return 审核后的课题DTO
     */
    ProjectDTO reviewProjectApplication(Long projectId, boolean approved);
    
    /**
     * 获取待审核的课题申请列表
     * 
     * @param supervisorId 督导员ID
     * @return 待审核的课题申请列表
     */
    List<ProjectDTO> getPendingReviewProjects(Long supervisorId);
} 