package com.example.auth.service.impl;

import com.example.auth.dto.ProjectDTO;
import com.example.auth.dto.TaskDTO;
import com.example.auth.entity.Project;
import com.example.auth.entity.User;
import com.example.auth.entity.Task;
import com.example.auth.repository.ProjectRepository;
import com.example.auth.repository.TaskRepository;
import com.example.auth.repository.TaskSubmissionRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ProjectService;
import com.example.auth.service.TaskService;
import com.example.auth.dto.TaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 课题服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Lazy
    @Autowired
    private TaskService taskService;

    /**
     * 创建课题
     */
    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        logger.info("创建课题: {}", projectDTO.getTitle());
        
        try {
            // 将DTO转换为实体
            Project project = projectDTO.toEntity();
            
            // 设置默认状态
            if (project.getStatus() == null) {
                project.setStatus("未开始");
            }
            
            // 保存课题
            Project savedProject = projectRepository.save(project);
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(project.getSupervisorId());
            String assigneeName = getUserNameById(project.getAssigneeId());
            
            // 返回创建的课题DTO
            return ProjectDTO.fromEntity(savedProject, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("创建课题失败", e);
            throw new RuntimeException("创建课题失败: " + e.getMessage());
        }
    }

    /**
     * 更新课题
     */
    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        logger.info("更新课题, ID: {}", id);
        
        try {
            // 查询课题
            Optional<Project> projectOptional = projectRepository.findById(id);
            if (!projectOptional.isPresent()) {
                throw new RuntimeException("课题不存在");
            }
            
            // 获取现有课题
            Project existingProject = projectOptional.get();
            
            // 更新属性
            Project updatedProject = projectDTO.toEntity();
            updatedProject.setId(id);
            
            // 保留原始的创建时间
            updatedProject.setCreateTime(existingProject.getCreateTime());
            
            // 保存更新后的课题
            Project savedProject = projectRepository.save(updatedProject);
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(savedProject.getSupervisorId());
            String assigneeName = getUserNameById(savedProject.getAssigneeId());
            
            // 返回更新后的课题DTO
            return ProjectDTO.fromEntity(savedProject, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("更新课题失败", e);
            throw new RuntimeException("更新课题失败: " + e.getMessage());
        }
    }

    /**
     * 获取课题详情
     */
    @Override
    public ProjectDTO getProjectById(Long id) {
        logger.info("获取课题详情, ID: {}", id);
        
        try {
            // 查询课题
            Optional<Project> projectOptional = projectRepository.findById(id);
            if (!projectOptional.isPresent()) {
                throw new RuntimeException("课题不存在");
            }
            
            Project project = projectOptional.get();
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(project.getSupervisorId());
            String assigneeName = getUserNameById(project.getAssigneeId());
            
            // 返回课题DTO
            return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("获取课题详情失败", e);
            throw new RuntimeException("获取课题详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除课题
     */
    @Override
    public boolean deleteProject(Long id) {
        logger.info("删除课题, ID: {}", id);
        
        try {
            // 查询课题
            if (!projectRepository.existsById(id)) {
                logger.warn("课题不存在, ID: {}", id);
                return false;
            }
            
            // 删除课题
            projectRepository.deleteById(id);
            logger.info("课题删除成功, ID: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("删除课题失败", e);
            return false;
        }
    }

    /**
     * 获取督导创建的所有课题
     */
    @Override
    public List<ProjectDTO> getProjectsBySupervisor(Long supervisorId) {
        logger.info("获取督导创建的所有课题, 督导ID: {}", supervisorId);
        
        try {
            // 查询课题
            List<Project> projects = projectRepository.findBySupervisorId(supervisorId);
            
            // 转换为DTO列表
            return projects.stream()
                .map(project -> {
                    String supervisorName = getUserNameById(project.getSupervisorId());
                    String assigneeName = getUserNameById(project.getAssigneeId());
                    return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取督导创建的课题失败", e);
            throw new RuntimeException("获取督导创建的课题失败: " + e.getMessage());
        }
    }

    /**
     * 获取督导创建的指定状态的课题
     */
    @Override
    public List<ProjectDTO> getProjectsBySupervisorAndStatus(Long supervisorId, String status) {
        logger.info("获取督导创建的指定状态的课题, 督导ID: {}, 状态: {}", supervisorId, status);
        
        try {
            // 查询课题
            List<Project> projects = projectRepository.findBySupervisorIdAndStatus(supervisorId, status);
            
            // 转换为DTO列表
            return projects.stream()
                .map(project -> {
                    String supervisorName = getUserNameById(project.getSupervisorId());
                    String assigneeName = getUserNameById(project.getAssigneeId());
                    return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取督导创建的指定状态的课题失败", e);
            throw new RuntimeException("获取督导创建的指定状态的课题失败: " + e.getMessage());
        }
    }

    /**
     * 获取分配给学生的所有课题
     */
    @Override
    public List<ProjectDTO> getProjectsByAssignee(Long assigneeId) {
        logger.info("获取分配给学生的所有课题, 学生ID: {}", assigneeId);
        
        try {
            // 查询课题
            List<Project> projects = projectRepository.findByAssigneeId(assigneeId);
            
            // 转换为DTO列表
            return projects.stream()
                .map(project -> {
                    String supervisorName = getUserNameById(project.getSupervisorId());
                    String assigneeName = getUserNameById(project.getAssigneeId());
                    return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取分配给学生的课题失败", e);
            throw new RuntimeException("获取分配给学生的课题失败: " + e.getMessage());
        }
    }

    /**
     * 获取分配给学生的指定状态的课题
     */
    @Override
    public List<ProjectDTO> getProjectsByAssigneeAndStatus(Long assigneeId, String status) {
        logger.info("获取分配给学生的指定状态的课题, 学生ID: {}, 状态: {}", assigneeId, status);
        
        try {
            // 查询课题
            List<Project> projects = projectRepository.findByAssigneeIdAndStatus(assigneeId, status);
            
            // 转换为DTO列表
            return projects.stream()
                .map(project -> {
                    String supervisorName = getUserNameById(project.getSupervisorId());
                    String assigneeName = getUserNameById(project.getAssigneeId());
                    return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取分配给学生的指定状态的课题失败", e);
            throw new RuntimeException("获取分配给学生的指定状态的课题失败: " + e.getMessage());
        }
    }

    /**
     * 根据类别获取课题
     */
    @Override
    public List<ProjectDTO> getProjectsByCategory(String category) {
        logger.info("根据类别获取课题, 类别: {}", category);
        
        try {
            // 查询课题
            List<Project> projects = projectRepository.findByCategory(category);
            
            // 转换为DTO列表
            return projects.stream()
                .map(project -> {
                    String supervisorName = getUserNameById(project.getSupervisorId());
                    String assigneeName = getUserNameById(project.getAssigneeId());
                    return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根据类别获取课题失败", e);
            throw new RuntimeException("根据类别获取课题失败: " + e.getMessage());
        }
    }

    /**
     * 将课题分配给学生
     */
    @Override
    public ProjectDTO assignProjectToStudent(Long projectId, Long studentId) {
        logger.info("将课题分配给学生, 课题ID: {}, 学生ID: {}", projectId, studentId);
        
        try {
            // 查询课题
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (!projectOptional.isPresent()) {
                throw new RuntimeException("课题不存在");
            }
            
            // 验证学生是否存在
            Optional<User> studentOptional = userRepository.findById(studentId);
            if (!studentOptional.isPresent()) {
                throw new RuntimeException("学生不存在");
            }
            
            Project project = projectOptional.get();
            
            // 设置分配的学生ID
            project.setAssigneeId(studentId);
            
            // 如果状态是未开始，则更新为进行中
            if ("未开始".equals(project.getStatus())) {
                project.setStatus("进行中");
            }
            
            // 保存更新
            Project savedProject = projectRepository.save(project);
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(savedProject.getSupervisorId());
            String assigneeName = getUserNameById(savedProject.getAssigneeId());
            
            // 返回更新后的课题DTO
            return ProjectDTO.fromEntity(savedProject, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("将课题分配给学生失败", e);
            throw new RuntimeException("将课题分配给学生失败: " + e.getMessage());
        }
    }

    /**
     * 更新课题状态
     */
    @Override
    public ProjectDTO updateProjectStatus(Long projectId, String status) {
        logger.info("更新课题状态, 课题ID: {}, 新状态: {}", projectId, status);
        
        try {
            // 查询课题
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (!projectOptional.isPresent()) {
                throw new RuntimeException("课题不存在");
            }
            
            // 验证状态值
            if (!isValidStatus(status)) {
                throw new RuntimeException("无效的状态值");
            }
            
            Project project = projectOptional.get();
            
            // 更新状态
            project.setStatus(status);
            
            // 如果状态是已完成，则设置结束时间为当前时间
            if ("已完成".equals(status) && project.getEndTime() == null) {
                project.setEndTime(LocalDateTime.now());
            }
            
            // 保存更新
            Project savedProject = projectRepository.save(project);
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(savedProject.getSupervisorId());
            String assigneeName = getUserNameById(savedProject.getAssigneeId());
            
            // 返回更新后的课题DTO
            return ProjectDTO.fromEntity(savedProject, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("更新课题状态失败", e);
            throw new RuntimeException("更新课题状态失败: " + e.getMessage());
        }
    }

    /**
     * 根据任务完成情况更新项目状态
     */
    @Override
    @Transactional
    public ProjectDTO updateProjectStatusBasedOnTasks(Long projectId) {
        logger.info("根据任务完成情况更新项目状态, 项目ID: {}", projectId);
        
        try {
            // 查询项目
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                throw new RuntimeException("项目不存在");
            }
            
            Project project = projectOpt.get();
            
            // 检查项目是否有任务
            List<Task> tasks = taskRepository.findByProjectId(projectId);
            long taskCount = tasks.size();
            
            if (taskCount == 0) {
                // 如果项目没有任务，则不更改状态
                logger.info("项目没有任务，不更改状态, 项目ID: {}", projectId);
                
                // 获取督导和学生的姓名
                String supervisorName = getUserNameById(project.getSupervisorId());
                String assigneeName = getUserNameById(project.getAssigneeId());
                
                // 返回项目DTO
                return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
            }
            
            // 使用JPA提供的方法检查项目的所有任务是否已完成
            boolean allTasksCompleted = taskRepository.areAllTasksCompletedForProject(projectId);
            logger.info("项目ID: {}, 所有任务是否已完成: {}", projectId, allTasksCompleted);
            
            // 计算有提交记录的任务数量
            long tasksWithSubmissions = 0;
            // 计算已完成的任务数量
            long completedTasksCount = 0;
            
            for (Task task : tasks) {
                if (taskSubmissionRepository.existsByTaskId(task.getId())) {
                    tasksWithSubmissions++;
                }
                if (Boolean.TRUE.equals(task.getCompleted())) {
                    completedTasksCount++;
                }
            }
            
            logger.info("项目ID: {}, 总任务数: {}, 有提交记录的任务数: {}, 已完成任务数: {}", 
                       projectId, taskCount, tasksWithSubmissions, completedTasksCount);
            
            // 根据任务完成情况更新项目状态
            String newStatus = null;
            String currentStatus = project.getStatus();
            
            if (completedTasksCount == 0) {
                // 没有任何任务已完成，状态为"未开始"
                newStatus = "未开始";
            } else if (completedTasksCount < taskCount) {
                // 部分任务已完成，状态为"进行中"
                newStatus = "进行中";
            } else if (completedTasksCount == taskCount) {
                // 所有任务都已完成，状态为"已完成"
                if (!"已结项".equals(currentStatus)) { // 如果已结项，不要改变状态
                    newStatus = "已完成";
                }
            }
            
            // 只有当状态需要变更时才更新
            if (newStatus != null && !newStatus.equals(currentStatus)) {
                project.setStatus(newStatus);
                project = projectRepository.save(project);
                logger.info("项目状态已更新为 '{}', 项目ID: {}", newStatus, projectId);
            } else {
                logger.info("项目状态保持不变 '{}', 项目ID: {}", currentStatus, projectId);
            }
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(project.getSupervisorId());
            String assigneeName = getUserNameById(project.getAssigneeId());
            
            // 返回更新后的项目DTO
            return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("根据任务完成情况更新项目状态失败", e);
            throw new RuntimeException("根据任务完成情况更新项目状态失败: " + e.getMessage());
        }
    }

    /**
     * 学生申请课题
     */
    @Override
    public ProjectDTO applyForProject(ProjectDTO projectDTO) {
        logger.info("学生申请课题: {}", projectDTO.getTitle());
        
        try {
            // 将DTO转换为实体
            Project project = projectDTO.toEntity();
            
            // 设置状态为未审核
            project.setStatus("未审核");
            
            // 保存课题
            Project savedProject = projectRepository.save(project);
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(project.getSupervisorId());
            String assigneeName = getUserNameById(project.getAssigneeId());
            
            // 检查是否包含任务计划
            TaskRequest taskRequest = projectDTO.getTaskRequest();
            if (taskRequest != null) {
                logger.info("课题申请包含任务计划，创建任务，课题ID: {}", savedProject.getId());
                
                // 调用TaskService创建任务
                taskService.createBatchTasksForProject(savedProject.getId(), taskRequest);
            }
            
            // 检查是否包含tasks数组
            List<TaskDTO> tasks = projectDTO.getTasks();
            if (tasks != null && !tasks.isEmpty()) {
                logger.info("课题申请包含tasks数组，创建任务，课题ID: {}, 任务数量: {}", savedProject.getId(), tasks.size());
                
                // 遍历并创建每个任务
                for (TaskDTO taskDTO : tasks) {
                    // 设置任务的项目ID、督导ID和学生ID
                    taskDTO.setProjectId(savedProject.getId());
                    
                    // 确保督导ID已设置
                    if (taskDTO.getSupervisorId() == null) {
                        taskDTO.setSupervisorId(project.getSupervisorId());
                    }
                    
                    // 设置学生ID为项目申请人
                    taskDTO.setAssigneeId(project.getAssigneeId());
                    
                    // 创建任务
                    try {
                        taskService.createTask(taskDTO);
                        logger.info("为课题创建任务成功: 课题ID={}, 任务标题={}", savedProject.getId(), taskDTO.getTitle());
                    } catch (Exception e) {
                        logger.error("为课题创建任务失败: 课题ID={}, 任务标题={}, 错误={}", 
                            savedProject.getId(), taskDTO.getTitle(), e.getMessage());
                    }
                }
            }
            
            // 返回创建的课题DTO
            return ProjectDTO.fromEntity(savedProject, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("课题申请失败", e);
            throw new RuntimeException("课题申请失败: " + e.getMessage());
        }
    }

    /**
     * 督导员审核课题申请
     */
    @Override
    public ProjectDTO reviewProjectApplication(Long projectId, boolean approved) {
        logger.info("督导员审核课题申请, 课题ID: {}, 是否通过: {}", projectId, approved);
        
        try {
            // 查询课题
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (!projectOptional.isPresent()) {
                throw new RuntimeException("课题不存在");
            }
            
            Project project = projectOptional.get();
            
            // 验证课题状态是否为未审核
            if (!"未审核".equals(project.getStatus())) {
                throw new RuntimeException("只能审核状态为'未审核'的课题");
            }
            
            // 根据审核结果设置状态
            if (approved) {
                project.setStatus("进行中");
            } else {
                project.setStatus("审核未通过");
            }
            
            // 更新课题
            Project savedProject = projectRepository.save(project);
            
            // 获取督导和学生的姓名
            String supervisorName = getUserNameById(savedProject.getSupervisorId());
            String assigneeName = getUserNameById(savedProject.getAssigneeId());
            
            // 返回更新后的课题DTO
            return ProjectDTO.fromEntity(savedProject, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("审核课题申请失败", e);
            throw new RuntimeException("审核课题申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核的课题申请列表
     */
    @Override
    public List<ProjectDTO> getPendingReviewProjects(Long supervisorId) {
        logger.info("获取督导的待审核课题申请, 督导ID: {}", supervisorId);
        
        try {
            // 查询状态为未审核的课题
            List<Project> projects = projectRepository.findBySupervisorIdAndStatus(supervisorId, "未审核");
            
            // 转换为DTO列表
            return projects.stream()
                .map(project -> {
                    String supervisorName = getUserNameById(project.getSupervisorId());
                    String assigneeName = getUserNameById(project.getAssigneeId());
                    return ProjectDTO.fromEntity(project, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取待审核课题申请失败", e);
            throw new RuntimeException("获取待审核课题申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取姓名
     */
    private String getUserNameById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                return user.getRealName() != null ? user.getRealName() : user.getUsername();
            }
        } catch (Exception e) {
            logger.warn("获取用户名失败, ID: {}", userId, e);
        }
        
        return null;
    }
    
    /**
     * 验证状态值是否有效
     */
    private boolean isValidStatus(String status) {
        return status != null && (
            "未开始".equals(status) ||
            "进行中".equals(status) ||
            "已完成".equals(status) ||
            "已结项".equals(status) ||
            "未审核".equals(status) ||
            "审核未通过".equals(status)
        );
    }
} 