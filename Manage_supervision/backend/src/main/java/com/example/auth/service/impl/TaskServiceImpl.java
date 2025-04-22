package com.example.auth.service.impl;

import com.example.auth.dto.TaskDTO;
import com.example.auth.dto.TaskRequest;
import com.example.auth.entity.Task;
import com.example.auth.entity.User;
import com.example.auth.repository.TaskRepository;
import com.example.auth.repository.TaskSubmissionRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ProjectService;
import com.example.auth.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Task Service Implementation
 */
@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    /**
     * Create task
     */
    @Override
    public TaskDTO createTask(TaskDTO taskDTO) {
        logger.info("Creating task: {}", taskDTO.getTitle());
        
        try {
            // Convert DTO to entity
            Task task = taskDTO.toEntity();
            
            // Set default status
            if (task.getStatus() == null) {
                task.setStatus("Draft");
            }
            
            // Save task
            Task savedTask = taskRepository.save(task);
            
            // Get supervisor and student names
            String supervisorName = getUserNameById(task.getSupervisorId());
            String assigneeName = getUserNameById(task.getAssigneeId());
            
            // Return created task DTO
            return TaskDTO.fromEntity(savedTask, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("Failed to create task", e);
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    /**
     * Update task
     */
    @Override
    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        logger.info("Updating task, ID: {}, Input data: {}", id, taskDTO);
        
        try {
            // Query task
            Optional<Task> taskOptional = taskRepository.findById(id);
            if (!taskOptional.isPresent()) {
                throw new RuntimeException("Task does not exist");
            }
            
            // Get existing task
            Task existingTask = taskOptional.get();
            Long projectId = existingTask.getProjectId();
            boolean statusChanged = false;
            boolean completedChanged = false;
            
            logger.info("Existing data before task update, ID: {}, Title: {}, Project ID: {}, Status: {}, Completion Status: {}", 
                id, existingTask.getTitle(), existingTask.getProjectId(), existingTask.getStatus(), existingTask.getCompleted());
            
            // Create task update object
            Task updatedTask = taskDTO.toEntity();
            updatedTask.setId(id);
            
            // Ensure projectId field is not empty, if frontend didn't pass it, keep the original value
            if (updatedTask.getProjectId() == null && existingTask.getProjectId() != null) {
                logger.info("No projectId specified during task update, keeping original project association, Task ID: {}, Kept projectId: {}", 
                    id, existingTask.getProjectId());
                updatedTask.setProjectId(existingTask.getProjectId());
            }
            
            // Check assigneeId, if null keep the original assignee
            if (updatedTask.getAssigneeId() == null && existingTask.getAssigneeId() != null) {
                logger.info("No assigneeId specified during task update, keeping original assignment, Task ID: {}, Kept assigneeId: {}", 
                    id, existingTask.getAssigneeId());
                updatedTask.setAssigneeId(existingTask.getAssigneeId());
            }
            
            // Check if completion status and status changed
            if (existingTask.getCompleted() != updatedTask.getCompleted()) {
                completedChanged = true;
            }
            
            if (existingTask.getStatus() != null && updatedTask.getStatus() != null &&
                !existingTask.getStatus().equals(updatedTask.getStatus())) {
                statusChanged = true;
            }
            
            // Keep original creation time
            updatedTask.setCreateTime(existingTask.getCreateTime());
            
            // Log key changes
            logger.info("Task update details, ID: {}, Original title: {}, New title: {}, Original projectId: {}, New projectId: {}, Original assigneeId: {}, New assigneeId: {}, Original completed: {}, New completed: {}", 
                id, existingTask.getTitle(), updatedTask.getTitle(), existingTask.getProjectId(), updatedTask.getProjectId(),
                existingTask.getAssigneeId(), updatedTask.getAssigneeId(), 
                existingTask.getCompleted(), updatedTask.getCompleted());
            
            // Save updated task
            Task savedTask = taskRepository.save(updatedTask);
            logger.info("Task update successful, ID: {}, Final projectId: {}, Status: {}, Completion Status: {}", 
                savedTask.getId(), savedTask.getProjectId(), savedTask.getStatus(), savedTask.getCompleted());
            
            // If task is associated with a project, and status or completion status changed, update project status
            if (projectId != null && (completedChanged || statusChanged)) {
                try {
                    projectService.updateProjectStatusBasedOnTasks(projectId);
                    logger.info("Task status changed, project status updated, Project ID: {}", projectId);
                } catch (Exception e) {
                    logger.warn("Failed to update project status, but task was successfully updated: {}", e.getMessage());
                }
            }
            
            // Get supervisor and student names
            String supervisorName = getUserNameById(savedTask.getSupervisorId());
            String assigneeName = getUserNameById(savedTask.getAssigneeId());
            
            // Return updated task DTO
            return TaskDTO.fromEntity(savedTask, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("Failed to update task", e);
            throw new RuntimeException("Failed to update task: " + e.getMessage());
        }
    }

    /**
     * Get task details
     */
    @Override
    public TaskDTO getTaskById(Long id) {
        logger.info("Getting task details, ID: {}", id);
        
        try {
            // Query task
            Optional<Task> taskOptional = taskRepository.findById(id);
            if (!taskOptional.isPresent()) {
                throw new RuntimeException("Task does not exist");
            }
            
            Task task = taskOptional.get();
            
            // Get supervisor and student names
            String supervisorName = getUserNameById(task.getSupervisorId());
            String assigneeName = getUserNameById(task.getAssigneeId());
            
            // Return task DTO
            return TaskDTO.fromEntity(task, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("Failed to get task details", e);
            throw new RuntimeException("Failed to get task details: " + e.getMessage());
        }
    }

    /**
     * Delete task
     */
    @Override
    public boolean deleteTask(Long id) {
        logger.info("Deleting task, ID: {}", id);
        
        try {
            // Query task
            if (!taskRepository.existsById(id)) {
                logger.warn("Task does not exist, ID: {}", id);
                return false;
            }
            
            // Delete task
            taskRepository.deleteById(id);
            logger.info("Task delete successful, ID: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete task", e);
            return false;
        }
    }

    /**
     * Get supervisor created all tasks
     */
    @Override
    public List<TaskDTO> getTasksBySupervisor(Long supervisorId) {
        logger.info("Getting supervisor created all tasks, Supervisor ID: {}", supervisorId);
        
        try {
            // Query task (only return tasks not associated with any project)
            List<Task> tasks = taskRepository.findBySupervisorIdAndProjectIdIsNull(supervisorId);
            
            // Convert to DTO list
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get supervisor created tasks", e);
            throw new RuntimeException("Failed to get supervisor created tasks: " + e.getMessage());
        }
    }

    /**
     * Get supervisor created specified status tasks
     */
    @Override
    public List<TaskDTO> getTasksBySupervisorAndStatus(Long supervisorId, String status) {
        logger.info("Getting supervisor created specified status tasks, Supervisor ID: {}, Status: {}", supervisorId, status);
        
        try {
            // Query task (only return tasks not associated with any project)
            List<Task> tasks = taskRepository.findBySupervisorIdAndStatusAndProjectIdIsNull(supervisorId, status);
            
            // Convert to DTO list
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get supervisor created specified status tasks", e);
            throw new RuntimeException("Failed to get supervisor created specified status tasks: " + e.getMessage());
        }
    }

    /**
     * Get assigned to student all tasks
     */
    @Override
    public List<TaskDTO> getTasksByAssignee(Long assigneeId) {
        logger.info("Getting assigned to student all tasks, Student ID: {}", assigneeId);
        
        try {
            // Query task
            List<Task> tasks = taskRepository.findByAssigneeId(assigneeId);
            
            // Convert to DTO list
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get assigned to student tasks", e);
            throw new RuntimeException("Failed to get assigned to student tasks: " + e.getMessage());
        }
    }

    /**
     * Get assigned to student specified status tasks
     */
    @Override
    public List<TaskDTO> getTasksByAssigneeAndStatus(Long assigneeId, String status) {
        logger.info("Getting assigned to student specified status tasks, Student ID: {}, Status: {}", assigneeId, status);
        
        try {
            // Query task
            List<Task> tasks = taskRepository.findByAssigneeIdAndStatus(assigneeId, status);
            
            // Convert to DTO list
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get assigned to student specified status tasks", e);
            throw new RuntimeException("Failed to get assigned to student specified status tasks: " + e.getMessage());
        }
    }

    /**
     * Assign task to student
     */
    @Override
    public TaskDTO assignTaskToStudent(Long taskId, Long studentId) {
        logger.info("Assigning task to student, Task ID: {}, Student ID: {}", taskId, studentId);
        
        try {
            // Query task
            Optional<Task> taskOptional = taskRepository.findById(taskId);
            if (!taskOptional.isPresent()) {
                throw new RuntimeException("Task does not exist");
            }
            
            // Verify student exists
            Optional<User> studentOptional = userRepository.findById(studentId);
            if (!studentOptional.isPresent()) {
                throw new RuntimeException("Student does not exist");
            }
            
            Task task = taskOptional.get();
            
            // Set assigned student ID
            task.setAssigneeId(studentId);
            
            // If status is draft, update to midterm
            if ("Draft".equals(task.getStatus())) {
                task.setStatus("Midterm");
            }
            
            // Save update
            Task savedTask = taskRepository.save(task);
            
            // Get supervisor and student names
            String supervisorName = getUserNameById(savedTask.getSupervisorId());
            String assigneeName = getUserNameById(savedTask.getAssigneeId());
            
            // Return updated task DTO
            return TaskDTO.fromEntity(savedTask, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("Failed to assign task to student", e);
            throw new RuntimeException("Failed to assign task to student: " + e.getMessage());
        }
    }

    /**
     * Update task status
     */
    @Override
    public TaskDTO updateTaskStatus(Long taskId, String status) {
        logger.info("Updating task status, Task ID: {}, New status: {}", taskId, status);
        
        try {
            // Query task
            Optional<Task> taskOptional = taskRepository.findById(taskId);
            if (!taskOptional.isPresent()) {
                throw new RuntimeException("Task does not exist");
            }
            
            // Verify status value
            if (!isValidStatus(status)) {
                throw new RuntimeException("Invalid status value");
            }
            
            Task task = taskOptional.get();
            
            // Update status
            task.setStatus(status);
            
            // If status is final, set end time to current time
            if ("Final".equals(status) && task.getEndTime() == null) {
                task.setEndTime(LocalDateTime.now());
            }
            
            // Save update
            Task savedTask = taskRepository.save(task);
            
            // Get supervisor and student names
            String supervisorName = getUserNameById(savedTask.getSupervisorId());
            String assigneeName = getUserNameById(savedTask.getAssigneeId());
            
            // Return updated task DTO
            return TaskDTO.fromEntity(savedTask, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("Failed to update task status", e);
            throw new RuntimeException("Failed to update task status: " + e.getMessage());
        }
    }
    
    /**
     * Get user name by ID
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
            logger.warn("Failed to get username, ID: {}", userId, e);
        }
        
        return null;
    }
    
    /**
     * Verify status value is valid
     */
    private boolean isValidStatus(String status) {
        return status != null && (
            "Draft".equals(status) ||
            "Midterm".equals(status) ||
            "Final".equals(status)
        );
    }
    
    /**
     * Get all tasks
     */
    @Override
    public List<TaskDTO> getAllTasks() {
        logger.info("Getting all tasks");
        
        try {
            List<Task> tasks = taskRepository.findAll();
            
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get all tasks", e);
            throw new RuntimeException("Failed to get all tasks: " + e.getMessage());
        }
    }
    
    /**
     * Batch create tasks and assign to all students under project
     */
    @Override
    public List<TaskDTO> createBatchTasksForProject(Long projectId, TaskRequest taskRequest) {
        logger.info("Batch creating tasks for project, Project ID: {}", projectId);
        
        try {
            // Here need to inject ProjectService and get project details, but for simplification, implement batchCreateTasks directly
            return batchCreateTasks(projectId, taskRequest);
        } catch (Exception e) {
            logger.error("Failed to batch create tasks for project", e);
            throw new RuntimeException("Failed to batch create tasks for project: " + e.getMessage());
        }
    }
    
    /**
     * Get project batch task list
     */
    @Override
    public List<TaskDTO> getBatchTasksByProject(Long projectId) {
        logger.info("Getting project batch tasks, Project ID: {}", projectId);
        
        try {
            // Use cache or efficient query method to get all tasks under project
            long startTime = System.currentTimeMillis();
            
            // Directly query associated tasks through project ID
            List<Task> tasks = taskRepository.findByProjectId(projectId);
            
            // Convert to DTO list and preload associated information
            List<TaskDTO> taskDTOs = tasks.parallelStream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
            
            long endTime = System.currentTimeMillis();
            logger.info("Getting project tasks completed, Project ID: {}, Task count: {}, Time: {}ms", 
                        projectId, taskDTOs.size(), (endTime - startTime));
            
            return taskDTOs;
        } catch (Exception e) {
            logger.error("Failed to get project batch tasks, Project ID: {}", projectId, e);
            throw new RuntimeException("Failed to get project batch tasks: " + e.getMessage());
        }
    }
    
    /**
     * Batch create tasks
     */
    @Override
    public List<TaskDTO> batchCreateTasks(Long projectId, TaskRequest taskRequest) {
        logger.info("Batch creating tasks, Project ID: {}, Task title template: {}", projectId, taskRequest.getTitleTemplate());
        
        try {
            // For simplicity, create a task as an example
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setTitle(taskRequest.getTitleTemplate());
            taskDTO.setDescription(taskRequest.getDescriptionTemplate());
            taskDTO.setPriority(taskRequest.getPriority());
            taskDTO.setStatus(taskRequest.getStatus());
            taskDTO.setStartTime(taskRequest.getStartTime());
            taskDTO.setEndTime(taskRequest.getEndTime());
            
            // Set project ID
            taskDTO.setProjectId(projectId);
            
            // Set supervisor ID (here should get project creator from ProjectService)
            // taskDTO.setSupervisorId(project.getSupervisorId());
            
            // Create task
            TaskDTO createdTask = createTask(taskDTO);
            
            // Return created task list
            return List.of(createdTask);
            
            // Actual implementation should:
            // 1. Get project information
            // 2. If assignToAllStudents is true, get all students
            // 3. Create task for each student
            // 4. Return created task list
        } catch (Exception e) {
            logger.error("Failed to batch create tasks", e);
            throw new RuntimeException("Failed to batch create tasks: " + e.getMessage());
        }
    }
    
    /**
     * Get project all tasks
     */
    @Override
    public List<TaskDTO> getTasksByProjectId(Long projectId) {
        logger.info("Getting project all tasks, Project ID: {}", projectId);
        
        try {
            // Query tasks through project ID
            List<Task> tasks = taskRepository.findByProjectId(projectId);
            
            // Convert to DTO
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get project tasks", e);
            throw new RuntimeException("Failed to get project tasks: " + e.getMessage());
        }
    }
    
    /**
     * Get project incomplete tasks
     */
    @Override
    public List<TaskDTO> getIncompleteTasksByProjectId(Long projectId) {
        logger.info("Getting project incomplete tasks, Project ID: {}", projectId);
        
        try {
            // Query tasks through project ID and completion status
            List<Task> tasks = taskRepository.findByProjectIdAndCompleted(projectId, false);
            
            // Convert to DTO
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get project incomplete tasks", e);
            throw new RuntimeException("Failed to get project incomplete tasks: " + e.getMessage());
        }
    }
    
    /**
     * Get project completed tasks
     */
    @Override
    public List<TaskDTO> getCompletedTasksByProjectId(Long projectId) {
        logger.info("Getting project completed tasks, Project ID: {}", projectId);
        
        try {
            // Query tasks through project ID and completion status
            List<Task> tasks = taskRepository.findByProjectIdAndCompleted(projectId, true);
            
            // Convert to DTO
            return tasks.stream()
                .map(task -> {
                    String supervisorName = getUserNameById(task.getSupervisorId());
                    String assigneeName = getUserNameById(task.getAssigneeId());
                    return TaskDTO.fromEntity(task, supervisorName, assigneeName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get project completed tasks", e);
            throw new RuntimeException("Failed to get project completed tasks: " + e.getMessage());
        }
    }
    
    /**
     * Update task completion status
     */
    @Override
    @Transactional
    public TaskDTO updateTaskCompletionStatus(Long taskId, boolean completed) {
        logger.info("Updating task completion status, Task ID: {}, Completion status: {}", taskId, completed);
        
        try {
            // Query task
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (!taskOpt.isPresent()) {
                throw new RuntimeException("Task does not exist");
            }
            
            Task task = taskOpt.get();
            
            // If request marks task as completed, but no submission record, check
            if (completed && !task.getCompleted()) {
                boolean hasSubmissions = false;
                try {
                    hasSubmissions = taskSubmissionRepository.existsByTaskId(taskId);
                } catch (Exception e) {
                    logger.warn("Failed to check task submission record, may cause status inconsistency: {}", e.getMessage());
                }
                
                // Provide warning log but still allow manual update completion status
                if (!hasSubmissions) {
                    logger.warn("Task marked as completed but no submission record. Task ID: {}", taskId);
                }
            }
            
            // Update completion status
            task.setCompleted(completed);
            Task updatedTask = taskRepository.save(task);
            logger.info("Task completion status updated to: {}, Task ID: {}", completed, taskId);
            
            // If task is associated with a project, update project status
            if (task.getProjectId() != null) {
                projectService.updateProjectStatusBasedOnTasks(task.getProjectId());
                logger.info("Project status updated, Project ID: {}", task.getProjectId());
            }
            
            // Get supervisor and student names
            String supervisorName = getUserNameById(task.getSupervisorId());
            String assigneeName = getUserNameById(task.getAssigneeId());
            
            // Return updated task DTO
            return TaskDTO.fromEntity(updatedTask, supervisorName, assigneeName);
        } catch (Exception e) {
            logger.error("Failed to update task completion status", e);
            throw new RuntimeException("Failed to update task completion status: " + e.getMessage());
        }
    }
    
    /**
     * Check if task belongs to specified project
     */
    @Override
    public boolean isTaskBelongToProject(Long taskId, Long projectId) {
        logger.info("Checking if task belongs to specified project, Task ID: {}, Project ID: {}", taskId, projectId);
        
        try {
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (!taskOpt.isPresent()) {
                return false;
            }
            
            Task task = taskOpt.get();
            return projectId.equals(task.getProjectId());
        } catch (Exception e) {
            logger.error("Failed to check task project affiliation", e);
            return false;
        }
    }
} 