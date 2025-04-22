package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.ProjectDTO;
import com.example.auth.dto.TaskDTO;
import com.example.auth.entity.User;
import com.example.auth.service.ProjectService;
import com.example.auth.service.TaskService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ProjectService projectService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7); // 去除"Bearer "前缀
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }

            // 确保上传目录存在
            String directory = new File(uploadDir).getAbsolutePath();
            Path uploadPath = Paths.get(directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 更新用户头像URL
            String avatarUrl = "/uploads/" + filename;
            
            // 调用UserService保存头像URL
            userService.updateAvatar(user, avatarUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("url", avatarUrl);
            response.put("message", "头像上传成功");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "头像上传失败: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData,
                                          @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }

            // 调用服务层保存个人信息
            User updatedUser = userService.updateProfile(user, profileData);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", updatedUser.getId());
            userData.put("username", updatedUser.getUsername());
            userData.put("realName", updatedUser.getRealName());
            userData.put("nickname", updatedUser.getNickname());
            userData.put("email", updatedUser.getEmail());
            userData.put("phone", updatedUser.getPhone());
            userData.put("bio", updatedUser.getBio());
            userData.put("avatar", updatedUser.getAvatar());
            userData.put("roles", updatedUser.getRoles().stream().map(role -> role.getName()).toList());
            
            response.put("user", userData);
            response.put("message", "个人信息更新成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "个人信息更新失败: " + e.getMessage()));
        }
    }
    
    // ==================== 任务相关接口 ====================
    
    /**
     * 获取分配给当前用户的所有任务
     */
    @GetMapping("/tasks")
    @RequireRole("USER")
    public ResponseEntity<?> getMyTasks(@RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求查看自己的任务, 用户ID: {}", user.getId());
            
            // 获取分配给当前用户的任务
            List<TaskDTO> tasks = taskService.getTasksByAssignee(user.getId());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("获取用户任务失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取任务失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取分配给当前用户的指定状态的任务
     */
    @GetMapping("/tasks/status/{status}")
    @RequireRole("USER")
    public ResponseEntity<?> getMyTasksByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求查看自己的指定状态的任务, 用户ID: {}, 状态: {}", user.getId(), status);
            
            // 获取分配给当前用户的指定状态的任务
            List<TaskDTO> tasks = taskService.getTasksByAssigneeAndStatus(user.getId(), status);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("获取用户指定状态的任务失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取任务失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{id}")
    @RequireRole("USER")
    public ResponseEntity<?> getTaskDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                logger.error("获取任务详情失败: 用户不存在, 用户名: {}, 任务ID: {}", username, id);
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求查看任务详情, 用户ID: {}, 用户名: {}, 任务ID: {}", user.getId(), username, id);
            
            // 获取任务详情
            TaskDTO task;
            try {
                task = taskService.getTaskById(id);
                logger.info("获取任务成功, 任务ID: {}, 任务标题: {}, 分配给用户ID: {}, 督导ID: {}, 当前用户ID: {}", 
                    id, task.getTitle(), task.getAssigneeId(), task.getSupervisorId(), user.getId());
            } catch (Exception e) {
                logger.error("获取任务详情失败: 任务不存在或无法访问, 任务ID: {}", id);
                return ResponseEntity.badRequest().body(Map.of("message", "任务不存在或无法访问"));
            }
            
            // 验证任务访问权限
            // 允许三种情况：1. 用户是任务的assignee 2. assigneeId为null且用户是任务的supervisor 3. 用户是任务的supervisor
            boolean hasAccess = false;
            if (task.getAssigneeId() != null && user.getId().equals(task.getAssigneeId())) {
                // 用户是任务的assignee
                hasAccess = true;
                logger.info("用户是任务的指定学生, 允许访问, 用户ID: {}, 任务ID: {}", user.getId(), id);
            } else if (task.getAssigneeId() == null && user.getId().equals(task.getSupervisorId())) {
                // assigneeId为null且用户是任务的supervisor
                hasAccess = true;
                logger.info("任务未分配给任何学生, 但用户是任务的督导, 允许访问, 用户ID: {}, 任务ID: {}", user.getId(), id);
            } else if (user.getId().equals(task.getSupervisorId())) {
                // 用户是任务的supervisor
                hasAccess = true;
                logger.info("用户是任务的督导, 允许访问, 用户ID: {}, 任务ID: {}", user.getId(), id);
            }
            
            if (!hasAccess) {
                logger.error("获取任务详情失败: 无权查看该任务, 任务ID: {}, 任务分配给用户ID: {}, 任务督导ID: {}, 当前用户ID: {}", 
                    id, task.getAssigneeId(), task.getSupervisorId(), user.getId());
                return ResponseEntity.badRequest().body(Map.of("message", "无权查看该任务"));
            }
            
            logger.info("用户成功获取任务详情, 用户ID: {}, 任务ID: {}", user.getId(), id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("获取任务详情失败, 任务ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取任务详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新任务状态
     */
    @PutMapping("/tasks/{id}/status")
    @RequireRole("USER")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData,
            @RequestHeader("Authorization") String auth) {
        String status = statusData.get("status");
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求更新任务状态, 用户ID: {}, 任务ID: {}, 新状态: {}", user.getId(), id, status);
            
            // 获取任务详情
            TaskDTO task = taskService.getTaskById(id);
            
            // 验证任务是否分配给当前用户
            if (!user.getId().equals(task.getAssigneeId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权更新该任务"));
            }
            
            // 更新任务状态
            TaskDTO updatedTask = taskService.updateTaskStatus(id, status);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("更新任务状态失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "更新任务状态失败: " + e.getMessage()));
        }
    }
    
    // ==================== 课题相关接口 ====================
    
    /**
     * 获取分配给当前用户的所有课题
     */
    @GetMapping("/projects")
    @RequireRole("USER")
    public ResponseEntity<?> getMyProjects(@RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求查看自己的课题, 用户ID: {}", user.getId());
            
            // 获取分配给当前用户的课题
            List<ProjectDTO> projects = projectService.getProjectsByAssignee(user.getId());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("获取用户课题失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取课题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取分配给当前用户的指定状态的课题
     */
    @GetMapping("/projects/status/{status}")
    @RequireRole("USER")
    public ResponseEntity<?> getMyProjectsByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求查看自己的指定状态的课题, 用户ID: {}, 状态: {}", user.getId(), status);
            
            // 获取分配给当前用户的指定状态的课题
            List<ProjectDTO> projects = projectService.getProjectsByAssigneeAndStatus(user.getId(), status);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("获取用户指定状态的课题失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取课题失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取课题详情
     */
    @GetMapping("/projects/{id}")
    @RequireRole("USER")
    public ResponseEntity<?> getProjectDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求查看课题详情, 用户ID: {}, 课题ID: {}", user.getId(), id);
            
            // 获取课题详情
            ProjectDTO project = projectService.getProjectById(id);
            
            // 验证课题是否分配给当前用户
            if (!user.getId().equals(project.getAssigneeId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权查看该课题"));
            }
            
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            logger.error("获取课题详情失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取课题详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新课题状态
     */
    @PutMapping("/projects/{id}/status")
    @RequireRole("USER")
    public ResponseEntity<?> updateProjectStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData,
            @RequestHeader("Authorization") String auth) {
        String status = statusData.get("status");
        
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求更新课题状态, 用户ID: {}, 课题ID: {}, 新状态: {}", user.getId(), id, status);
            
            // 获取课题详情
            ProjectDTO project = projectService.getProjectById(id);
            
            // 验证课题是否分配给当前用户
            if (!user.getId().equals(project.getAssigneeId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "无权更新该课题"));
            }
            
            // 更新课题状态
            ProjectDTO updatedProject = projectService.updateProjectStatus(id, status);
            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            logger.error("更新课题状态失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "更新课题状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学生申请课题
     */
    @PostMapping("/projects/apply")
    @RequireRole("USER")
    public ResponseEntity<?> applyForProject(
            @RequestBody ProjectDTO projectDTO,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户申请课题, 用户ID: {}, 课题标题: {}", user.getId(), projectDTO.getTitle());
            
            // 设置申请人ID
            projectDTO.setAssigneeId(user.getId());
            
            // 申请课题
            ProjectDTO appliedProject = projectService.applyForProject(projectDTO);
            return ResponseEntity.ok(appliedProject);
        } catch (Exception e) {
            logger.error("申请课题失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "申请课题失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有督导员
     */
    @GetMapping("/supervisors")
    public ResponseEntity<?> getAllSupervisors() {
        try {
            List<User> supervisors = userService.getAllSupervisors();
            return ResponseEntity.ok(supervisors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取督导员列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取课题下的所有任务
     */
    @GetMapping("/projects/{id}/tasks")
    @RequireRole("USER")
    public ResponseEntity<?> getProjectTasks(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
            }
            
            logger.info("用户请求查看课题关联任务, 用户ID: {}, 课题ID: {}", user.getId(), id);
            
            // 获取课题详情以验证权限
            ProjectDTO project = projectService.getProjectById(id);
            
            // 检查用户是否是督导角色
            boolean isSupervisor = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equalsIgnoreCase("SUPERVISOR") || 
                              role.getName().equalsIgnoreCase("ADMIN"));
            
            // 验证课题是否属于当前用户，如果是督导则跳过这个验证
            if (!isSupervisor && !user.getId().equals(project.getAssigneeId())) {
                logger.warn("权限错误：用户 {} 尝试访问不属于他的课题 {} 的任务", user.getId(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "无权查看该课题的任务"));
            }
            
            // 获取课题下的任务列表
            List<TaskDTO> tasks = taskService.getBatchTasksByProject(id);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("获取课题任务失败", e);
            return ResponseEntity.badRequest().body(Map.of("message", "获取课题任务失败: " + e.getMessage()));
        }
    }
}