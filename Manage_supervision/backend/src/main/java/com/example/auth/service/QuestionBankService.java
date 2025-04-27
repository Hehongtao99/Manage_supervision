package com.example.auth.service;

import com.example.auth.dto.QuestionBankDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionBankService {
    
    QuestionBankDTO createQuestionBank(QuestionBankDTO questionBankDTO);
    
    QuestionBankDTO updateQuestionBank(Long id, QuestionBankDTO questionBankDTO);
    
    void deleteQuestionBank(Long id);
    
    QuestionBankDTO getQuestionBank(Long id);
    
    Page<QuestionBankDTO> getQuestionBanksByCreator(Long creatorId, String status, Pageable pageable);
    
    Page<QuestionBankDTO> searchQuestionBanks(String keyword, Long creatorId, String status, Pageable pageable);
    
    boolean archiveQuestionBank(Long id);
    
    boolean activateQuestionBank(Long id);
    
    long getQuestionCountInBank(Long questionBankId);
} 