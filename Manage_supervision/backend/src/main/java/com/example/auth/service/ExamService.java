package com.example.auth.service;

import com.example.auth.dto.ExamDTO;
import com.example.auth.dto.ExamQuestionDTO;
import com.example.auth.dto.ExamStudentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 考试服务接口
 */
public interface ExamService {
    
    /**
     * 创建考试
     */
    ExamDTO createExam(ExamDTO examDTO, Long creatorId);
    
    /**
     * 更新考试
     */
    ExamDTO updateExam(Long examId, ExamDTO examDTO, Long creatorId);
    
    /**
     * 删除考试
     */
    void deleteExam(Long examId, Long creatorId);
    
    /**
     * 获取考试详情
     */
    ExamDTO getExamById(Long examId);
    
    /**
     * 获取教师创建的考试列表
     */
    List<ExamDTO> getExamsByCreatorId(Long creatorId);
    
    /**
     * 分页获取教师创建的考试列表
     */
    Page<ExamDTO> getExamsByCreatorId(Long creatorId, Pageable pageable);
    
    /**
     * 分页获取教师创建的考试列表（带状态过滤）
     */
    Page<ExamDTO> getExamsByCreatorIdAndStatus(Long creatorId, String status, Pageable pageable);
    
    /**
     * 搜索考试
     */
    Page<ExamDTO> searchExams(String keyword, Long creatorId, Pageable pageable);
    
    /**
     * 发布考试
     */
    ExamDTO publishExam(Long examId, Long creatorId);
    
    /**
     * 获取考试题目列表
     */
    List<ExamQuestionDTO> getExamQuestions(Long examId);
    
    /**
     * 获取考试学生列表
     */
    List<ExamStudentDTO> getExamStudents(Long examId);
    
    /**
     * 添加考试题目
     */
    void addExamQuestions(Long examId, List<ExamQuestionDTO> questions, Long creatorId);
    
    /**
     * 添加考试学生
     */
    void addExamStudents(Long examId, List<ExamStudentDTO> students, Long creatorId);
    
    /**
     * 删除考试题目
     */
    void removeExamQuestion(Long examId, Long questionId, Long creatorId);
    
    /**
     * 删除考试学生
     */
    void removeExamStudent(Long examId, Long studentId, Long creatorId);
    
    /**
     * 获取分配给学生的考试列表（分页）
     */
    Page<ExamDTO> getExamsByStudentId(Long studentId, String status, Pageable pageable);
    
    /**
     * 统计学生关联的考试数量
     */
    Long countExamsByStudentId(Long studentId);
    
    /**
     * 检查学生是否有权限参加考试
     */
    boolean isStudentAssignedToExam(Long examId, Long studentId);
    
    /**
     * 获取学生的考试状态
     */
    ExamStudentDTO getExamStudentStatus(Long examId, Long studentId);
    
    /**
     * 学生开始考试
     */
    ExamStudentDTO startExam(Long examId, Long studentId);
    
    /**
     * 获取考试题目（学生视图，不包含答案）
     */
    List<ExamQuestionDTO> getExamQuestionsForStudent(Long examId);
    
    /**
     * 学生提交考试答案
     */
    ExamStudentDTO submitExam(Long examId, Long studentId, Map<String, String> answers);
    
    /**
     * 获取学生考试答案和评分详情
     */
    Map<String, Object> getStudentExamAnswers(Long examId, Long studentId, Long creatorId);
    
    /**
     * 批阅学生考试（为主观题打分）
     */
    ExamStudentDTO gradeStudentExam(Long examId, Long studentId, Map<String, Object> gradeData, Long creatorId);
} 