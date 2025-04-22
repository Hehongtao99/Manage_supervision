package com.example.auth.service.impl;

import com.example.auth.dto.TaskSubmissionDTO;
import com.example.auth.entity.Task;
import com.example.auth.entity.TaskSubmission;
import com.example.auth.entity.User;
import com.example.auth.repository.TaskRepository;
import com.example.auth.repository.TaskSubmissionRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ProjectService;
import com.example.auth.service.TaskSubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskSubmissionServiceImpl implements TaskSubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(TaskSubmissionServiceImpl.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectService projectService;

    @Override
    @Transactional
    public TaskSubmissionDTO submitTaskFile(Long taskId, MultipartFile file, Long submitterId, String comment) {
        logger.info("提交任务文件, 任务ID: {}, 提交者ID: {}", taskId, submitterId);
        
        try {
            // 验证任务是否存在
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (!taskOpt.isPresent()) {
                logger.error("任务不存在, 任务ID: {}", taskId);
                throw new RuntimeException("任务不存在");
            }
            
            Task task = taskOpt.get();
            logger.info("验证任务提交权限, 任务ID: {}, 任务分配给用户ID: {}, 任务督导ID: {}, 任务完成状态: {}, 提交者ID: {}", 
                taskId, task.getAssigneeId(), task.getSupervisorId(), task.getCompleted(), submitterId);
            
            // 验证提交者权限
            // 允许三种情况：
            // 1. 提交者是任务的assignee（无论任务状态如何）
            // 2. assigneeId为null且提交者是任务的supervisor
            // 3. 提交者是任务的supervisor
            boolean hasAccess = false;
            
            if (task.getAssigneeId() != null && submitterId.equals(task.getAssigneeId())) {
                // 提交者是任务的assignee（即使任务已完成也允许上传）
                hasAccess = true;
                logger.info("提交者是任务的指定学生, 允许提交, 提交者ID: {}, 任务ID: {}, 任务是否完成: {}", 
                    submitterId, taskId, task.getCompleted());
            } else if (task.getAssigneeId() == null && submitterId.equals(task.getSupervisorId())) {
                // assigneeId为null且提交者是任务的supervisor
                hasAccess = true;
                logger.info("任务未分配给任何学生, 但提交者是任务的督导, 允许提交, 提交者ID: {}, 任务ID: {}", submitterId, taskId);
            } else if (submitterId.equals(task.getSupervisorId())) {
                // 提交者是任务的supervisor
                hasAccess = true;
                logger.info("提交者是任务的督导, 允许提交, 提交者ID: {}, 任务ID: {}", submitterId, taskId);
            }
            
            if (!hasAccess) {
                logger.error("提交任务文件失败: 无权提交该任务, 任务ID: {}, 任务分配给用户ID: {}, 任务督导ID: {}, 提交者ID: {}, 提交者和assigneeId是否匹配: {}, 提交者和supervisorId是否匹配: {}", 
                    taskId, task.getAssigneeId(), task.getSupervisorId(), submitterId, 
                    submitterId.equals(task.getAssigneeId()), submitterId.equals(task.getSupervisorId()));
                throw new RuntimeException("无权提交该任务");
            }
            
            // 确保上传根目录存在
            String baseDirectory = new File(uploadDir + "/task_submissions").getAbsolutePath();
            Path baseUploadPath = Paths.get(baseDirectory);
            if (!Files.exists(baseUploadPath)) {
                Files.createDirectories(baseUploadPath);
            }
            
            // 创建任务特定的目录 - 这是修改的关键点，为每个任务创建独立目录
            String taskSpecificDirectory = baseDirectory + "/task_" + taskId;
            Path taskUploadPath = Paths.get(taskSpecificDirectory);
            if (!Files.exists(taskUploadPath)) {
                Files.createDirectories(taskUploadPath);
                logger.info("已创建任务专属目录: {}", taskSpecificDirectory);
            }
            
            // 获取文件信息
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileType = file.getContentType();
            
            // 检查并截断文件类型，以防数据库字段溢出
            if (fileType != null && fileType.length() > 255) {
                logger.warn("文件类型字符串过长，将被截断: {}", fileType);
                fileType = fileType.substring(0, 255);
            }
            
            long fileSize = file.getSize();
            
            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = taskUploadPath.resolve(filename);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 创建提交记录，使用任务特定的路径
            TaskSubmission submission = new TaskSubmission();
            submission.setTaskId(taskId);
            submission.setFilePath("/uploads/task_submissions/task_" + taskId + "/" + filename);
            submission.setOriginalFilename(originalFilename);
            submission.setFileType(fileType);
            submission.setFileSize(fileSize);
            submission.setSubmitterId(submitterId);
            submission.setComment(comment);
            
            // 保存提交记录
            TaskSubmission savedSubmission = taskSubmissionRepository.save(submission);
            
            // 更新任务状态为已完成（如果尚未完成）
            if (!task.getCompleted()) {
                logger.info("更新任务状态为已完成, 任务ID: {}", taskId);
                task.setCompleted(true);
                taskRepository.save(task);
            } else {
                logger.info("任务已处于完成状态，不需要更新, 任务ID: {}", taskId);
            }
            
            // 更新项目状态
            if (task.getProjectId() != null) {
                projectService.updateProjectStatusBasedOnTasks(task.getProjectId());
            }
            
            // 获取任务标题和提交者名称
            String taskTitle = task.getTitle();
            String submitterName = getUserNameById(submitterId);
            
            logger.info("任务文件提交成功, 任务ID: {}, 提交者ID: {}, 提交ID: {}", 
                taskId, submitterId, savedSubmission.getId());
            
            return TaskSubmissionDTO.fromEntity(savedSubmission, taskTitle, submitterName);
        } catch (IOException e) {
            logger.error("文件上传失败, 任务ID: {}, 提交者ID: {}, 错误: {}", taskId, submitterId, e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("提交任务文件失败, 任务ID: {}, 提交者ID: {}, 错误: {}", taskId, submitterId, e.getMessage(), e);
            throw new RuntimeException("提交任务文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<TaskSubmissionDTO> getSubmissionsByTaskId(Long taskId, Long userId) {
        logger.info("获取任务提交记录, 任务ID: {}, 用户ID: {}", taskId, userId);
        
        try {
            // 获取任务详情
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (!taskOpt.isPresent()) {
                logger.error("任务不存在, 任务ID: {}", taskId);
                throw new RuntimeException("任务不存在");
            }
            
            Task task = taskOpt.get();
            String taskTitle = task.getTitle();
            
            // 权限检查
            boolean isSupervisor = userId.equals(task.getSupervisorId());
            boolean isAssignee = userId.equals(task.getAssigneeId());
            
            logger.info("检查文件访问权限: 用户ID: {}, 是否为督导: {}, 是否为该任务指定学生: {}", 
                userId, isSupervisor, isAssignee);
            
            if (!isSupervisor && !isAssignee) {
                // 既不是督导也不是任务指定学生，返回空列表
                logger.warn("用户无权查看该任务的提交记录, 用户ID: {}, 任务ID: {}", userId, taskId);
                return new ArrayList<>();
            }
            
            // 获取提交记录
            List<TaskSubmission> submissions = taskSubmissionRepository.findByTaskId(taskId);
            
            return submissions.stream()
                .map(submission -> {
                    String submitterName = getUserNameById(submission.getSubmitterId());
                    return TaskSubmissionDTO.fromEntity(submission, taskTitle, submitterName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取任务提交记录失败", e);
            throw new RuntimeException("获取任务提交记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<TaskSubmissionDTO> getSubmissionsBySubmitterId(Long submitterId) {
        logger.info("获取用户提交记录, 提交者ID: {}", submitterId);
        
        try {
            List<TaskSubmission> submissions = taskSubmissionRepository.findBySubmitterId(submitterId);
            
            return submissions.stream()
                .map(submission -> {
                    // 获取任务标题
                    Optional<Task> taskOpt = taskRepository.findById(submission.getTaskId());
                    String taskTitle = taskOpt.isPresent() ? taskOpt.get().getTitle() : "未知任务";
                    
                    String submitterName = getUserNameById(submitterId);
                    return TaskSubmissionDTO.fromEntity(submission, taskTitle, submitterName);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取用户提交记录失败", e);
            throw new RuntimeException("获取用户提交记录失败: " + e.getMessage());
        }
    }

    @Override
    public TaskSubmissionDTO getSubmissionById(Long submissionId) {
        logger.info("获取提交详情, 提交ID: {}", submissionId);
        
        try {
            Optional<TaskSubmission> submissionOpt = taskSubmissionRepository.findById(submissionId);
            
            if (submissionOpt.isPresent()) {
                TaskSubmission submission = submissionOpt.get();
                
                // 获取任务标题
                Optional<Task> taskOpt = taskRepository.findById(submission.getTaskId());
                String taskTitle = taskOpt.isPresent() ? taskOpt.get().getTitle() : "未知任务";
                
                String submitterName = getUserNameById(submission.getSubmitterId());
                
                return TaskSubmissionDTO.fromEntity(submission, taskTitle, submitterName);
            } else {
                throw new RuntimeException("提交记录不存在");
            }
        } catch (Exception e) {
            logger.error("获取提交详情失败", e);
            throw new RuntimeException("获取提交详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteSubmission(Long submissionId) {
        logger.info("删除提交记录, 提交ID: {}", submissionId);
        
        try {
            Optional<TaskSubmission> submissionOpt = taskSubmissionRepository.findById(submissionId);
            
            if (submissionOpt.isPresent()) {
                TaskSubmission submission = submissionOpt.get();
                Long taskId = submission.getTaskId();
                
                // 删除文件
                String filePath = submission.getFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    // 从路径中移除"/uploads/"前缀
                    String relativePath = filePath.replaceFirst("^/uploads/", "");
                    Path path = Paths.get(uploadDir, relativePath);
                    try {
                        Files.deleteIfExists(path);
                        logger.info("文件删除成功: {}", path);
                    } catch (IOException e) {
                        logger.warn("删除文件失败: {}", path, e);
                    }
                }
                
                // 获取任务信息
                Optional<Task> taskOpt = taskRepository.findById(taskId);
                
                // 删除提交记录
                taskSubmissionRepository.deleteById(submissionId);
                logger.info("提交记录删除成功: 提交ID={}, 任务ID={}", submissionId, taskId);
                
                // 获取任务现有的提交记录数量
                long submissionCount = taskSubmissionRepository.countByTaskId(taskId);
                
                // 更新任务状态
                if (taskOpt.isPresent()) {
                    Task task = taskOpt.get();
                    boolean hasSubmissions = submissionCount > 0;
                    
                    // 只有当完成状态需要改变时才更新
                    if (task.getCompleted() != hasSubmissions) {
                        task.setCompleted(hasSubmissions);
                        taskRepository.save(task);
                        logger.info("任务完成状态已更新为: {}, 任务ID: {}", hasSubmissions, taskId);
                    }
                    
                    // 更新项目状态
                    if (task.getProjectId() != null) {
                        projectService.updateProjectStatusBasedOnTasks(task.getProjectId());
                        logger.info("项目状态已更新, 项目ID: {}", task.getProjectId());
                    }
                }
                
                return true;
            } else {
                logger.warn("提交记录不存在, 提交ID: {}", submissionId);
                return false;
            }
        } catch (Exception e) {
            logger.error("删除提交记录失败", e);
            throw new RuntimeException("删除提交记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public TaskSubmissionDTO updateSubmissionComment(Long submissionId, String comment) {
        logger.info("更新提交评论, 提交ID: {}", submissionId);
        
        try {
            Optional<TaskSubmission> submissionOpt = taskSubmissionRepository.findById(submissionId);
            
            if (submissionOpt.isPresent()) {
                TaskSubmission submission = submissionOpt.get();
                
                // 只更新评论，不影响其他字段
                submission.setComment(comment);
                
                TaskSubmission updatedSubmission = taskSubmissionRepository.save(submission);
                logger.info("提交评论更新成功, 提交ID: {}", submissionId);
                
                // 获取任务标题
                Optional<Task> taskOpt = taskRepository.findById(submission.getTaskId());
                String taskTitle = taskOpt.isPresent() ? taskOpt.get().getTitle() : "未知任务";
                
                String submitterName = getUserNameById(submission.getSubmitterId());
                
                return TaskSubmissionDTO.fromEntity(updatedSubmission, taskTitle, submitterName);
            } else {
                logger.warn("提交记录不存在, 提交ID: {}", submissionId);
                throw new RuntimeException("提交记录不存在");
            }
        } catch (Exception e) {
            logger.error("更新提交评论失败", e);
            throw new RuntimeException("更新提交评论失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isTaskSubmitted(Long taskId) {
        return taskSubmissionRepository.findByTaskId(taskId).size() > 0;
    }
    
    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            logger.info("尝试加载文件资源: {}", filePath);
            
            // 1. 先尝试直接加载原始路径
            String relativePath = filePath.replaceFirst("^/uploads/", "");
            relativePath = relativePath.replaceFirst("^uploads/", ""); // 移除可能存在的uploads前缀
            Path path = Paths.get(uploadDir, relativePath);
            
            logger.debug("尝试路径1 (原始路径): {}", path.toString());
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                logger.info("文件已找到: {}", path.toString());
                return resource;
            }
            
            // 2. 如果直接路径不存在，尝试各种替代路径
            List<Path> possiblePaths = new ArrayList<>();
            
            // 2.1 如果路径包含任务目录(task_X)，尝试去掉这一层
            if (filePath.contains("/task_")) {
                String pathWithoutTaskDir = filePath.replaceFirst("/task_submissions/task_\\d+/", "/task_submissions/");
                String relPathWithoutTaskDir = pathWithoutTaskDir.replaceFirst("^/uploads/", "");
                relPathWithoutTaskDir = relPathWithoutTaskDir.replaceFirst("^uploads/", "");
                Path pathAlternative = Paths.get(uploadDir, relPathWithoutTaskDir);
                possiblePaths.add(pathAlternative);
                logger.debug("尝试路径2 (无任务目录): {}", pathAlternative.toString());
            }
            
            // 2.2 获取文件名，尝试在所有task_submissions下的子目录中查找
            String filename = path.getFileName().toString();
            File uploadDirFile = new File(uploadDir);
            if (uploadDirFile.exists() && uploadDirFile.isDirectory()) {
                // 查找task_submissions目录
                File taskSubmissionsDir = new File(uploadDirFile, "task_submissions");
                if (taskSubmissionsDir.exists() && taskSubmissionsDir.isDirectory()) {
                    // 在task_submissions下直接查找文件
                    Path directPath = Paths.get(taskSubmissionsDir.getAbsolutePath(), filename);
                    possiblePaths.add(directPath);
                    logger.debug("尝试路径3 (直接在task_submissions中): {}", directPath.toString());
                    
                    // 在各个task_X子目录中查找
                    File[] taskDirs = taskSubmissionsDir.listFiles(file -> file.isDirectory() && file.getName().startsWith("task_"));
                    if (taskDirs != null) {
                        for (File taskDir : taskDirs) {
                            Path taskDirPath = Paths.get(taskDir.getAbsolutePath(), filename);
                            possiblePaths.add(taskDirPath);
                            logger.debug("尝试路径4 (任务子目录): {}", taskDirPath.toString());
                        }
                    }
                }
                
                // 直接在上传根目录查找
                Path rootDirPath = Paths.get(uploadDirFile.getAbsolutePath(), filename);
                possiblePaths.add(rootDirPath);
                logger.debug("尝试路径5 (上传根目录): {}", rootDirPath.toString());
            }
            
            // 2.3 检查所有可能的路径
            for (Path possiblePath : possiblePaths) {
                try {
                    Resource possibleResource = new UrlResource(possiblePath.toUri());
                    if (possibleResource.exists()) {
                        logger.info("文件已找到在替代路径: {}", possiblePath.toString());
                        return possibleResource;
                    }
                } catch (Exception e) {
                    logger.warn("检查替代路径时出错: {}, 错误: {}", possiblePath, e.getMessage());
                }
            }
            
            // 3. 如果还是找不到，提供详细的错误信息
            logger.error("文件不存在，已尝试各种路径: 原始路径={}, 可能的替代路径={}", path, possiblePaths);
            throw new RuntimeException("文件不存在: " + filePath + "，已尝试多种路径但均未找到");
        } catch (MalformedURLException e) {
            logger.error("文件路径无效: {}, 错误: {}", filePath, e.getMessage());
            throw new RuntimeException("文件路径无效: " + filePath, e);
        } catch (Exception e) {
            logger.error("加载文件资源时发生未知错误: {}, 错误: {}", filePath, e.getMessage());
            throw new RuntimeException("加载文件资源时发生未知错误: " + filePath, e);
        }
    }
    
    /**
     * 根据用户ID获取用户名
     */
    private String getUserNameById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return user.getRealName() != null ? user.getRealName() : user.getUsername();
            }
        } catch (Exception e) {
            logger.warn("获取用户名失败, ID: {}", userId, e);
        }
        
        return "未知用户";
    }
} 