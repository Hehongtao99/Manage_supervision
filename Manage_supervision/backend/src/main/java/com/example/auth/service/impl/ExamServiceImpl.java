package com.example.auth.service.impl;

import com.example.auth.dto.ExamDTO;
import com.example.auth.dto.ExamQuestionDTO;
import com.example.auth.dto.ExamStudentDTO;
import com.example.auth.dto.UserSimpleDTO;
import com.example.auth.dto.ExamPendingGradingDTO;
import com.example.auth.dto.StudentScoreDTO;
import com.example.auth.dto.NotificationRequest;
import com.example.auth.entity.Exam;
import com.example.auth.entity.ExamAnswer;
import com.example.auth.entity.ExamQuestion;
import com.example.auth.entity.ExamStudent;
import com.example.auth.entity.User;
import com.example.auth.entity.Notification;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.exception.UnauthorizedException;
import com.example.auth.repository.ExamAnswerRepository;
import com.example.auth.repository.ExamQuestionRepository;
import com.example.auth.repository.ExamRepository;
import com.example.auth.repository.ExamStudentRepository;
import com.example.auth.repository.QuestionRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ExamService;
import com.example.auth.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 考试服务实现类
 */
@Service
public class ExamServiceImpl implements ExamService {
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    @Autowired
    private ExamStudentRepository examStudentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExamAnswerRepository examAnswerRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    @Transactional
    public ExamDTO createExam(ExamDTO examDTO, Long creatorId) {
        Exam exam = new Exam();
        BeanUtils.copyProperties(examDTO, exam, "id", "createTime", "updateTime");
        
        // 设置创建者ID
        exam.setCreatorId(creatorId);
        
        // 设置状态为草稿
        if (exam.getStatus() == null) {
            exam.setStatus("DRAFT");
        }
        
        // 保存考试
        exam = examRepository.save(exam);
        ExamDTO savedExamDTO = convertToDTO(exam);
        
        // 处理考试题目
        if (examDTO.getExamQuestions() != null && !examDTO.getExamQuestions().isEmpty()) {
            List<ExamQuestionDTO> questionsDTO = addExamQuestionsInternal(exam.getId(), examDTO.getExamQuestions());
            savedExamDTO.setExamQuestions(questionsDTO);
        }
        
        // 处理考试学生
        if (examDTO.getExamStudents() != null && !examDTO.getExamStudents().isEmpty()) {
            List<ExamStudentDTO> studentsDTO = addExamStudentsInternal(exam.getId(), examDTO.getExamStudents());
            savedExamDTO.setExamStudents(studentsDTO);
        }
        
        return savedExamDTO;
    }
    
    @Override
    @Transactional
    public ExamDTO updateExam(Long examId, ExamDTO examDTO, Long creatorId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 检查是否是创建者
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("只有创建者才能修改考试");
        }
        
        // 不允许修改已发布的考试
        if ("PUBLISHED".equals(exam.getStatus()) || "ONGOING".equals(exam.getStatus())) {
            throw new IllegalStateException("已发布或进行中的考试不能修改");
        }
        
        // 更新基本信息
        exam.setTitle(examDTO.getTitle());
        exam.setDescription(examDTO.getDescription());
        exam.setStartTime(examDTO.getStartTime());
        exam.setEndTime(examDTO.getEndTime());
        exam.setDuration(examDTO.getDuration());
        exam.setPassingScore(examDTO.getPassingScore());
        exam.setTotalScore(examDTO.getTotalScore());
        
        // 保存考试
        exam = examRepository.save(exam);
        ExamDTO updatedExamDTO = convertToDTO(exam);
        
        // 处理考试题目（先删除原有题目，再添加新题目）
        if (examDTO.getExamQuestions() != null) {
            examQuestionRepository.deleteByExamId(examId);
            List<ExamQuestionDTO> questionsDTO = addExamQuestionsInternal(exam.getId(), examDTO.getExamQuestions());
            updatedExamDTO.setExamQuestions(questionsDTO);
        }
        
        // 处理考试学生（先删除原有学生，再添加新学生）
        if (examDTO.getExamStudents() != null) {
            examStudentRepository.deleteByExamId(examId);
            List<ExamStudentDTO> studentsDTO = addExamStudentsInternal(exam.getId(), examDTO.getExamStudents());
            updatedExamDTO.setExamStudents(studentsDTO);
        }
        
