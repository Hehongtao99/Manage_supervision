package com.example.auth.service;

import com.example.auth.dto.QuestionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionService {
    
    QuestionDTO createQuestion(QuestionDTO questionDTO);
    
    QuestionDTO updateQuestion(Long id, QuestionDTO questionDTO);
    
    void deleteQuestion(Long id);
    
    QuestionDTO getQuestion(Long id);
    
    Page<QuestionDTO> getQuestionsByCreator(Long creatorId, Pageable pageable);
    
    Page<QuestionDTO> getQuestionsByType(String type, Long creatorId, Pageable pageable);
    
    Page<QuestionDTO> getQuestionsByQuestionBank(Long questionBankId, Pageable pageable);
    
    Page<QuestionDTO> getQuestionsByQuestionBankAndType(Long questionBankId, String type, Pageable pageable);
    
    Page<QuestionDTO> searchQuestions(String keyword, Long creatorId, Pageable pageable);
    
    boolean addQuestionToBank(Long questionId, Long questionBankId);
    
    boolean removeQuestionFromBank(Long questionId, Long questionBankId);
    
    List<Long> getQuestionBankIdsByQuestionId(Long questionId);
} 