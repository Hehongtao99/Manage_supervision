package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.service.ProjectService;
import com.example.auth.service.TaskEvaluationService;
import com.example.auth.service.TaskService;
import com.example.auth.service.UserService;
import com.example.auth.service.ProjectEvaluationService;
import com.example.auth.service.ClassService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling supervisor-related requests
 */
@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    private static final Logger logger = LoggerFactory.getLogger(SupervisorController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private TaskEvaluationService taskEvaluationService;
    
    @Autowired
    private ProjectEvaluationService projectEvaluationService;
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all students list
     */
    @GetMapping("/students")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getAllStudents() {
        logger.info("Supervisor requested all students list");
        try {
            List<StudentDTO> students = userService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Failed to get students list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get students list: " + e.getMessage()));
        }
    }

    /**
     * Get students assigned to supervisor
     */
    @GetMapping("/students/assigned")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getAssignedStudents(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long supervisorId = jwtUtil.getUserIdFromToken(token);
            logger.info("Supervisor requested their assigned students list, ID: {}", supervisorId);
            
            List<UserDTO> students = userService.getStudentsByTeacher(supervisorId);
            if (!students.isEmpty()) {
                logger.info("学生数据示例: id={}, name={}, userNumber={}", 
                            students.get(0).getId(), 
                            students.get(0).getRealName(), 
                            students.get(0).getUserNumber());
            } else {
                logger.info("未找到分配给督导员ID: {}的学生", supervisorId);
            }
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Failed to get assigned students list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get assigned students list: " + e.getMessage()));
        }
    }

    /**
     * Get student details
     */
    @GetMapping("/students/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getStudentDetails(@PathVariable Long id) {
        logger.info("Supervisor requested student details, ID: {}", id);
        try {
            StudentDetailDTO student = userService.getStudentDetails(id);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            logger.error("Failed to get student details", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get student details: " + e.getMessage()));
        }
    }

    /**
     * Update student status
     */
    @PutMapping("/students/{id}/status")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateStudentStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        String status = statusData.get("status");
        logger.info("Supervisor requested to update student status, ID: {}, New status: {}", id, status);
        
        if (status == null || (!status.equals("active") && !status.equals("inactive"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status value, must be 'active' or 'inactive'"));
        }
        
        try {
            boolean success = userService.updateStudentStatus(id, status);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Student status has been updated to: " + status,
                    "status", status
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to update student status, student may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to update student status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update student status: " + e.getMessage()));
        }
    }

    /**
     * Delete student
     */
    @DeleteMapping("/students/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        logger.info("Supervisor requested to delete student, ID: {}", id);
        try {
            boolean success = userService.deleteStudent(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Student has been successfully deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete student, student may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete student", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete student: " + e.getMessage()));
        }
    }
    
    // ==================== Task Management APIs ====================

    /**
     * Create task
     */
    @PostMapping("/tasks")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        logger.info("Supervisor requested to create task: {}", taskDTO.getTitle());
        try {
            TaskDTO createdTask = taskService.createTask(taskDTO);
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            logger.error("Failed to create task", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to create task: " + e.getMessage()));
        }
    }

    /**
     * Update task
     */
    @PutMapping("/tasks/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        logger.info("Supervisor requested to update task, ID: {}", id);
        try {
            TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("Failed to update task", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update task: " + e.getMessage()));
        }
    }

    /**
     * Get task details
     */
    @GetMapping("/tasks/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        logger.info("Supervisor requested task details, ID: {}", id);
        try {
            TaskDTO task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("Failed to get task details", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get task details: " + e.getMessage()));
        }
    }

    /**
     * Delete task
     */
    @DeleteMapping("/tasks/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        logger.info("Supervisor requested to delete task, ID: {}", id);
        try {
            boolean success = taskService.deleteTask(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Task has been successfully deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete task, task may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete task", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete task: " + e.getMessage()));
        }
    }

    /**
     * Get supervisor created tasks
     */
    @GetMapping("/tasks")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTasksBySupervisor(@RequestParam Long supervisorId) {
        logger.info("Supervisor requested to get all tasks created by themselves, Supervisor ID: {}", supervisorId);
        try {
            List<TaskDTO> tasks = taskService.getTasksBySupervisor(supervisorId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Failed to get supervisor created tasks", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get supervisor created tasks: " + e.getMessage()));
        }
    }

    /**
     * Get supervisor created tasks by status
     */
    @GetMapping("/tasks/status/{status}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTasksBySupervisorAndStatus(
            @RequestParam Long supervisorId,
            @PathVariable String status) {
        logger.info("Supervisor requested to get all tasks created by themselves by specified status, Supervisor ID: {}, status: {}", supervisorId, status);
        try {
            List<TaskDTO> tasks = taskService.getTasksBySupervisorAndStatus(supervisorId, status);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Failed to get supervisor created tasks by status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get supervisor created tasks by status: " + e.getMessage()));
        }
    }

    /**
     * Assign task to student
     */
    @PutMapping("/tasks/{taskId}/assign/{studentId}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> assignTaskToStudent(
            @PathVariable Long taskId,
            @PathVariable Long studentId) {
        logger.info("Supervisor requested to assign task to student, Task ID: {}, Student ID: {}", taskId, studentId);
        try {
            TaskDTO task = taskService.assignTaskToStudent(taskId, studentId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("Failed to assign task to student", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to assign task to student: " + e.getMessage()));
        }
    }

    /**
     * Update task status
     */
    @PutMapping("/tasks/{id}/status")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData) {
        String status = statusData.get("status");
        logger.info("Supervisor requested to update task status, ID: {}, New status: {}", id, status);
        
        try {
            TaskDTO task = taskService.updateTaskStatus(id, status);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("Failed to update task status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update task status: " + e.getMessage()));
        }
    }
    
    // ==================== Project Management APIs ====================

    /**
     * Create project
     */
    @PostMapping("/projects")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO) {
        logger.info("Supervisor requested to create project: {}", projectDTO.getTitle());
        try {
            ProjectDTO createdProject = projectService.createProject(projectDTO);
            return ResponseEntity.ok(createdProject);
        } catch (Exception e) {
            logger.error("Failed to create project", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to create project: " + e.getMessage()));
        }
    }

    /**
     * Update project
     */
    @PutMapping("/projects/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        logger.info("Supervisor requested to update project, ID: {}", id);
        try {
            ProjectDTO updatedProject = projectService.updateProject(id, projectDTO);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            logger.error("Failed to update project", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update project: " + e.getMessage()));
        }
    }

    /**
     * Get project details
     */
    @GetMapping("/projects/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        logger.info("Supervisor requested project details, ID: {}", id);
        try {
            ProjectDTO project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            logger.error("Failed to get project details", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get project details: " + e.getMessage()));
        }
    }

    /**
     * Delete project
     */
    @DeleteMapping("/projects/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        logger.info("Supervisor requested to delete project, ID: {}", id);
        try {
            boolean success = projectService.deleteProject(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Project has been successfully deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete project, project may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete project", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete project: " + e.getMessage()));
        }
    }

    /**
     * Get supervisor created projects
     */
    @GetMapping("/projects")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getProjectsBySupervisor(@RequestParam Long supervisorId) {
        logger.info("Supervisor requested to get all projects created by themselves, Supervisor ID: {}", supervisorId);
        try {
            List<ProjectDTO> projects = projectService.getProjectsBySupervisor(supervisorId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Failed to get supervisor created projects", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get supervisor created projects: " + e.getMessage()));
        }
    }

    /**
     * Get supervisor created projects by status
     */
    @GetMapping("/projects/status/{status}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getProjectsBySupervisorAndStatus(
            @RequestParam Long supervisorId,
            @PathVariable String status) {
        logger.info("Supervisor requested to get all projects created by themselves by specified status, Supervisor ID: {}, status: {}", supervisorId, status);
        try {
            List<ProjectDTO> projects = projectService.getProjectsBySupervisorAndStatus(supervisorId, status);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Failed to get supervisor created projects by status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get supervisor created projects by status: " + e.getMessage()));
        }
    }

    /**
     * Get projects by category
     */
    @GetMapping("/projects/category/{category}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getProjectsByCategory(@PathVariable String category) {
        logger.info("Supervisor requested to get projects by specified category, category: {}", category);
        try {
            List<ProjectDTO> projects = projectService.getProjectsByCategory(category);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Failed to get projects by category", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get projects by category: " + e.getMessage()));
        }
    }

    /**
     * Get pending project application review
     */
    @GetMapping("/projects/pending-review")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getPendingReviewProjects(@RequestParam Long supervisorId) {
        logger.info("Supervisor requested to get pending project application review, Supervisor ID: {}", supervisorId);
        try {
            List<ProjectDTO> projects = projectService.getPendingReviewProjects(supervisorId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Failed to get pending project application review", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get pending project application review: " + e.getMessage()));
        }
    }
    
    /**
     * Review project application
     */
    @PutMapping("/projects/{id}/review")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> reviewProjectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reviewData) {
        logger.info("Supervisor requested to review project application, Project ID: {}", id);
        
        try {
            Boolean approved = (Boolean) reviewData.get("approved");
            
            if (approved == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Missing review result"));
            }
            
            ProjectDTO reviewedProject = projectService.reviewProjectApplication(id, approved);
            return ResponseEntity.ok(reviewedProject);
        } catch (Exception e) {
            logger.error("Failed to review project application", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to review project application: " + e.getMessage()));
        }
    }

    /**
     * Assign project to student
     */
    @PutMapping("/projects/{projectId}/assign/{studentId}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> assignProjectToStudent(
            @PathVariable Long projectId,
            @PathVariable Long studentId) {
        logger.info("Supervisor requested to assign project to student, Project ID: {}, Student ID: {}", projectId, studentId);
        try {
            ProjectDTO project = projectService.assignProjectToStudent(projectId, studentId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            logger.error("Failed to assign project to student", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to assign project to student: " + e.getMessage()));
        }
    }

    /**
     * Update project status
     */
    @PutMapping("/projects/{id}/status")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateProjectStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData) {
        String status = statusData.get("status");
        logger.info("Supervisor requested to update project status, ID: {}, New status: {}", id, status);
        
        try {
            ProjectDTO project = projectService.updateProjectStatus(id, status);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            logger.error("Failed to update project status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update project status: " + e.getMessage()));
        }
    }

    /**
     * Batch create tasks and assign to all students in project
     * @param projectId  project ID
     * @param taskRequest  task request
     * @return  create result
     */
    @PostMapping("/projects/{projectId}/batch-tasks")
    public ResponseEntity<?> createBatchTasksForProject(
            @PathVariable Long projectId,
            @RequestBody TaskRequest taskRequest) {
        try {
            logger.info("Supervisor creating batch tasks for project ID: {}", projectId);
            List<TaskDTO> createdTasks = taskService.createBatchTasksForProject(projectId, taskRequest);
            return ResponseEntity.ok(createdTasks);
        } catch (Exception e) {
            logger.error("Error in batch task creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create batch tasks: " + e.getMessage()));
        }
    }
    
    /**
     * Get batch task list in project
     * @param projectId  project ID
     * @return  batch task list
     */
    @GetMapping("/projects/{projectId}/batch-tasks")
    public ResponseEntity<?> getBatchTasksByProject(@PathVariable Long projectId) {
        try {
            logger.info("Fetching batch tasks for project ID: {}", projectId);
            List<TaskDTO> batchTasks = taskService.getBatchTasksByProject(projectId);
            return ResponseEntity.ok(batchTasks);
        } catch (Exception e) {
            logger.error("Failed to get batch tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to get batch tasks: " + e.getMessage()));
        }
    }

    /**
     * Evaluate task (give task score and comment)
     */
    @PostMapping("/tasks/{taskId}/evaluate")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> evaluateTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> evaluationData,
            @RequestHeader("Authorization") String auth) {
        
        Integer score = null;
        String comment = null;
        
        // Get score and comment from request body
        if (evaluationData.containsKey("score")) {
            if (evaluationData.get("score") instanceof Integer) {
                score = (Integer) evaluationData.get("score");
            } else if (evaluationData.get("score") instanceof Double) {
                score = ((Double) evaluationData.get("score")).intValue();
            } else {
                try {
                    score = Integer.parseInt(evaluationData.get("score").toString());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Score must be a number"));
                }
            }
        }
        
        if (evaluationData.containsKey("comment")) {
            comment = (String) evaluationData.get("comment");
        }
        
        if (score == null || score < 0 || score > 100) {
            return ResponseEntity.badRequest().body(Map.of("message", "Score must be between 0-100"));
        }
        
        logger.info("Supervisor requested to evaluate task, Task ID: {}, Score: {}", taskId, score);
        
        try {
            // Get current user
            String token = auth.substring(7); // Remove "Bearer " prefix
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User does not exist"));
            }
            
            // Get task details
            TaskDTO task = taskService.getTaskById(taskId);
            
            // Check if it's the supervisor of the task
            if (!task.getSupervisorId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Only the supervisor of the task can evaluate the task"));
            }
            
            // Check if the task is completed
            if (!task.getCompleted()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Can only evaluate completed tasks"));
            }
            
            // Create or update evaluation
            TaskEvaluationDTO evaluation = taskEvaluationService.createOrUpdateEvaluation(
                    taskId, score, comment, user.getId());
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("Failed to evaluate task", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to evaluate task: " + e.getMessage()));
        }
    }

    /**
     * Get task evaluation (supervisor perspective)
     */
    @GetMapping("/tasks/{taskId}/evaluation")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTaskEvaluation(@PathVariable Long taskId) {
        logger.info("Supervisor requested to get task evaluation, Task ID: {}", taskId);
        try {
            TaskEvaluationDTO evaluation = taskEvaluationService.getEvaluationByTaskId(taskId);
            
            if (evaluation == null) {
                return ResponseEntity.ok(Map.of("message", "Task has not been evaluated"));
            }
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("Failed to get task evaluation", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get task evaluation: " + e.getMessage()));
        }
    }

    /**
     * Get project evaluation (supervisor perspective)
     */
    @GetMapping("/projects/{projectId}/evaluation")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getProjectEvaluation(@PathVariable Long projectId) {
        logger.info("Supervisor requested to get project evaluation, Project ID: {}", projectId);
        try {
            ProjectEvaluationDTO evaluation = projectEvaluationService.getEvaluationByProjectId(projectId);
            
            if (evaluation == null) {
                return ResponseEntity.ok(Map.of("message", "Project has not been evaluated"));
            }
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("Failed to get project evaluation", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get project evaluation: " + e.getMessage()));
        }
    }

    /**
     * Evaluate project (give project score and comment)
     */
    @PostMapping("/projects/{projectId}/evaluate")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> evaluateProject(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> evaluationData,
            @RequestHeader("Authorization") String auth) {
        
        Integer score = null;
        String comment = null;
        
        // Get score and comment from request body
        if (evaluationData.containsKey("score")) {
            if (evaluationData.get("score") instanceof Integer) {
                score = (Integer) evaluationData.get("score");
            } else if (evaluationData.get("score") instanceof Double) {
                score = ((Double) evaluationData.get("score")).intValue();
            } else {
                try {
                    score = Integer.parseInt(evaluationData.get("score").toString());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Score must be a number"));
                }
            }
        }
        
        if (evaluationData.containsKey("comment")) {
            comment = (String) evaluationData.get("comment");
        }
        
        if (score == null || score < 0 || score > 100) {
            return ResponseEntity.badRequest().body(Map.of("message", "Score must be between 0-100"));
        }
        
        logger.info("Supervisor requested to evaluate project, Project ID: {}, Score: {}", projectId, score);
        
        try {
            // Get current user
            String token = auth.substring(7); // Remove "Bearer " prefix
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User does not exist"));
            }
            
            // Get project details
            ProjectDTO project = projectService.getProjectById(projectId);
            
            // Check if it's the supervisor of the project
            if (!project.getSupervisorId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Only the supervisor of the project can evaluate the project"));
            }
            
            // Check if the project is completed
            if (!"已完成".equals(project.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Can only evaluate completed projects"));
            }
            
            // Create or update evaluation
            ProjectEvaluationDTO evaluation = projectEvaluationService.createOrUpdateEvaluation(
                    projectId, score, comment, user.getId());
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("Failed to evaluate project", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to evaluate project: " + e.getMessage()));
        }
    }

    /**
     * Delete task evaluation
     */
    @DeleteMapping("/tasks/{taskId}/evaluation")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteTaskEvaluation(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String auth) {
        
        logger.info("Supervisor requested to delete task evaluation, Task ID: {}", taskId);
        
        try {
            // Get current user
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User does not exist"));
            }
            
            // Get task details
            TaskDTO task = taskService.getTaskById(taskId);
            
            // Check if it's the supervisor of the task
            if (!task.getSupervisorId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Only the supervisor of the task can delete evaluation"));
            }
            
            boolean deleted = taskEvaluationService.deleteEvaluation(taskId);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Evaluation has been deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Evaluation does not exist or failed to delete"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete task evaluation", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete task evaluation: " + e.getMessage()));
        }
    }

    /**
     * Delete project evaluation
     */
    @DeleteMapping("/projects/{projectId}/evaluation")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteProjectEvaluation(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String auth) {
        
        logger.info("Supervisor requested to delete project evaluation, Project ID: {}", projectId);
        
        try {
            // Get current user
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User does not exist"));
            }
            
            // Get project details
            ProjectDTO project = projectService.getProjectById(projectId);
            
            // Check if it's the supervisor of the project
            if (!project.getSupervisorId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Only the supervisor of the project can delete evaluation"));
            }
            
            boolean deleted = projectEvaluationService.deleteEvaluation(projectId);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Evaluation has been deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Evaluation does not exist or failed to delete"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete project evaluation", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete project evaluation: " + e.getMessage()));
        }
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        token = token.substring(7); // 移除"Bearer "前缀
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("从token中获取用户ID失败", e);
            return null;
        }
    }

    // 获取教师教授的班级列表
    @GetMapping("/classes")
    public ResponseEntity<?> getTeacherClasses(HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
            }
            
            List<Map<String, Object>> classes = classService.getClassesByTeacherId(teacherId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取班级列表失败：" + e.getMessage());
        }
    }

    // 获取指定班级的学生列表
    @GetMapping("/classes/{classId}/students")
    public ResponseEntity<?> getClassStudents(@PathVariable Long classId, HttpServletRequest request) {
        try {
            Long teacherId = getUserIdFromRequest(request);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权的访问");
            }
            
            // 验证教师是否有权限访问该班级
            if (!classService.isTeacherAssignedToClass(teacherId, classId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您没有权限访问该班级");
            }
            
            List<Map<String, Object>> students = classService.getClassStudentsDetail(classId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取班级学生失败：" + e.getMessage());
        }
    }
}