        return updatedExamDTO;
    }
    
    @Override
    @Transactional
    public void deleteExam(Long examId, Long creatorId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 检查是否是创建者
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("只有创建者才能删除考试");
        }
        
        // 不允许删除进行中的考试
        if ("ONGOING".equals(exam.getStatus())) {
            throw new IllegalStateException("进行中的考试不能删除");
        }
        
        // 删除考试相关的题目和学生
        examQuestionRepository.deleteByExamId(examId);
        examStudentRepository.deleteByExamId(examId);
        
        // 删除考试
        examRepository.deleteById(examId);
    }
    
    @Override
    public ExamDTO getExamById(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        ExamDTO examDTO = convertToDTO(exam);
        
        // 获取考试题目
        List<ExamQuestionDTO> questions = getExamQuestions(examId);
        examDTO.setExamQuestions(questions);
        
        // 获取考试学生
        List<ExamStudentDTO> students = getExamStudents(examId);
        examDTO.setExamStudents(students);
        
        // 获取更新后的统计信息
        Long totalStudents = examStudentRepository.countByExamId(examId);
        Long submittedCount = examStudentRepository.countSubmittedByExamId(exam.getId()); // 使用新的方法
        Long pendingPublishCount = examStudentRepository.countPendingPublishByExamId(exam.getId()); // 使用新的方法
        Long publishedCount = examStudentRepository.countPublishedByExamId(exam.getId()); // 使用新的方法
        
        examDTO.setTotalStudents(totalStudents);
        examDTO.setSubmittedCount(submittedCount);
        examDTO.setPendingPublishCount(pendingPublishCount);
        examDTO.setPublishedCount(publishedCount);
        
        // 计算并设置显示状态 (如果 getExamById 也需要这个状态)
        examDTO.setDisplayStatus(calculateDisplayStatus(exam, totalStudents, submittedCount, pendingPublishCount, publishedCount));
        
        return examDTO;
    }
    
    @Override
    public List<ExamDTO> getExamsByCreatorId(Long creatorId) {
        List<Exam> exams = examRepository.findByCreatorId(creatorId);
        return exams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<ExamDTO> getExamsByCreatorId(Long creatorId, Pageable pageable) {
        Page<Exam> examPage = examRepository.findByCreatorId(creatorId, pageable);
        
        List<ExamDTO> examDTOs = examPage.getContent().stream()
                .map(exam -> {
                    ExamDTO dto = convertToDTO(exam);
                    
                    // 获取更新后的统计信息
                    Long totalStudents = examStudentRepository.countByExamId(exam.getId());
                    Long submittedCount = examStudentRepository.countSubmittedByExamId(exam.getId()); // SUBMITTED
                    Long pendingPublishCount = examStudentRepository.countPendingPublishByExamId(exam.getId()); // PENDING_PUBLISH
                    Long publishedCount = examStudentRepository.countPublishedByExamId(exam.getId()); // PUBLISHED
                    
                    dto.setTotalStudents(totalStudents);
                    dto.setSubmittedCount(submittedCount);
                    dto.setPendingPublishCount(pendingPublishCount);
                    dto.setPublishedCount(publishedCount);

                    // 重新计算显示状态
                    dto.setDisplayStatus(calculateDisplayStatus(exam, totalStudents, submittedCount, pendingPublishCount, publishedCount));
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(examDTOs, pageable, examPage.getTotalElements());
    }
    
    @Override
    public Page<ExamDTO> getExamsByCreatorIdAndStatus(Long creatorId, String status, Pageable pageable) {
        Page<Exam> examPage = examRepository.findByCreatorIdAndStatus(creatorId, status, pageable);
        
        List<ExamDTO> examDTOs = examPage.getContent().stream()
                .map(exam -> {
                    ExamDTO dto = convertToDTO(exam);
                    
                     // 获取更新后的统计信息
                    Long totalStudents = examStudentRepository.countByExamId(exam.getId());
                    Long submittedCount = examStudentRepository.countSubmittedByExamId(exam.getId()); // SUBMITTED
                    Long pendingPublishCount = examStudentRepository.countPendingPublishByExamId(exam.getId()); // PENDING_PUBLISH
                    Long publishedCount = examStudentRepository.countPublishedByExamId(exam.getId()); // PUBLISHED
                    
                    dto.setTotalStudents(totalStudents);
                    dto.setSubmittedCount(submittedCount);
                    dto.setPendingPublishCount(pendingPublishCount);
                    dto.setPublishedCount(publishedCount);
                    
                    // 重新计算显示状态
                     dto.setDisplayStatus(calculateDisplayStatus(exam, totalStudents, submittedCount, pendingPublishCount, publishedCount));
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(examDTOs, pageable, examPage.getTotalElements());
    }
    
    @Override
    public Page<ExamDTO> searchExams(String keyword, Long creatorId, Pageable pageable) {
        Page<Exam> examPage = examRepository.searchByTitleAndCreatorId(keyword, creatorId, pageable);
        
        List<ExamDTO> examDTOs = examPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(examDTOs, pageable, examPage.getTotalElements());
    }
    
    @Override
    @Transactional
    public ExamDTO publishExam(Long examId, Long creatorId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 检查是否是创建者
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("只有创建者才能发布考试");
        }
        
        // 检查考试状态
        if (!"DRAFT".equals(exam.getStatus())) {
            throw new IllegalStateException("只有草稿状态的考试才能发布");
        }
        
        // 检查考试是否有题目
        Long questionCount = examQuestionRepository.countByExamId(examId);
        if (questionCount == 0) {
            throw new IllegalStateException("考试至少需要一道题目");
        }
        
        // 检查考试是否有学生
        List<ExamStudent> assignedStudents = examStudentRepository.findByExamId(examId);
        if (assignedStudents.isEmpty()) {
            throw new IllegalStateException("考试需要至少一名学生参加");
        }
        
        // 更新考试状态为已发布
        exam.setStatus("PUBLISHED");
        
        // 保存考试
        Exam savedExam = examRepository.save(exam);
        
        // --- 发送考试发布通知 --- 
        try {
            User teacher = userRepository.findById(creatorId).orElse(null);
            if (teacher != null) {
                String teacherName = teacher.getRealName();
                if (teacherName == null || teacherName.trim().isEmpty()) {
                    teacherName = teacher.getUsername();
                }
                if (teacherName == null || teacherName.trim().isEmpty()) {
                    teacherName = "教师"; // Default if both are empty
                }
                
                String notificationTitle = "新考试发布";
                String notificationContent = String.format("教师 %s 发布了新的考试: %s", 
                        teacherName, savedExam.getTitle());
                
                List<Long> recipientIds = assignedStudents.stream()
                        .map(ExamStudent::getStudentId)
                        .collect(Collectors.toList());
                
                if (!recipientIds.isEmpty()) {
                    NotificationRequest notificationRequest = new NotificationRequest();
                    notificationRequest.setTitle(notificationTitle);
                    notificationRequest.setContent(notificationContent);
                    notificationRequest.setRecipientIds(recipientIds);
                    
                    // 调用通知服务创建并发送通知
                    notificationService.createNotification(creatorId, notificationRequest);
                    System.out.println("已为考试 " + savedExam.getId() + " 发送发布通知给 " + recipientIds.size() + " 名学生");
                }
            } else {
                System.err.println("发送考试发布通知失败：找不到教师 ID " + creatorId);
            }
        } catch (Exception e) {
            // 记录通知发送失败的日志，但不影响考试发布的流程
            System.err.println("发送考试发布通知时发生异常: " + e.getMessage());
            e.printStackTrace(); // Consider using logger
        }
        // --- 通知发送结束 ---
        
        return convertToDTO(savedExam);
    }
    
    @Override
    public List<ExamQuestionDTO> getExamQuestions(Long examId) {
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByDisplayOrderAsc(examId);
        return questions.stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExamStudentDTO> getExamStudents(Long examId) {
        List<ExamStudent> students = examStudentRepository.findByExamId(examId);
        return students.stream()
                .map(this::convertToStudentDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void addExamQuestions(Long examId, List<ExamQuestionDTO> questions, Long creatorId) {
        // 验证考试是否存在且是否是创建者
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("只有创建者才能添加考试题目");
        }
        
        addExamQuestionsInternal(examId, questions);
    }
    
    private List<ExamQuestionDTO> addExamQuestionsInternal(Long examId, List<ExamQuestionDTO> questions) {
        List<ExamQuestion> examQuestions = new ArrayList<>();
        List<Long> questionIds = questions.stream().map(ExamQuestionDTO::getQuestionId).filter(id -> id != null).collect(Collectors.toList());

        // 优化：一次性查询所有需要的 Question 实体
        Map<Long, com.example.auth.entity.Question> originalQuestionsMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            List<com.example.auth.entity.Question> originalQuestions = questionRepository.findAllById(questionIds);
            originalQuestionsMap = originalQuestions.stream()
                    .collect(Collectors.toMap(com.example.auth.entity.Question::getId, q -> q));
        }
        
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestionDTO questionDTO = questions.get(i);
            Long originalQuestionId = questionDTO.getQuestionId();
            
            // 从预查询的 Map 中获取原始 Question 实体
            com.example.auth.entity.Question originalQuestion = originalQuestionsMap.get(originalQuestionId);
            
            if (originalQuestion == null) {
                System.err.println("警告: 在添加考试题目时未找到 ID 为 " + originalQuestionId + " 的原始题目，跳过此题。");
                continue; // 跳过找不到原始题目的情况
            }
            
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(examId);
            examQuestion.setQuestionId(originalQuestionId);
            
            // 从原始 Question 实体获取信息
            examQuestion.setQuestionTitle(originalQuestion.getTitle()); 
            examQuestion.setQuestionType(originalQuestion.getType());
            examQuestion.setContent(originalQuestion.getOptions());
            // 恢复之前的逻辑：如果 DTO 没有分数，使用默认值 5.0
            examQuestion.setQuestionScore(questionDTO.getQuestionScore() != null ? questionDTO.getQuestionScore() : new BigDecimal("5.0"));
            examQuestion.setDisplayOrder(i);
            
            examQuestions.add(examQuestion);
        }
        
        // 批量保存考试题目
        if (!examQuestions.isEmpty()) {
            List<ExamQuestion> savedQuestions = examQuestionRepository.saveAll(examQuestions);
            
            // 转换为DTO返回
            return savedQuestions.stream()
                    .map(this::convertToQuestionDTO)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>(); // 如果没有有效题目被添加，返回空列表
        }
    }
    
    @Override
    @Transactional
    public void addExamStudents(Long examId, List<ExamStudentDTO> students, Long creatorId) {
        // 验证考试是否存在且是否是创建者
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("只有创建者才能添加考试学生");
        }
        
        addExamStudentsInternal(examId, students);
    }
    
    private List<ExamStudentDTO> addExamStudentsInternal(Long examId, List<ExamStudentDTO> students) {
        List<ExamStudent> examStudents = new ArrayList<>();
        
        for (ExamStudentDTO studentDTO : students) {
            ExamStudent examStudent = new ExamStudent();
            
            examStudent.setExamId(examId);
            examStudent.setStudentId(studentDTO.getStudentId());
            examStudent.setStatus("NOT_STARTED");
            
            examStudents.add(examStudent);
        }
        
        // 批量保存考试学生
        List<ExamStudent> savedStudents = examStudentRepository.saveAll(examStudents);
        
        // 转换为DTO返回
        return savedStudents.stream()
                .map(this::convertToStudentDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void removeExamQuestion(Long examId, Long questionId, Long creatorId) {
        // 验证考试是否存在且是否是创建者
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("只有创建者才能删除考试题目");
        }
        
        // 查找考试题目
        ExamQuestion examQuestion = examQuestionRepository.findByExamIdAndQuestionId(examId, questionId);
        if (examQuestion == null) {
            throw new ResourceNotFoundException("考试题目不存在");
        }
        
        // 删除考试题目
        examQuestionRepository.delete(examQuestion);
    }
    
    @Override
    @Transactional
    public void removeExamStudent(Long examId, Long studentId, Long creatorId) {
        // 检查创建者权限
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));

        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("只有创建者才能删除考试学生");
        }

        // 查找考试学生
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("考试学生不存在"));

        // 删除考试学生
        examStudentRepository.delete(examStudent);
    }
    
    /**
     * 将考试实体转换为DTO
     */
    private ExamDTO convertToDTO(Exam exam) {
        ExamDTO examDTO = new ExamDTO();
        BeanUtils.copyProperties(exam, examDTO);
        
        // 设置创建者名称
        User creator = userRepository.findById(exam.getCreatorId()).orElse(null);
        if (creator != null) {
            examDTO.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
        }
        
        return examDTO;
    }
    
    /**
     * 将考试题目实体转换为DTO
     */
    private ExamQuestionDTO convertToQuestionDTO(ExamQuestion examQuestion) {
        ExamQuestionDTO dto = new ExamQuestionDTO();
        dto.setId(examQuestion.getId());
        dto.setExamId(examQuestion.getExamId());
        dto.setQuestionId(examQuestion.getQuestionId());
        dto.setQuestionTitle(examQuestion.getQuestionTitle());
        dto.setQuestionType(examQuestion.getQuestionType());
        
        // 添加日志：打印从实体获取的 content 值
        String contentFromEntity = examQuestion.getContent();
        System.out.println("Converting ExamQuestion ID: " + examQuestion.getId() + ", Content from entity: " + contentFromEntity);
        dto.setContent(contentFromEntity); 
        
        dto.setQuestionScore(examQuestion.getQuestionScore());
        dto.setDisplayOrder(examQuestion.getDisplayOrder());
        if (examQuestion.getCreateTime() != null) {
            dto.setCreateTime(LocalDateTime.ofInstant(examQuestion.getCreateTime(), ZoneId.systemDefault()));
        }
        return dto;
    }
    
    /**
     * 将考试学生实体转换为DTO
     */
    private ExamStudentDTO convertToStudentDTO(ExamStudent examStudent) {
        ExamStudentDTO dto = new ExamStudentDTO();
        BeanUtils.copyProperties(examStudent, dto);
        
        // 获取学生信息
        User student = userRepository.findById(examStudent.getStudentId()).orElse(null);
        if (student != null) {
            dto.setStudentName(student.getRealName() != null ? student.getRealName() : student.getUsername());
            dto.setStudentUsername(student.getUsername());
            dto.setStudentUserNumber(student.getUserNumber());
        }
        
        // 计算是否通过考试
        if (examStudent.getScore() != null && examStudent.getExam() != null) {
            BigDecimal passingScore = examStudent.getExam().getPassingScore();
            dto.setIsPassed(examStudent.getScore().compareTo(passingScore) >= 0);
        } else {
            dto.setIsPassed(false);
        }
        
        return dto;
    }
    
    @Override
    public Page<ExamDTO> getExamsByStudentId(Long studentId, String status, Pageable pageable) {
        Page<ExamStudent> examStudentPage;
        
        // 增加日志输出
        System.out.println("获取学生考试列表 - 学生ID: " + studentId + ", 状态: " + status);
        
        try {
            if (status != null && !status.isEmpty()) {
                // 根据状态筛选
                examStudentPage = examStudentRepository.findByStudentIdAndStatus(studentId, status, pageable);
            } else {
                // 获取全部
                examStudentPage = examStudentRepository.findByStudentId(studentId, pageable);
            }
            
            System.out.println("查询到学生考试数量: " + examStudentPage.getTotalElements());
            
            // 检查examStudentPage内容是否为空
            if (examStudentPage.getContent().isEmpty()) {
                System.out.println("学生考试关联记录为空");
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }
            
            List<Long> examIds = examStudentPage.getContent().stream()
                    .map(ExamStudent::getExamId)
                    .collect(Collectors.toList());
            
            if (examIds.isEmpty()) {
                System.out.println("没有找到考试ID列表");
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }
            
            List<Exam> exams = examRepository.findByIdIn(examIds);
            System.out.println("根据ID列表查询到考试数量: " + exams.size());
            
            if (exams.isEmpty()) {
                System.out.println("根据ID列表未找到任何考试");
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }
            
            Map<Long, Exam> examMap = exams.stream()
                    .collect(Collectors.toMap(Exam::getId, exam -> exam));
            
            Map<Long, ExamStudent> examStudentMap = examStudentPage.getContent().stream()
                    .collect(Collectors.toMap(ExamStudent::getExamId, student -> student, (s1, s2) -> s1));
            
            List<ExamDTO> examDTOs = examIds.stream()
                    .filter(examMap::containsKey)
                    .map(examId -> {
                        ExamDTO dto = convertToDTO(examMap.get(examId));
                        ExamStudent student = examStudentMap.get(examId);
                        if (student != null) {
                            dto.setStudentStatus(student.getStatus());
                            dto.setStudentScore(student.getScore());
                            
                            if (student.getStartTime() != null) {
                                dto.setStudentStartTime(student.getStartTime());
                            }
                            
                            if (student.getSubmitTime() != null) {
                                dto.setStudentSubmitTime(student.getSubmitTime());
                            }
                        }
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            System.out.println("最终生成的考试DTO数量: " + examDTOs.size());
            
            return new PageImpl<>(examDTOs, pageable, examStudentPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("获取学生考试列表出错: " + e.getMessage());
            e.printStackTrace();
            // 发生异常时返回空列表而不是抛出异常
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
    
    @Override
    public boolean isStudentAssignedToExam(Long examId, Long studentId) {
        return examStudentRepository.existsByExamIdAndStudentId(examId, studentId);
    }
    
    @Override
    public Long countExamsByStudentId(Long studentId) {
        return examStudentRepository.countByStudentId(studentId);
    }
    
    @Override
    public ExamStudentDTO getExamStudentStatus(Long examId, Long studentId) {
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生未分配此考试"));
        
        return convertToStudentDTO(examStudent);
    }
    
    @Override
    @Transactional
    public ExamStudentDTO startExam(Long examId, Long studentId) {
        // 获取考试信息
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 检查考试状态
        if (!"PUBLISHED".equals(exam.getStatus()) && !"ONGOING".equals(exam.getStatus())) {
            throw new IllegalStateException("考试未发布或已结束");
        }
        
        // 获取学生的考试状态
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生未分配此考试"));
        
        // 检查是否已经开始或完成
        if ("SUBMITTED".equals(examStudent.getStatus()) || "GRADED".equals(examStudent.getStatus())) {
            throw new IllegalStateException("考试已提交或已评分");
        }
        
        // 更新考试状态为进行中
        examStudent.setStatus("IN_PROGRESS");
        
        // 如果是第一次开始，记录开始时间
        if (examStudent.getStartTime() == null) {
            examStudent.setStartTime(Instant.now());
        }
        
        // 保存状态
        examStudent = examStudentRepository.save(examStudent);
        
        // 更新考试状态为进行中（如果当前是已发布状态）
        if ("PUBLISHED".equals(exam.getStatus())) {
            exam.setStatus("ONGOING");
            examRepository.save(exam);
        }
        
        return convertToStudentDTO(examStudent);
    }
    
    @Override
    public List<ExamQuestionDTO> getExamQuestionsForStudent(Long examId) {
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByDisplayOrderAsc(examId);
        
        // 转换为DTO，不包含答案
        return questions.stream()
                .map(q -> {
                    ExamQuestionDTO dto = convertToQuestionDTO(q);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ExamStudentDTO submitExam(Long examId, Long studentId, Map<String, String> answers) {
        System.out.println("Submit Exam - ExamID: " + examId + ", StudentID: " + studentId);
        System.out.println("Received answers map size: " + (answers != null ? answers.size() : "null"));
        
        // 获取考试信息
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在, ID: " + examId));
        System.out.println("Found exam: " + exam.getTitle());
        
        // 获取学生的考试状态
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生未分配此考试, ExamID: " + examId + ", StudentID: " + studentId));
        System.out.println("Found exam student record, Status: " + examStudent.getStatus());
        
        // 检查是否已经提交或评分
        if ("SUBMITTED".equals(examStudent.getStatus()) || "GRADED".equals(examStudent.getStatus())) {
            System.err.println("Attempt to submit already submitted/graded exam");
            throw new IllegalStateException("考试已提交或已评分");
        }
        
        // 获取考试题目
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByDisplayOrderAsc(examId);
        if (questions.isEmpty()) {
            System.err.println("No questions found for exam ID: " + examId);
            // 即使没有题目，也应该允许提交（可能得0分），但标记为已提交
        }
        Map<Long, ExamQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));
        System.out.println("Found " + questions.size() + " questions for the exam.");
                
        BigDecimal totalScore = BigDecimal.ZERO;
        
        // 删除之前的答案（如果有）
        try {
            examAnswerRepository.deleteByExamIdAndStudentId(examId, studentId);
            System.out.println("Deleted previous answers for student.");
        } catch (Exception e) {
            System.err.println("Error deleting previous answers: " + e.getMessage());
            // 不应该因为删除旧答案失败而阻止提交，但需要记录错误
        }
        
        List<ExamAnswer> answersToSave = new ArrayList<>();
        // 保存答案并计算分数
        if (answers != null) {
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                Long questionEntityId = null; 
                try {
                    questionEntityId = Long.parseLong(entry.getKey()); 
                } catch (NumberFormatException e) {
                    System.err.println("Invalid question ID format in answer key: " + entry.getKey());
                    continue; 
                }
                
                String answer = entry.getValue();
                System.out.println("Processing answer for ExamQuestion ID: " + questionEntityId + ", Answer: " + answer);
                
                ExamQuestion question = questionMap.get(questionEntityId);
                if (question == null) {
                    System.err.println("Warning: ExamQuestion with ID " + questionEntityId + " not found in exam's question list. Skipping answer.");
                    continue; 
                }
                
                ExamAnswer examAnswer = new ExamAnswer();
                examAnswer.setExamId(examId);
                examAnswer.setStudentId(studentId);
                examAnswer.setQuestionId(question.getQuestionId());
                examAnswer.setAnswer(answer != null ? answer : ""); 
                
                // 自动评分（选择题和判断题）
                String questionType = question.getQuestionType();
                String correctAnswer = question.getAnswer(); 
                
                 if (correctAnswer == null && ("SINGLE_CHOICE".equals(questionType) || "MULTIPLE_CHOICE".equals(questionType) || "JUDGMENT".equals(questionType))) {
                     System.err.println("Warning: Correct answer is missing for question ID: " + questionEntityId + " (ExamQuestion ID: " + question.getId() + "). Cannot auto-grade.");
                     examAnswer.setIsCorrect(null);
                     examAnswer.setScore(BigDecimal.ZERO); // 或者设为 null，取决于业务逻辑
                 } else if ("SINGLE_CHOICE".equals(questionType) || "JUDGMENT".equals(questionType)) {
                     boolean isCorrect = answer != null && answer.equals(correctAnswer);
                     examAnswer.setIsCorrect(isCorrect);
                     examAnswer.setScore(isCorrect ? question.getQuestionScore() : BigDecimal.ZERO);
                     if (isCorrect) totalScore = totalScore.add(question.getQuestionScore());
                     System.out.println("  -> Single/Judgment: Correct Answer=" + correctAnswer + ", Submitted=" + answer + ", Correct?=" + isCorrect + ", Score=" + examAnswer.getScore());
                 } else if ("MULTIPLE_CHOICE".equals(questionType)) {
                     // 多选题答案处理，假设答案是逗号分隔的字符串，如 "A,B,C"
                     String sortedStudentAnswer = (answer != null) 
                         ? Arrays.stream(answer.split(",")).map(String::trim).filter(s -> !s.isEmpty()).sorted().collect(Collectors.joining(","))
                         : "";
                     String sortedCorrectAnswer = (correctAnswer != null)
                         ? Arrays.stream(correctAnswer.split(",")).map(String::trim).filter(s -> !s.isEmpty()).sorted().collect(Collectors.joining(","))
                         : "";
                     boolean isCorrect = sortedStudentAnswer.equals(sortedCorrectAnswer);
                     examAnswer.setIsCorrect(isCorrect);
                     examAnswer.setScore(isCorrect ? question.getQuestionScore() : BigDecimal.ZERO);
                     if (isCorrect) totalScore = totalScore.add(question.getQuestionScore());
                     System.out.println("  -> Multiple Choice: Correct Answer=" + sortedCorrectAnswer + ", Submitted=" + sortedStudentAnswer + ", Correct?=" + isCorrect + ", Score=" + examAnswer.getScore());
                 } else {
                    // 主观题需要人工评分
                    examAnswer.setIsCorrect(null);
                    examAnswer.setScore(null);
                    System.out.println("  -> Essay: Requires manual grading.");
                }
                
                answersToSave.add(examAnswer);
            }
        }
        
        // 批量保存答案
        try {
            if (!answersToSave.isEmpty()) {
                examAnswerRepository.saveAll(answersToSave);
                System.out.println("Saved " + answersToSave.size() + " answers.");
            }
        } catch (Exception e) {
            System.err.println("Error saving answers: " + e.getMessage());
            // 根据业务决定是否抛出异常或仅记录
            throw new RuntimeException("保存答案时出错", e);
        }
        
        // 更新考试状态为已提交
        examStudent.setStatus("SUBMITTED");
        examStudent.setSubmitTime(Instant.now());
        System.out.println("Student status updated to SUBMITTED.");
        
        // 如果全是客观题，自动设置分数；否则分数为null，等待人工评分
        boolean hasSubjectiveQuestions = questions.stream()
                .anyMatch(q -> "ESSAY".equals(q.getQuestionType()));
        System.out.println("Has subjective questions: " + hasSubjectiveQuestions);
                
        if (!hasSubjectiveQuestions) {
            examStudent.setScore(totalScore);
            // 如果全是客观题，状态变为待发布，等待教师确认发布
            examStudent.setStatus("PENDING_PUBLISH"); 
            System.out.println("All objective questions. Final score: " + totalScore + ". Status set to PENDING_PUBLISH.");
        } else {
             examStudent.setScore(null); // 包含主观题，分数待定
             // 状态保持为 SUBMITTED，等待教师批阅
             System.out.println("Contains subjective questions. Score set to null. Status remains SUBMITTED.");
        }
        
        // 保存状态
        try {
            examStudent = examStudentRepository.save(examStudent);
            System.out.println("ExamStudent status saved.");
        } catch (Exception e) {
            System.err.println("Error saving ExamStudent status: " + e.getMessage());
            throw new RuntimeException("更新学生考试状态时出错", e);
        }
                
        return convertToStudentDTO(examStudent);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentExamAnswers(Long examId, Long studentId, Long creatorId) {
        // 检查考试存在性和权限
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("无权访问此考试");
        }
        
        // 检查学生是否分配到此考试
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生未分配此考试"));
        
        // 检查状态是否为已提交
        if (!"SUBMITTED".equals(examStudent.getStatus()) && !"GRADED".equals(examStudent.getStatus())) {
            throw new IllegalStateException("考试尚未提交，无法查看答案");
        }
        
        // 获取考试题目
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByDisplayOrderAsc(examId);
        
        // 获取学生答案
        List<ExamAnswer> answers = examAnswerRepository.findByExamIdAndStudentId(examId, studentId);
        
        // 将答案按题目ID分组
        Map<Long, ExamAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a));
        
        // 构建题目和答案的列表
        List<Map<String, Object>> questionAnswers = new ArrayList<>();
        
        for (ExamQuestion question : questions) {
            Map<String, Object> qaItem = new HashMap<>();
            qaItem.put("question", convertToQuestionDTO(question));
            
            ExamAnswer answer = answerMap.get(question.getQuestionId());
            if (answer != null) {
                Map<String, Object> answerData = new HashMap<>();
                answerData.put("id", answer.getId());
                answerData.put("answer", answer.getAnswer());
                answerData.put("isCorrect", answer.getIsCorrect());
                answerData.put("score", answer.getScore());
                qaItem.put("answer", answerData);
            } else {
                qaItem.put("answer", null);
            }
            
            questionAnswers.add(qaItem);
        }
        
        // 获取学生信息
        User studentEntity = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));
        
        // 创建 UserSimpleDTO
        UserSimpleDTO studentDTO = new UserSimpleDTO(
            studentEntity.getId(),
            studentEntity.getUsername(),
            studentEntity.getRealName(),
            studentEntity.getNickname(),
            studentEntity.getUserNumber()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("exam", convertToDTO(exam));
        result.put("student", studentDTO); // 使用 DTO 替换实体
        result.put("examStudent", convertToStudentDTO(examStudent));
        result.put("questionAnswers", questionAnswers);
        
        return result;
    }
    
    @Override
    @Transactional
    public ExamStudentDTO gradeStudentExam(Long examId, Long studentId, Map<String, Object> gradeData, Long creatorId) {
        // 检查考试存在性和权限
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("无权批阅此考试");
        }
        
        // 检查学生是否分配到此考试
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生未分配此考试"));
        
        // 检查状态是否为已提交
        if (!"SUBMITTED".equals(examStudent.getStatus()) && !"GRADED".equals(examStudent.getStatus()) 
            && !"PENDING_PUBLISH".equals(examStudent.getStatus()) && !"PUBLISHED".equals(examStudent.getStatus())) {
            throw new IllegalStateException("考试尚未提交，无法批阅");
        }
        
        // 获取答案评分数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answersGrade = (List<Map<String, Object>>) gradeData.get("answers");
        
        if (answersGrade == null || answersGrade.isEmpty()) {
            throw new IllegalArgumentException("未提供批阅数据");
        }
        
        // 处理每个答案的评分
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (Map<String, Object> answerGrade : answersGrade) {
            Long answerId = Long.valueOf(answerGrade.get("id").toString());
            BigDecimal score = new BigDecimal(answerGrade.get("score").toString());
            Boolean isCorrect = (Boolean) answerGrade.get("isCorrect");
            
            // 更新答案
            ExamAnswer answer = examAnswerRepository.findById(answerId)
                    .orElseThrow(() -> new ResourceNotFoundException("答案不存在"));
            
            // 确认答案属于正确的考试和学生
            if (!answer.getExamId().equals(examId) || !answer.getStudentId().equals(studentId)) {
                throw new IllegalArgumentException("答案ID与考试或学生不匹配");
            }
            
            answer.setScore(score);
            answer.setIsCorrect(isCorrect);
            examAnswerRepository.save(answer);
            
            totalScore = totalScore.add(score);
        }
        
        // 更新学生考试状态为待发布，并设置总分
        examStudent.setStatus("PENDING_PUBLISH");
        examStudent.setScore(totalScore);
        examStudent = examStudentRepository.save(examStudent);
        
        return convertToStudentDTO(examStudent);
    }

    @Override
    public List<ExamPendingGradingDTO> getExamsWithPendingGrading(Long creatorId) {
        // 1. 查找该教师创建的所有考试
        List<Exam> exams = examRepository.findByCreatorId(creatorId);
        if (exams.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> examIds = exams.stream().map(Exam::getId).collect(Collectors.toList());

        // 2. 查找这些考试中所有状态为 'SUBMITTED' 的学生记录
        List<ExamStudent> submittedStudents = examStudentRepository.findByExamIdInAndStatus(examIds, "SUBMITTED");

        // 3. 按 examId 统计待批阅学生数量
        Map<Long, Long> pendingCounts = submittedStudents.stream()
                .collect(Collectors.groupingBy(ExamStudent::getExamId, Collectors.counting()));

        // 4. 构建 DTO 列表
        return exams.stream()
                .filter(exam -> pendingCounts.containsKey(exam.getId()) && pendingCounts.get(exam.getId()) > 0)
                .map(exam -> new ExamPendingGradingDTO(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getStartTime(),
                        exam.getEndTime(),
                        pendingCounts.getOrDefault(exam.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Page<StudentScoreDTO> getExamScores(Long examId, Long creatorId, Pageable pageable) {
        // 1. 检查考试存在性和权限
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("无权访问此考试的成绩");
        }

        // 2. 查询状态为 'GRADED' 的学生记录（带分页和排序）
        // 注意: Pageable 对象已经包含了排序信息，Spring Data JPA 会自动应用
        Page<ExamStudent> gradedStudentsPage = examStudentRepository.findByExamIdAndStatus(examId, "GRADED", pageable);

        // 3. 获取学生 ID 列表
        List<Long> studentIds = gradedStudentsPage.getContent().stream()
                                    .map(ExamStudent::getStudentId)
                                    .collect(Collectors.toList());

        // 4. 批量查询学生信息
        Map<Long, User> studentMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<User> students = userRepository.findAllById(studentIds);
            studentMap = students.stream().collect(Collectors.toMap(User::getId, user -> user));
        }
        final Map<Long, User> finalStudentMap = studentMap; // 需要 final 或 effectively final

        // 5. 转换为 StudentScoreDTO
        List<StudentScoreDTO> scoreDTOs = gradedStudentsPage.getContent().stream()
            .map(es -> {
                User student = finalStudentMap.get(es.getStudentId());
                String studentName = (student != null) ? (student.getRealName() != null ? student.getRealName() : student.getUsername()) : "未知学生";
                String studentUserNumber = (student != null) ? student.getUserNumber() : "N/A";
                return new StudentScoreDTO(
                    es.getStudentId(),
                    studentName,
                    studentUserNumber,
                    es.getScore() // 分数直接从 ExamStudent 获取
                );
            })
            .collect(Collectors.toList());

        // 6. 返回分页结果
        return new PageImpl<>(scoreDTOs, pageable, gradedStudentsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentExamResultDetails(Long examId, Long studentId) {
        // 1. 检查考试是否存在
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));

        // 2. 检查学生是否分配到此考试
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("您未参加此考试"));

        // 3. 检查状态是否为已发布（学生只能查看已发布的成绩）
        if (!"PUBLISHED".equals(examStudent.getStatus())) {
            throw new IllegalStateException("成绩尚未发布，无法查看结果");
        }

        // 4. 获取考试题目 (此处需要包含正确答案以供对比)
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByDisplayOrderAsc(examId);

        // 5. 获取学生答案
        List<ExamAnswer> answers = examAnswerRepository.findByExamIdAndStudentId(examId, studentId);
        Map<Long, ExamAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a)); // 使用原始 Question ID

        // 6. 构建题目和答案的列表
        List<Map<String, Object>> questionAnswers = new ArrayList<>();
        for (ExamQuestion question : questions) {
            Map<String, Object> qaItem = new HashMap<>();
             // 注意：这里需要转换，确保 QuestionDTO 包含正确答案字段
            ExamQuestionDTO questionDTO = convertToQuestionDTOWithAnswer(question); 
            qaItem.put("question", questionDTO);

            // 使用 ExamQuestion 的原始 questionId 从 answerMap 获取答案
            ExamAnswer answer = answerMap.get(question.getQuestionId()); 
            if (answer != null) {
                Map<String, Object> answerData = new HashMap<>();
                answerData.put("id", answer.getId());
                answerData.put("answer", answer.getAnswer());
                answerData.put("isCorrect", answer.getIsCorrect());
                answerData.put("score", answer.getScore());
                qaItem.put("answer", answerData);
            } else {
                qaItem.put("answer", null); // 学生未作答此题
            }
            questionAnswers.add(qaItem);
        }

        // 7. 获取学生信息 (使用 DTO 避免懒加载问题)
        User studentEntity = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生信息不存在"));
        UserSimpleDTO studentDTO = new UserSimpleDTO(
            studentEntity.getId(),
            studentEntity.getUsername(),
            studentEntity.getRealName(),
            studentEntity.getNickname(),
            studentEntity.getUserNumber()
        );
        
        // 8. 组装结果返回
        Map<String, Object> result = new HashMap<>();
        result.put("exam", convertToDTO(exam)); // Exam 基本信息 DTO
        result.put("student", studentDTO); // 学生基本信息 DTO
        result.put("examStudent", convertToStudentDTO(examStudent)); // 学生考试状态 DTO
        result.put("questionAnswers", questionAnswers); // 包含题目和答案的列表

        return result;
    }
    
    // 辅助方法：转换 ExamQuestion 为 DTO，包含答案
    private ExamQuestionDTO convertToQuestionDTOWithAnswer(ExamQuestion examQuestion) {
        ExamQuestionDTO dto = convertToQuestionDTO(examQuestion); // 复用现有转换方法
        // 确保从原始 Question 实体获取正确答案
        com.example.auth.entity.Question originalQuestion = questionRepository.findById(examQuestion.getQuestionId()).orElse(null);
        if(originalQuestion != null) {
             dto.setAnswer(originalQuestion.getAnswer()); 
        }
        return dto;
    }

    @Override
    public Page<ExamDTO> getGradingOverviewExams(Long creatorId, String gradingStatus, Pageable pageable) {
        // 1. 获取教师创建的所有考试（不分页，后续手动处理）
        List<Exam> exams = examRepository.findByCreatorId(creatorId);
        if (exams.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 准备 DTO 列表并计算统计数据
        List<ExamDTO> examDTOs = new ArrayList<>();
        for (Exam exam : exams) {
            ExamDTO dto = convertToDTO(exam);
            // 获取更新后的统计信息
            Long totalStudents = examStudentRepository.countByExamId(exam.getId());
            Long submittedCount = examStudentRepository.countSubmittedByExamId(exam.getId());
            Long pendingPublishCount = examStudentRepository.countPendingPublishByExamId(exam.getId());
            Long publishedCount = examStudentRepository.countPublishedByExamId(exam.getId());

            dto.setTotalStudents(totalStudents);
            dto.setSubmittedCount(submittedCount);
            dto.setPendingPublishCount(pendingPublishCount);
            dto.setPublishedCount(publishedCount);
            examDTOs.add(dto);
        }

        // 3. 根据 gradingStatus 筛选
        List<ExamDTO> filteredExams;
        if ("pending".equalsIgnoreCase(gradingStatus)) {
            filteredExams = examDTOs.stream()
                    .filter(dto -> dto.getSubmittedCount() != null && dto.getSubmittedCount() > 0)
                    .collect(Collectors.toList());
        } else if ("graded".equalsIgnoreCase(gradingStatus)) {
            filteredExams = examDTOs.stream()
                    .filter(dto -> dto.getPendingPublishCount() != null && dto.getPendingPublishCount() > 0)
                     .collect(Collectors.toList());
        } else if ("published".equalsIgnoreCase(gradingStatus)) {
            filteredExams = examDTOs.stream()
                    .filter(dto -> dto.getPublishedCount() != null && dto.getPublishedCount() > 0)
                    .collect(Collectors.toList());
        }
        else {
            // 不筛选或 gradingStatus 为空/null
            filteredExams = examDTOs;
        }

        // 4. 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredExams.size());
        
        List<ExamDTO> pageContent = new ArrayList<>();
        if (start <= end && start < filteredExams.size()) { // 确保 start 不大于 end 且在列表范围内
             pageContent = filteredExams.subList(start, end);
        }
       
        return new PageImpl<>(pageContent, pageable, filteredExams.size());
    }

    // 辅助方法：计算考试的显示状态 (更新版)
    private String calculateDisplayStatus(Exam exam, Long totalStudents, Long submittedCount, Long pendingPublishCount, Long publishedCount) {
        Instant now = Instant.now();
        String originalStatus = exam.getStatus();
        Instant startTime = exam.getStartTime();
        Instant endTime = exam.getEndTime();
        
        totalStudents = totalStudents == null ? 0L : totalStudents;
        submittedCount = submittedCount == null ? 0L : submittedCount;
        pendingPublishCount = pendingPublishCount == null ? 0L : pendingPublishCount;
        publishedCount = publishedCount == null ? 0L : publishedCount;
        
        // 检查是否所有人都已完成（状态为 PUBLISHED）
        boolean allCompleted = totalStudents > 0 && publishedCount >= totalStudents;
        
        if ("DRAFT".equals(originalStatus)) {
            return "草稿";
        }
        
        if (("PUBLISHED".equals(originalStatus) || "ONGOING".equals(originalStatus)) && startTime != null && now.isBefore(startTime)) {
            return "已发布"; // 还未到开始时间
        }
        
        // 检查是否所有人都已完成
        if (allCompleted) {
             return "已完成"; // 所有人都已发布成绩
        }
        
        // 检查是否已过结束时间
        if (endTime != null && now.isAfter(endTime)) {
             // 即使过了结束时间，如果还有人未完成（未发布成绩），状态也不是"已完成"
             return "已结束"; 
        }
        
        // 在考试进行时间内
        if (("PUBLISHED".equals(originalStatus) || "ONGOING".equals(originalStatus)) && 
            (startTime == null || !now.isBefore(startTime)) && 
            (endTime == null || !now.isAfter(endTime))) {
                // 在进行中，但需要细分状态
                 if (pendingPublishCount > 0 || publishedCount > 0) {
                     return "批阅中"; // 有人已提交，且有人处于待发布或已发布状态
                 } else if (submittedCount > 0) {
                     return "批阅中"; // 有人提交，但还没人被批阅
                 } else {
                     return "进行中"; // 还没人提交
                 }
        }

        // 其他情况（理论上主要是考试结束后未完成的情况）
        if (pendingPublishCount > 0 || submittedCount > 0) {
            return "待批阅/发布"; // 结束后，还有待处理的学生
        } else if (publishedCount > 0 && publishedCount < totalStudents) {
             return "已结束 (部分完成)"; // 结束后，有人完成但不是全部
        }
        
        return "已结束"; // 默认的结束状态
    }

    @Override
    @Transactional
    public List<ExamStudentDTO> publishStudentGrades(Long examId, Long studentId, Long creatorId) {
        // 检查考试存在性和权限
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        if (!exam.getCreatorId().equals(creatorId)) {
            throw new UnauthorizedException("无权发布此考试成绩");
        }
        
        List<ExamStudent> studentsToPublish;
        
        // 如果提供了学生ID，只发布该学生的成绩
        if (studentId != null) {
            ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("学生未分配此考试"));
            
            // 检查状态是否为待发布
            if (!"PENDING_PUBLISH".equals(examStudent.getStatus()) && !"GRADED".equals(examStudent.getStatus())) {
                throw new IllegalStateException("该学生成绩未完成批阅，无法发布");
            }
            
            studentsToPublish = List.of(examStudent);
        } else {
            // 否则，发布所有待发布状态的学生成绩
            studentsToPublish = examStudentRepository.findByExamIdAndStatusIn(
                    examId, Arrays.asList("PENDING_PUBLISH", "GRADED"));
            
            if (studentsToPublish.isEmpty()) {
                throw new IllegalStateException("没有待发布的学生成绩");
            }
        }
        
        // 更新状态为已发布
        List<ExamStudentDTO> publishedStudents = new ArrayList<>();
        for (ExamStudent student : studentsToPublish) {
            student.setStatus("PUBLISHED");
            ExamStudent savedStudent = examStudentRepository.save(student);
            publishedStudents.add(convertToStudentDTO(savedStudent));
            
            // 发送成绩通知给学生
            try {
                User studentUser = userRepository.findById(student.getStudentId()).orElse(null);
                if (studentUser != null) {
                    String notificationContent = String.format(
                            "您的考试 '%s' 成绩已发布，您的得分为：%s。",
                            exam.getTitle(),
                            student.getScore()
                    );
                    
                    // 创建 NotificationRequest 对象
                    NotificationRequest notificationRequest = new NotificationRequest();
                    notificationRequest.setTitle("考试成绩发布");
                    notificationRequest.setContent(notificationContent);
                    notificationRequest.setRecipientIds(Collections.singletonList(student.getStudentId()));
                    
                    // 调用通知服务
                    notificationService.createNotification(creatorId, notificationRequest);
                }
            } catch (Exception e) {
                // 记录错误但不中断流程
                System.err.println("发送成绩通知失败: " + e.getMessage());
            }
        }
        
        return publishedStudents;
    }
}