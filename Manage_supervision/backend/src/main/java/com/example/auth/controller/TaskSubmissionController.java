package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.TaskDTO;
import com.example.auth.dto.TaskEvaluationDTO;
import com.example.auth.dto.ProjectEvaluationDTO;
import com.example.auth.dto.ProjectDTO;
import com.example.auth.dto.TaskSubmissionDTO;
import com.example.auth.entity.User;
import com.example.auth.service.TaskEvaluationService;
import com.example.auth.service.ProjectEvaluationService;
import com.example.auth.service.ProjectService;
import com.example.auth.service.TaskService;
import com.example.auth.service.TaskSubmissionService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务提交控制器
 */
@RestController
@RequestMapping("/api")
public class TaskSubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(TaskSubmissionController.class);

    @Autowired
    private TaskSubmissionService taskSubmissionService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TaskEvaluationService taskEvaluationService;
    
    @Autowired
    private ProjectEvaluationService projectEvaluationService;
    
    @Autowired
    private ProjectService projectService;

    /**
     * 学生提交任务文件
     */
    @PostMapping("/user/tasks/{taskId}/submit")
    @RequireRole("USER")
    public ResponseEntity<?> submitTaskFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestHeader("Authorization") String auth) {
        
        try {
            // 获取当前用户
            String token = auth.substring(7); // 去除"Bearer "前缀
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                logger.error("提交任务文件失败: 用户不存在, 任务ID: {}", taskId);
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户提交任务文件, 用户ID: {}, 任务ID: {}, 文件名: {}", user.getId(), taskId, file.getOriginalFilename());
            
            // 检查任务是否存在
            TaskDTO task;
            try {
                task = taskService.getTaskById(taskId);
                logger.info("任务检索成功, 任务ID: {}, 分配给用户ID: {}, 督导ID: {}, 当前用户ID: {}", 
                    taskId, task.getAssigneeId(), task.getSupervisorId(), user.getId());
            } catch (Exception e) {
                logger.error("提交任务文件失败: 任务不存在, 任务ID: {}", taskId);
                return ResponseEntity.badRequest().body(Map.of("message", "任务不存在"));
            }
            
            // 验证任务提交权限
            // 允许三种情况：1. 用户是任务的assignee 2. assigneeId为null且用户是任务的supervisor 3. 用户是任务的supervisor
            boolean hasAccess = false;
            if (task.getAssigneeId() != null && user.getId().equals(task.getAssigneeId())) {
                // 用户是任务的assignee
                hasAccess = true;
                logger.info("用户是任务的指定学生, 允许提交, 用户ID: {}, 任务ID: {}", user.getId(), taskId);
            } else if (task.getAssigneeId() == null && user.getId().equals(task.getSupervisorId())) {
                // assigneeId为null且用户是任务的supervisor
                hasAccess = true;
                logger.info("任务未分配给任何学生, 但用户是任务的督导, 允许提交, 用户ID: {}, 任务ID: {}", user.getId(), taskId);
            } else if (user.getId().equals(task.getSupervisorId())) {
                // 用户是任务的supervisor
                hasAccess = true;
                logger.info("用户是任务的督导, 允许提交, 用户ID: {}, 任务ID: {}", user.getId(), taskId);
            }
            
            if (!hasAccess) {
                logger.error("提交任务文件失败: 无权提交该任务, 任务ID: {}, 任务分配给用户ID: {}, 任务督导ID: {}, 当前用户ID: {}", 
                    taskId, task.getAssigneeId(), task.getSupervisorId(), user.getId());
                return ResponseEntity.badRequest().body(Map.of("message", "无权提交该任务"));
            }
            
            // 提交任务文件
            TaskSubmissionDTO submissionDTO = taskSubmissionService.submitTaskFile(taskId, file, user.getId(), comment);
            logger.info("任务文件提交成功, 任务ID: {}, 用户ID: {}, 提交ID: {}", 
                taskId, user.getId(), submissionDTO.getId());
            
            return ResponseEntity.ok(submissionDTO);
        } catch (Exception e) {
            logger.error("任务文件提交失败, 任务ID: {}, 错误: {}", taskId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "任务文件提交失败: " + e.getMessage()));
        }
    }

    /**
     * 获取任务提交记录
     */
    @GetMapping("/tasks/{taskId}/submissions")
    public ResponseEntity<?> getTaskSubmissions(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取任务提交记录（直接传递用户ID给服务层进行权限过滤）
            List<TaskSubmissionDTO> submissions = taskSubmissionService.getSubmissionsByTaskId(taskId, user.getId());
            
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("获取任务提交记录失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取任务提交记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取提交详情
     */
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<?> getSubmissionDetail(
            @PathVariable Long submissionId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取提交详情
            TaskSubmissionDTO submission = taskSubmissionService.getSubmissionById(submissionId);
            
            // 检查权限（任务的督导或提交者可以查看）
            Long taskId = submission.getTaskId();
            boolean isSupervisor = taskService.getTaskById(taskId).getSupervisorId().equals(user.getId());
            boolean isAssignee = taskService.getTaskById(taskId).getAssigneeId().equals(user.getId());
            
            if (!isSupervisor && !isAssignee) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权查看该提交记录"));
            }
            
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            logger.error("获取提交详情失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取提交详情失败: " + e.getMessage()));
        }
    }

    /**
     * 删除提交记录
     */
    @DeleteMapping("/user/submissions/{submissionId}")
    @RequireRole("USER")
    public ResponseEntity<?> deleteSubmission(
            @PathVariable Long submissionId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取提交详情
            TaskSubmissionDTO submission = taskSubmissionService.getSubmissionById(submissionId);
            
            // 检查是否为提交者
            if (!submission.getSubmitterId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权删除该提交记录"));
            }
            
            // 删除提交记录
            boolean deleted = taskSubmissionService.deleteSubmission(submissionId);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "提交记录删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "提交记录删除失败"));
            }
        } catch (Exception e) {
            logger.error("删除提交记录失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "删除提交记录失败: " + e.getMessage()));
        }
    }

    /**
     * 更新提交评论
     */
    @PutMapping("/user/submissions/{submissionId}/comment")
    @RequireRole("USER")
    public ResponseEntity<?> updateSubmissionComment(
            @PathVariable Long submissionId,
            @RequestBody Map<String, String> commentData,
            @RequestHeader("Authorization") String auth) {
        
        String comment = commentData.get("comment");
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取提交详情
            TaskSubmissionDTO submission = taskSubmissionService.getSubmissionById(submissionId);
            
            // 检查是否为提交者
            if (!submission.getSubmitterId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权更新该提交评论"));
            }
            
            // 更新提交评论
            TaskSubmissionDTO updatedSubmission = taskSubmissionService.updateSubmissionComment(submissionId, comment);
            
            return ResponseEntity.ok(updatedSubmission);
        } catch (Exception e) {
            logger.error("更新提交评论失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "更新提交评论失败: " + e.getMessage()));
        }
    }

    /**
     * 查看用户的所有提交记录
     */
    @GetMapping("/user/submissions")
    @RequireRole("USER")
    public ResponseEntity<?> getUserSubmissions(@RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取用户提交记录
            List<TaskSubmissionDTO> submissions = taskSubmissionService.getSubmissionsBySubmitterId(user.getId());
            
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            logger.error("获取用户提交记录失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取用户提交记录失败: " + e.getMessage()));
        }
    }

    /**
     * 更新任务完成状态
     */
    @PutMapping("/tasks/{taskId}/completion")
    public ResponseEntity<?> updateTaskCompletionStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, Boolean> completionData,
            @RequestHeader("Authorization") String auth) {
        Boolean completed = completionData.get("completed");
        
        try {
            if (completed == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "缺少完成状态参数"));
            }
            
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求更新任务完成状态, 用户ID: {}, 任务ID: {}, 完成状态: {}", user.getId(), taskId, completed);
            
            // 获取任务详情
            TaskDTO task = taskService.getTaskById(taskId);
            
            // 验证任务是否分配给当前用户或者是督导
            if (!user.getId().equals(task.getAssigneeId()) && !user.getId().equals(task.getSupervisorId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权更新该任务"));
            }
            
            // 更新任务完成状态
            TaskDTO updatedTask = taskService.updateTaskCompletionStatus(taskId, completed);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("更新任务完成状态失败, 任务ID: {}, 错误: {}", taskId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "更新任务完成状态失败: " + e.getMessage()));
        }
    }

    /**
     * 督导下载提交文件
     */
    @GetMapping("/supervisor/submissions/{submissionId}/download")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> downloadSubmissionFile(
            @PathVariable Long submissionId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取提交详情
            TaskSubmissionDTO submission = taskSubmissionService.getSubmissionById(submissionId);
            
            // 获取任务详情
            TaskDTO task = taskService.getTaskById(submission.getTaskId());
            
            // 检查是否为任务的督导
            if (!task.getSupervisorId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权下载该提交文件"));
            }
            
            // 获取文件路径
            String filePath = submission.getFilePath();
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            
            // 获取文件
            Resource resource = taskSubmissionService.loadFileAsResource(filePath);
            
            // 返回文件
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submission.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(submission.getFileType()))
                    .body(resource);
        } catch (Exception e) {
            logger.error("下载文件失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "下载文件失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学生下载提交文件
     */
    @GetMapping("/user/submissions/{submissionId}/download")
    @RequireRole("USER")
    public ResponseEntity<?> downloadUserSubmissionFile(
            @PathVariable Long submissionId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取提交详情
            TaskSubmissionDTO submission = taskSubmissionService.getSubmissionById(submissionId);
            
            // 检查是否为提交者
            if (!submission.getSubmitterId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权下载该提交文件"));
            }
            
            // 获取文件路径
            String filePath = submission.getFilePath();
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            
            // 获取文件
            Resource resource = taskSubmissionService.loadFileAsResource(filePath);
            
            // 返回文件
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submission.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(submission.getFileType()))
                    .body(resource);
        } catch (Exception e) {
            logger.error("下载文件失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "下载文件失败: " + e.getMessage()));
        }
    }

    /**
     * 学生获取任务评价
     */
    @GetMapping("/user/tasks/{taskId}/evaluation")
    @RequireRole("USER")
    public ResponseEntity<?> getTaskEvaluationForStudent(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String auth) {
        
        logger.info("学生请求获取任务评价, 任务ID: {}", taskId);
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取任务详情
            TaskDTO task = taskService.getTaskById(taskId);
            
            // 检查权限（任务的学生可以查看）
            boolean isAssignee = task.getAssigneeId() != null && task.getAssigneeId().equals(user.getId());
            if (!isAssignee) {
                return ResponseEntity.badRequest().body(Map.of("message", "只有任务分配的学生才能查看评价"));
            }
            
            // 检查任务是否已完成
            if (!task.getCompleted()) {
                return ResponseEntity.badRequest().body(Map.of("message", "任务尚未完成，无法查看评价"));
            }
            
            // 获取评价
            TaskEvaluationDTO evaluation = taskEvaluationService.getEvaluationByTaskId(taskId);
            
            if (evaluation == null) {
                return ResponseEntity.ok(Map.of("message", "任务尚未评价"));
            }
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("获取任务评价失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取任务评价失败: " + e.getMessage()));
        }
    }

    /**
     * 学生获取课题评价
     */
    @GetMapping("/user/projects/{projectId}/evaluation")
    @RequireRole("USER")
    public ResponseEntity<?> getProjectEvaluationForStudent(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String auth) {
        
        logger.info("学生请求获取课题评价, 课题ID: {}", projectId);
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            // 获取课题详情
            ProjectDTO project = projectService.getProjectById(projectId);
            
            // 检查权限（课题的学生可以查看）
            boolean isAssignee = project.getAssigneeId() != null && project.getAssigneeId().equals(user.getId());
            if (!isAssignee) {
                return ResponseEntity.badRequest().body(Map.of("message", "只有课题分配的学生才能查看评价"));
            }
            
            // 检查课题是否已完成
            if (!"已完成".equals(project.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "课题尚未完成，无法查看评价"));
            }
            
            // 获取评价
            ProjectEvaluationDTO evaluation = projectEvaluationService.getEvaluationByProjectId(projectId);
            
            if (evaluation == null) {
                return ResponseEntity.ok(Map.of("message", "课题尚未评价"));
            }
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("获取课题评价失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取课题评价失败: " + e.getMessage()));
        }
    }
} 