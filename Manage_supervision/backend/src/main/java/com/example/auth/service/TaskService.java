package com.example.auth.service;

import com.example.auth.dto.TaskDTO;
import com.example.auth.dto.TaskRequest;
import com.example.auth.entity.Task;

import java.util.List;
import java.util.Map;

/**
 * Task Service Interface
 */
public interface TaskService {
    
    /**
     * Create task
     */
    TaskDTO createTask(TaskDTO taskDTO);
    
    /**
     * Update task
     */
    TaskDTO updateTask(Long id, TaskDTO taskDTO);
    
    /**
     * Get task details
     */
    TaskDTO getTaskById(Long id);
    
    /**
     * Delete task
     */
    boolean deleteTask(Long id);
    
    /**
     * Get all tasks created by supervisor
     */
    List<TaskDTO> getTasksBySupervisor(Long supervisorId);
    
    /**
     * Get tasks created by supervisor with specific status
     */
    List<TaskDTO> getTasksBySupervisorAndStatus(Long supervisorId, String status);
    
    /**
     * Get all tasks assigned to student
     */
    List<TaskDTO> getTasksByAssignee(Long assigneeId);
    
    /**
     * Get tasks assigned to student with specific status
     */
    List<TaskDTO> getTasksByAssigneeAndStatus(Long assigneeId, String status);
    
    /**
     * Assign task to student
     */
    TaskDTO assignTaskToStudent(Long taskId, Long studentId);
    
    /**
     * Update task status
     */
    TaskDTO updateTaskStatus(Long taskId, String status);

    /**
     * Get all tasks
     */
    List<TaskDTO> getAllTasks();

    /**
     * Batch create tasks and assign to all students in a project
     * @param projectId Project ID
     * @param taskRequest Task request object
     * @return List of created tasks
     */
    List<TaskDTO> createBatchTasksForProject(Long projectId, TaskRequest taskRequest);

    /**
     * Get batch tasks for a project
     * @param projectId Project ID
     * @return List of batch tasks
     */
    List<TaskDTO> getBatchTasksByProject(Long projectId);

    /**
     * Batch create tasks
     * @param projectId Project ID
     * @param taskRequest Task request object
     * @return List of created tasks
     */
    List<TaskDTO> batchCreateTasks(Long projectId, TaskRequest taskRequest);
    
    /**
     * Get all tasks for a project
     * @param projectId Project ID
     * @return List of tasks
     */
    List<TaskDTO> getTasksByProjectId(Long projectId);
    
    /**
     * Get incomplete tasks for a project
     * @param projectId Project ID
     * @return List of incomplete tasks
     */
    List<TaskDTO> getIncompleteTasksByProjectId(Long projectId);
    
    /**
     * Get completed tasks for a project
     * @param projectId Project ID
     * @return List of completed tasks
     */
    List<TaskDTO> getCompletedTasksByProjectId(Long projectId);
    
    /**
     * Update task completion status
     * @param taskId Task ID
     * @param completed Whether completed
     * @return Updated task DTO
     */
    TaskDTO updateTaskCompletionStatus(Long taskId, boolean completed);
    
    /**
     * Check if task belongs to specified project
     * @param taskId Task ID
     * @param projectId Project ID
     * @return Whether belongs
     */
    boolean isTaskBelongToProject(Long taskId, Long projectId);
} 