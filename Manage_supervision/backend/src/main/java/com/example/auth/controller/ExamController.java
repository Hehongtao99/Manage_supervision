package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.dto.ExamDTO;
import com.example.auth.dto.ExamQuestionDTO;
import com.example.auth.dto.ExamStudentDTO;
import com.example.auth.dto.ExamPendingGradingDTO;
import com.example.auth.dto.StudentScoreDTO;
import com.example.auth.entity.User;
import com.example.auth.entity.Exam;
import com.example.auth.entity.ExamStudent;
import com.example.auth.entity.ExamQuestion;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.service.ExamService;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import com.example.auth.util.ResponseUtil;
import com.example.auth.repository.ExamRepository;
import com.example.auth.repository.ExamStudentRepository;
import com.example.auth.repository.ExamQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 考试控制器
 */
@RestController
@RequestMapping("/api")
public class ExamController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private ExamStudentRepository examStudentRepository;
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    /**
     * 创建考试
     */
    @PostMapping("/supervisor/exams")
    @RequireRole({"SUPERVISOR"})
    public ResponseEntity<?> createExam(
            @RequestBody ExamDTO examDTO,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师创建考试: {}, 教师ID: {}", examDTO.getTitle(), user.getId());
            
            // 创建考试
            ExamDTO createdExam = examService.createExam(examDTO, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success(createdExam));
        } catch (Exception e) {
            logger.error("创建考试失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("创建考试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新考试
     */
    @PutMapping("/supervisor/exams/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateExam(
            @PathVariable Long id,
            @RequestBody ExamDTO examDTO,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师更新考试: {}, 教师ID: {}", id, user.getId());
            
            // 更新考试
            ExamDTO updatedExam = examService.updateExam(id, examDTO, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success(updatedExam));
        } catch (Exception e) {
            logger.error("更新考试失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("更新考试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除考试
     */
    @DeleteMapping("/supervisor/exams/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteExam(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师删除考试: {}, 教师ID: {}", id, user.getId());
            
            // 删除考试
            examService.deleteExam(id, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success("删除成功"));
        } catch (Exception e) {
            logger.error("删除考试失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("删除考试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取考试详情
     */
    @GetMapping("/supervisor/exams/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getExamById(@PathVariable Long id) {
        try {
            logger.info("获取考试详情: {}", id);
            
            // 获取考试详情
            ExamDTO exam = examService.getExamById(id);
            
            return ResponseEntity.ok(ResponseUtil.success(exam));
        } catch (Exception e) {
            logger.error("获取考试详情失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取考试详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取教师创建的考试列表（分页）
     */
    @GetMapping("/supervisor/exams")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getExamsByTeacher(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师获取考试列表, 教师ID: {}, 状态: {}, 关键词: {}", user.getId(), status, keyword);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            
            // 获取考试列表
            Page<ExamDTO> exams;
            if (keyword != null && !keyword.isEmpty()) {
                // 根据关键词搜索
                exams = examService.searchExams(keyword, user.getId(), pageable);
            } else if (status != null && !status.isEmpty()) {
                // 根据状态筛选
                exams = examService.getExamsByCreatorIdAndStatus(user.getId(), status, pageable);
            } else {
                // 获取全部
                exams = examService.getExamsByCreatorId(user.getId(), pageable);
            }
            
            return ResponseEntity.ok(ResponseUtil.success(exams));
        } catch (Exception e) {
            logger.error("获取考试列表失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取考试列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取教师创建的所有考试简要信息（用于下拉列表）
     */
    @GetMapping("/supervisor/exams/options")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getExamOptionsByTeacher(
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师获取考试选项列表, 教师ID: {}", user.getId());
            
            // 获取该教师创建的所有考试
            List<Exam> exams = examRepository.findByCreatorId(user.getId());
            
            // 转换为只包含ID和Title的Map列表
            List<Map<String, Object>> examOptions = exams.stream()
                .map(exam -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", exam.getId());
                    map.put("label", exam.getTitle());
                    return map;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ResponseUtil.success(examOptions));
        } catch (Exception e) {
            logger.error("获取考试选项列表失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取考试选项列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发布考试
     */
    @PostMapping("/supervisor/exams/{id}/publish")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> publishExam(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师发布考试: {}, 教师ID: {}", id, user.getId());
            
            // 发布考试
            ExamDTO publishedExam = examService.publishExam(id, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success(publishedExam));
        } catch (Exception e) {
            logger.error("发布考试失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("发布考试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取考试题目列表
     */
    @GetMapping("/supervisor/exams/{id}/questions")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getExamQuestions(@PathVariable Long id) {
        try {
            logger.info("获取考试题目列表: {}", id);
            
            // 获取考试题目列表
            List<ExamQuestionDTO> questions = examService.getExamQuestions(id);
            
            return ResponseEntity.ok(ResponseUtil.success(questions));
        } catch (Exception e) {
            logger.error("获取考试题目列表失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取考试题目列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取考试学生列表
     */
    @GetMapping("/supervisor/exams/{id}/students")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getExamStudents(@PathVariable Long id) {
        try {
            logger.info("获取考试学生列表: {}", id);
            
            // 获取考试学生列表
            List<ExamStudentDTO> students = examService.getExamStudents(id);
            
            return ResponseEntity.ok(ResponseUtil.success(students));
        } catch (Exception e) {
            logger.error("获取考试学生列表失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取考试学生列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 添加考试题目
     */
    @PostMapping("/supervisor/exams/{id}/questions")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> addExamQuestions(
            @PathVariable Long id,
            @RequestBody List<ExamQuestionDTO> questions,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师添加考试题目: {}, 教师ID: {}", id, user.getId());
            
            // 添加考试题目
            examService.addExamQuestions(id, questions, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success("添加成功"));
        } catch (Exception e) {
            logger.error("添加考试题目失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("添加考试题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 添加考试学生
     */
    @PostMapping("/supervisor/exams/{id}/students")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> addExamStudents(
            @PathVariable Long id,
            @RequestBody List<ExamStudentDTO> students,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师添加考试学生: {}, 教师ID: {}", id, user.getId());
            
            // 添加考试学生
            examService.addExamStudents(id, students, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success("添加成功"));
        } catch (Exception e) {
            logger.error("添加考试学生失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("添加考试学生失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除考试题目
     */
    @DeleteMapping("/supervisor/exams/{examId}/questions/{questionId}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> removeExamQuestion(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师删除考试题目: {}, 题目ID: {}, 教师ID: {}", examId, questionId, user.getId());
            
            // 删除考试题目
            examService.removeExamQuestion(examId, questionId, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success("删除成功"));
        } catch (Exception e) {
            logger.error("删除考试题目失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("删除考试题目失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除考试学生
     */
    @DeleteMapping("/supervisor/exams/{examId}/students/{studentId}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> removeExamStudent(
            @PathVariable Long examId,
            @PathVariable Long studentId,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师删除考试学生: {}, 学生ID: {}, 教师ID: {}", examId, studentId, user.getId());
            
            // 删除考试学生
            examService.removeExamStudent(examId, studentId, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success("删除成功"));
        } catch (Exception e) {
            logger.error("删除考试学生失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("删除考试学生失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学生获取可参加的考试列表
     */
    @GetMapping("/student/exams")
    @RequireRole("USER")
    public ResponseEntity<?> getStudentExams(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前学生用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                logger.error("获取学生考试列表失败：用户不存在, token: {}, username: {}", 
                        token.substring(0, Math.min(10, token.length())), username);
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("学生获取考试列表, 学生ID: {}, 用户名: {}, token中提取的ID: {}, 状态: {}, 页码: {}, 大小: {}", 
                    user.getId(), username, userId, status, page, size);
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
            
            try {
                // 临时方法：检查数据库中是否有该学生的考试关联记录
                Long examStudentCount = examService.countExamsByStudentId(user.getId());
                logger.info("学生关联的考试数量: {}", examStudentCount);
                
                // 如果学生没有关联考试记录，返回空列表
                if (examStudentCount == 0) {
                    logger.warn("学生没有关联任何考试, 学生ID: {}", user.getId());
                    return ResponseEntity.ok(ResponseUtil.success(Page.empty(pageable)));
                }
                
                // 获取分配给学生的考试列表
                Page<ExamDTO> exams = examService.getExamsByStudentId(user.getId(), status, pageable);
                
                // 检查返回的数据是否为null
                if (exams == null) {
                    logger.warn("服务返回的考试列表为null");
                    return ResponseEntity.ok(ResponseUtil.success(Page.empty(pageable)));
                }
                
                // 打印返回数据的详细信息
                logger.info("考试列表页码信息: 当前页={}, 每页大小={}, 总页数={}, 总元素数={}",
                        exams.getNumber(), exams.getSize(), exams.getTotalPages(), exams.getTotalElements());
                
                logger.info("返回考试数量: {}, 总数: {}", 
                        exams.getContent().size(), exams.getTotalElements());
                
                // 检查内容是否为空
                if (exams.getContent().isEmpty()) {
                    logger.warn("考试列表内容为空");
                } else {
                    // 打印每一条考试记录的关键信息
                    for (ExamDTO exam : exams.getContent()) {
                        logger.info("考试记录: ID={}, 标题={}, 状态={}, 学生状态={}",
                                exam.getId(), exam.getTitle(), exam.getStatus(), exam.getStudentStatus());
                    }
                }
                
                // 创建一个空Map来保存调试信息
                Map<String, Object> result = new HashMap<>();
                result.put("page", exams);
                result.put("debug", Map.of(
                    "pageNumber", exams.getNumber(),
                    "pageSize", exams.getSize(),
                    "totalPages", exams.getTotalPages(),
                    "totalElements", exams.getTotalElements(),
                    "content", exams.getContent().isEmpty() ? "empty" : "has-content",
                    "examCount", exams.getContent().size()
                ));
                
                // 验证返回的数据是否正确
                if (exams.getContent().isEmpty() && exams.getTotalElements() > 0) {
                    logger.warn("分页数据异常: 总数为{}但内容为空", exams.getTotalElements());
                }
                
                // 直接返回exams对象，而不是result，以避免改变原有的返回格式
                return ResponseEntity.ok(ResponseUtil.success(exams));
            } catch (Exception e) {
                logger.error("获取学生考试列表服务异常", e);
                return ResponseEntity.ok(ResponseUtil.success(Page.empty(pageable)));
            }
        } catch (Exception e) {
            logger.error("获取学生考试列表控制器异常", e);
            return ResponseEntity.ok(ResponseUtil.success(Page.empty()));
        }
    }
    
    /**
     * 学生获取考试详情
     */
    @GetMapping("/student/exams/{id}")
    @RequireRole("USER")
    public ResponseEntity<?> getStudentExamDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前学生用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("学生获取考试详情, 学生ID: {}, 考试ID: {}", user.getId(), id);
            
            // 检查学生是否有权限参加该考试
            if (!examService.isStudentAssignedToExam(id, user.getId())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("您没有权限参加此考试"));
            }
            
            // 获取考试详情
            ExamDTO exam = examService.getExamById(id);
            
            // 获取学生的考试状态
            ExamStudentDTO examStudentStatus = examService.getExamStudentStatus(id, user.getId());
            
            Map<String, Object> result = Map.of(
                "exam", exam,
                "studentStatus", examStudentStatus
            );
            
            return ResponseEntity.ok(ResponseUtil.success(result));
        } catch (Exception e) {
            logger.error("获取学生考试详情失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取考试详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学生开始考试
     */
    @PostMapping("/student/exams/{id}/start")
    @RequireRole("USER")
    public ResponseEntity<?> startExam(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前学生用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("学生开始考试, 学生ID: {}, 考试ID: {}", user.getId(), id);
            
            // 检查学生是否有权限参加该考试
            if (!examService.isStudentAssignedToExam(id, user.getId())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("您没有权限参加此考试"));
            }
            
            // 获取考试详情
            ExamDTO exam = examService.getExamById(id);
            
            // 检查考试是否在有效时间内
            Instant now = Instant.now();
            if (now.isBefore(exam.getStartTime())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("考试尚未开始"));
            }
            
            if (now.isAfter(exam.getEndTime())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("考试已经结束"));
            }
            
            // 获取学生的考试状态
            ExamStudentDTO examStudentStatus = examService.getExamStudentStatus(id, user.getId());
            
            // 检查考试是否已经开始或完成
            if ("SUBMITTED".equals(examStudentStatus.getStatus()) || "GRADED".equals(examStudentStatus.getStatus())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("您已经提交过该考试"));
            }
            
            // 更新考试状态为进行中
            examStudentStatus = examService.startExam(id, user.getId());
            
            // 获取考试题目
            List<ExamQuestionDTO> questions = examService.getExamQuestionsForStudent(id);
            
            Map<String, Object> result = Map.of(
                "examStatus", examStudentStatus,
                "questions", questions
            );
            
            return ResponseEntity.ok(ResponseUtil.success(result));
        } catch (Exception e) {
            logger.error("开始考试失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("开始考试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学生提交考试答案
     */
    @PostMapping("/student/exams/{id}/submit")
    @RequireRole("USER")
    public ResponseEntity<?> submitExam(
            @PathVariable Long id,
            @RequestBody Map<String, String> answers,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前学生用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("学生提交考试答案, 学生ID: {}, 考试ID: {}", user.getId(), id);
            
            // 检查学生是否有权限参加该考试
            if (!examService.isStudentAssignedToExam(id, user.getId())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("您没有权限参加此考试"));
            }
            
            // 获取考试详情
            ExamDTO exam = examService.getExamById(id);
            
            // 检查考试是否已经结束
            Instant now = Instant.now();
            if (now.isAfter(exam.getEndTime())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("考试已经结束，无法提交"));
            }
            
            // 获取学生的考试状态
            ExamStudentDTO examStudentStatus = examService.getExamStudentStatus(id, user.getId());
            
            // 检查考试是否已经提交或评分
            if ("SUBMITTED".equals(examStudentStatus.getStatus()) || "GRADED".equals(examStudentStatus.getStatus())) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("您已经提交过该考试"));
            }
            
            // 提交答案并计算分数
            ExamStudentDTO result = examService.submitExam(id, user.getId(), answers);
            
            return ResponseEntity.ok(ResponseUtil.success(result));
        } catch (Exception e) {
            logger.error("提交考试答案失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("提交考试答案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 调试API：创建测试考试数据
     */
    @PostMapping("/student/exams/test-data")
    @RequireRole("USER")
    public ResponseEntity<?> createTestExamData(@RequestHeader("Authorization") String auth) {
        try {
            // 获取当前学生用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("为学生创建测试考试数据, 学生ID: {}", user.getId());
            
            // 创建考试（简化流程，直接在数据库中插入数据）
            Exam exam = new Exam();
            exam.setTitle("测试考试 - " + System.currentTimeMillis());
            exam.setDescription("这是一个测试考试，用于调试目的");
            exam.setStatus("PUBLISHED");
            exam.setStartTime(Instant.now().minusSeconds(3600)); // 开始时间为1小时前
            exam.setEndTime(Instant.now().plusSeconds(3600));    // 结束时间为1小时后
            exam.setDuration(120);                               // 考试时长120分钟
            exam.setPassingScore(new BigDecimal("60.0"));        // 及格分数60分
            exam.setTotalScore(new BigDecimal("100.0"));         // 总分100分
            exam.setCreatorId(1L);                               // 假设创建者ID为1
            exam = examRepository.save(exam);
            
            // 创建学生与考试的关联
            ExamStudent examStudent = new ExamStudent();
            examStudent.setExamId(exam.getId());
            examStudent.setStudentId(user.getId());
            examStudent.setStatus("NOT_STARTED");
            examStudentRepository.save(examStudent);
            
            // 添加一些考试题目
            for (int i = 1; i <= 5; i++) {
                ExamQuestion question = new ExamQuestion();
                question.setExamId(exam.getId());
                question.setQuestionId((long)i);
                question.setQuestionTitle("测试题目 " + i);
                question.setQuestionType(i % 2 == 0 ? "SINGLE_CHOICE" : "ESSAY");
                question.setQuestionScore(new BigDecimal("20.0"));
                question.setDisplayOrder(i - 1);
                examQuestionRepository.save(question);
            }
            
            return ResponseEntity.ok(ResponseUtil.success("测试数据创建成功，考试ID: " + exam.getId()));
        } catch (Exception e) {
            logger.error("创建测试考试数据失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("创建测试数据失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取学生考试答案详情（教师批阅用）
     */
    @GetMapping("/supervisor/exams/{examId}/students/{studentId}/answers")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getStudentExamAnswers(
            @PathVariable Long examId,
            @PathVariable Long studentId,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师获取学生考试答案: 考试ID={}, 学生ID={}, 教师ID={}", examId, studentId, user.getId());
            
            // 获取学生考试答案
            Map<String, Object> result = examService.getStudentExamAnswers(examId, studentId, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success(result));
        } catch (Exception e) {
            logger.error("获取学生考试答案失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取学生考试答案失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批阅学生考试
     */
    @PostMapping("/supervisor/exams/{examId}/students/{studentId}/grade")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> gradeStudentExam(
            @PathVariable Long examId,
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> gradeData,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师批阅学生考试: 考试ID={}, 学生ID={}, 教师ID={}", examId, studentId, user.getId());
            
            // 批阅学生考试
            ExamStudentDTO result = examService.gradeStudentExam(examId, studentId, gradeData, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success(result));
        } catch (Exception e) {
            logger.error("批阅学生考试失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("批阅学生考试失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发布学生成绩
     */
    @PostMapping("/supervisor/exams/{examId}/students/publish")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> publishStudentGrades(
            @PathVariable Long examId,
            @RequestParam(required = false) Long studentId,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师发布学生成绩: 考试ID={}, 学生ID={}, 教师ID={}", examId, studentId, user.getId());
            
            // 发布学生成绩
            List<ExamStudentDTO> result = examService.publishStudentGrades(examId, studentId, user.getId());
            
            return ResponseEntity.ok(ResponseUtil.success(result));
        } catch (Exception e) {
            logger.error("发布学生成绩失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("发布学生成绩失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取教师名下有待批阅学生的考试列表
     */
    @GetMapping("/supervisor/exams/pending-grading")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getPendingGradingExams(
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }

            logger.info("教师获取待批阅考试列表, 教师ID: {}", user.getId());

            // 获取待批阅考试列表
            List<ExamPendingGradingDTO> pendingExams = examService.getExamsWithPendingGrading(user.getId());

            return ResponseEntity.ok(ResponseUtil.success(pendingExams));
        } catch (Exception e) {
            logger.error("获取待批阅考试列表失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取待批阅考试列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取指定考试的学生成绩列表（分页和排序）
     */
    @GetMapping("/supervisor/exams/{examId}/scores")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getExamScores(
            @PathVariable Long examId,
            @RequestHeader("Authorization") String auth,
            @PageableDefault(size = 10, sort = "score", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ResponseUtil.error("用户不存在"));
            }
            
            logger.info("教师获取考试成绩列表: 考试ID={}, 教师ID={}, 分页: {}", examId, user.getId(), pageable);
            
            // 获取成绩列表
            Page<StudentScoreDTO> scores = examService.getExamScores(examId, user.getId(), pageable);
            
            return ResponseEntity.ok(ResponseUtil.success(scores));
        } catch (Exception e) {
            logger.error("获取考试成绩列表失败", e);
            return ResponseEntity.badRequest().body(ResponseUtil.error("获取考试成绩列表失败: " + e.getMessage()));
        }
    }

    /**
     * 学生获取自己的考试结果详情
     */
    @GetMapping("/student/exams/{examId}/answers")
    @RequireRole("USER")
    public ResponseEntity<?> getStudentExamResultDetails(
            @PathVariable Long examId,
            @RequestHeader("Authorization") String auth) {
        try {
            // 获取当前学生用户 ID
            String token = auth.substring(7);
            Long studentId = jwtUtil.getUserIdFromToken(token); // 从 Token 获取学生 ID
            // 可以选择性地再查一次 User 确保用户存在
            User user = userService.findById(studentId);
            if (user == null) {
                 return ResponseEntity.status(401).body(ResponseUtil.error("用户认证失败或不存在"));
            }

            logger.info("学生获取考试结果详情: 考试ID={}, 学生ID={}", examId, studentId);

            // 调用 Service 获取结果
            Map<String, Object> result = examService.getStudentExamResultDetails(examId, studentId);

            return ResponseEntity.ok(ResponseUtil.success(result));
        } catch (ResourceNotFoundException e) {
             logger.warn("获取学生考试结果失败: {}", e.getMessage());
             return ResponseEntity.status(404).body(ResponseUtil.error(e.getMessage()));
        } catch (IllegalStateException e) {
             logger.warn("获取学生考试结果失败: {}", e.getMessage());
             return ResponseEntity.badRequest().body(ResponseUtil.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("获取学生考试结果详情时发生内部错误", e);
            return ResponseEntity.internalServerError().body(ResponseUtil.error("获取考试结果失败: " + e.getMessage()));
        }
    }

    /**
     * 获取教师的批阅概览考试列表（分页和筛选）
     */
    @GetMapping("/supervisor/exams/grading-overview")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getGradingOverviewExams(
            @RequestParam(required = false) String gradingStatus, // pending, graded
            @RequestHeader("Authorization") String auth,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            // 获取当前用户
            String token = auth.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body(ResponseUtil.error("用户认证失败或不存在"));
            }

            logger.info("教师获取批阅概览: 教师ID={}, 筛选状态={}, 分页: {}", user.getId(), gradingStatus, pageable);

            // 调用 Service 获取列表
            Page<ExamDTO> exams = examService.getGradingOverviewExams(user.getId(), gradingStatus, pageable);

            return ResponseEntity.ok(ResponseUtil.success(exams));
        } catch (Exception e) {
            logger.error("获取批阅概览列表失败", e);
            return ResponseEntity.internalServerError().body(ResponseUtil.error("获取列表失败: " + e.getMessage()));
        }
    }
} 