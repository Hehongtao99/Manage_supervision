package com.example.auth.service.impl;

import com.example.auth.dto.ExamDTO;
import com.example.auth.dto.ExamQuestionDTO;
import com.example.auth.dto.ExamStudentDTO;
import com.example.auth.entity.Exam;
import com.example.auth.entity.ExamAnswer;
import com.example.auth.entity.ExamQuestion;
import com.example.auth.entity.ExamStudent;
import com.example.auth.entity.User;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.exception.UnauthorizedException;
import com.example.auth.repository.ExamAnswerRepository;
import com.example.auth.repository.ExamQuestionRepository;
import com.example.auth.repository.ExamRepository;
import com.example.auth.repository.ExamStudentRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ExamService;
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
import java.util.ArrayList;
import java.util.Arrays;
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
        
        // 获取统计信息
        Long totalStudents = examStudentRepository.countByExamId(examId);
        Long submittedCount = examStudentRepository.countByExamIdAndStatusIn(examId, Arrays.asList("SUBMITTED", "GRADED"));
        Long gradedCount = examStudentRepository.countGradedByExamId(examId);
        
        examDTO.setTotalStudents(totalStudents);
        examDTO.setSubmittedCount(submittedCount);
        examDTO.setGradedCount(gradedCount);
        
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
                    
                    // 获取统计信息
                    Long totalStudents = examStudentRepository.countByExamId(exam.getId());
                    Long submittedCount = examStudentRepository.countByExamIdAndStatusIn(exam.getId(), Arrays.asList("SUBMITTED", "GRADED"));
                    Long gradedCount = examStudentRepository.countGradedByExamId(exam.getId());
                    
                    dto.setTotalStudents(totalStudents);
                    dto.setSubmittedCount(submittedCount);
                    dto.setGradedCount(gradedCount);
                    
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
                    
                    // 获取统计信息
                    Long totalStudents = examStudentRepository.countByExamId(exam.getId());
                    Long submittedCount = examStudentRepository.countByExamIdAndStatusIn(exam.getId(), Arrays.asList("SUBMITTED", "GRADED"));
                    Long gradedCount = examStudentRepository.countGradedByExamId(exam.getId());
                    
                    dto.setTotalStudents(totalStudents);
                    dto.setSubmittedCount(submittedCount);
                    dto.setGradedCount(gradedCount);
                    
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
        Long studentCount = examStudentRepository.countByExamId(examId);
        if (studentCount == 0) {
            throw new IllegalStateException("考试需要至少一名学生参加");
        }
        
        // 更新考试状态为已发布
        exam.setStatus("PUBLISHED");
        
        // 保存考试
        exam = examRepository.save(exam);
        return convertToDTO(exam);
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
        
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestionDTO questionDTO = questions.get(i);
            ExamQuestion examQuestion = new ExamQuestion();
            
            examQuestion.setExamId(examId);
            examQuestion.setQuestionId(questionDTO.getQuestionId());
            examQuestion.setQuestionTitle(questionDTO.getQuestionTitle());
            examQuestion.setQuestionType(questionDTO.getQuestionType());
            examQuestion.setQuestionScore(questionDTO.getQuestionScore() != null ? questionDTO.getQuestionScore() : new BigDecimal("5.0"));
            examQuestion.setDisplayOrder(i); // 使用列表索引作为显示顺序
            
            examQuestions.add(examQuestion);
        }
        
        // 批量保存考试题目
        List<ExamQuestion> savedQuestions = examQuestionRepository.saveAll(examQuestions);
        
        // 转换为DTO返回
        return savedQuestions.stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
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
        BeanUtils.copyProperties(examQuestion, dto);
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
        // 获取考试信息
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 获取学生的考试状态
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生未分配此考试"));
        
        // 检查是否已经提交或评分
        if ("SUBMITTED".equals(examStudent.getStatus()) || "GRADED".equals(examStudent.getStatus())) {
            throw new IllegalStateException("考试已提交或已评分");
        }
        
        // 获取考试题目
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByDisplayOrderAsc(examId);
        Map<Long, ExamQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));
        
        BigDecimal totalScore = BigDecimal.ZERO;
        
        // 删除之前的答案（如果有）
        examAnswerRepository.deleteByExamIdAndStudentId(examId, studentId);
        
        // 保存答案并计算分数
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            Long questionId = Long.parseLong(entry.getKey());
            String answer = entry.getValue();
            
            ExamQuestion question = questionMap.get(questionId);
            if (question == null) {
                continue; // 跳过无效题目
            }
            
            ExamAnswer examAnswer = new ExamAnswer();
            examAnswer.setExamId(examId);
            examAnswer.setStudentId(studentId);
            examAnswer.setQuestionId(questionId);
            examAnswer.setAnswer(answer);
            
            // 自动评分（选择题和判断题）
            if ("SINGLE_CHOICE".equals(question.getQuestionType()) || 
                "MULTIPLE_CHOICE".equals(question.getQuestionType()) || 
                "JUDGMENT".equals(question.getQuestionType())) {
                boolean isCorrect = answer.equals(question.getAnswer());
                examAnswer.setIsCorrect(isCorrect);
                
                if (isCorrect) {
                    examAnswer.setScore(question.getQuestionScore());
                    totalScore = totalScore.add(question.getQuestionScore());
                } else {
                    examAnswer.setScore(BigDecimal.ZERO);
                }
            } else {
                // 主观题需要人工评分
                examAnswer.setIsCorrect(null);
                examAnswer.setScore(null);
            }
            
            examAnswerRepository.save(examAnswer);
        }
        
        // 更新考试状态为已提交
        examStudent.setStatus("SUBMITTED");
        examStudent.setSubmitTime(Instant.now());
        
        // 如果全是客观题，自动设置分数；否则分数为null，等待人工评分
        boolean hasSubjectiveQuestions = questions.stream()
                .anyMatch(q -> "ESSAY".equals(q.getQuestionType()));
        
        if (!hasSubjectiveQuestions) {
            examStudent.setScore(totalScore);
            examStudent.setStatus("GRADED");
        }
        
        // 保存状态
        examStudent = examStudentRepository.save(examStudent);
        
        return convertToStudentDTO(examStudent);
    }
} 