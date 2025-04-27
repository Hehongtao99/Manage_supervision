package com.example.auth.service.impl;

import com.example.auth.dto.QuestionBankDTO;
import com.example.auth.entity.QuestionBank;
import com.example.auth.entity.User;
import com.example.auth.repository.QuestionBankQuestionRepository;
import com.example.auth.repository.QuestionBankRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QuestionBankServiceImpl implements QuestionBankService {

    @Autowired
    private QuestionBankRepository questionBankRepository;
    
    @Autowired
    private QuestionBankQuestionRepository questionBankQuestionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public QuestionBankDTO createQuestionBank(QuestionBankDTO questionBankDTO) {
        QuestionBank questionBank = new QuestionBank();
        questionBank.setName(questionBankDTO.getName());
        questionBank.setDescription(questionBankDTO.getDescription());
        questionBank.setCreatorId(questionBankDTO.getCreatorId());
        questionBank.setStatus("ACTIVE");
        
        QuestionBank savedQuestionBank = questionBankRepository.save(questionBank);
        return convertToDTO(savedQuestionBank);
    }

    @Override
    @Transactional
    public QuestionBankDTO updateQuestionBank(Long id, QuestionBankDTO questionBankDTO) {
        Optional<QuestionBank> optionalQuestionBank = questionBankRepository.findById(id);
        if (optionalQuestionBank.isPresent()) {
            QuestionBank questionBank = optionalQuestionBank.get();
            
            // 只有创建者可以更新
            if (!questionBank.getCreatorId().equals(questionBankDTO.getCreatorId())) {
                throw new RuntimeException("You don't have permission to update this question bank");
            }
            
            questionBank.setName(questionBankDTO.getName());
            questionBank.setDescription(questionBankDTO.getDescription());
            questionBank.setUpdateTime(LocalDateTime.now());
            
            QuestionBank updatedQuestionBank = questionBankRepository.save(questionBank);
            return convertToDTO(updatedQuestionBank);
        }
        
        throw new RuntimeException("Question bank not found with id: " + id);
    }

    @Override
    @Transactional
    public void deleteQuestionBank(Long id) {
        if (questionBankRepository.existsById(id)) {
            // 删除所有相关的题库-题目关联
            questionBankQuestionRepository.deleteByQuestionBankId(id);
            
            // 删除题库
            questionBankRepository.deleteById(id);
        } else {
            throw new RuntimeException("Question bank not found with id: " + id);
        }
    }

    @Override
    public QuestionBankDTO getQuestionBank(Long id) {
        Optional<QuestionBank> optionalQuestionBank = questionBankRepository.findById(id);
        if (optionalQuestionBank.isPresent()) {
            return convertToDTO(optionalQuestionBank.get());
        }
        
        throw new RuntimeException("Question bank not found with id: " + id);
    }

    @Override
    public Page<QuestionBankDTO> getQuestionBanksByCreator(Long creatorId, String status, Pageable pageable) {
        Page<QuestionBank> questionBanks = questionBankRepository.findByCreatorIdAndStatusOrderByCreateTimeDesc(creatorId, status, pageable);
        return questionBanks.map(this::convertToDTO);
    }

    @Override
    public Page<QuestionBankDTO> searchQuestionBanks(String keyword, Long creatorId, String status, Pageable pageable) {
        Page<QuestionBank> questionBanks = questionBankRepository.searchQuestionBanks(keyword, creatorId, status, pageable);
        return questionBanks.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public boolean archiveQuestionBank(Long id) {
        Optional<QuestionBank> optionalQuestionBank = questionBankRepository.findById(id);
        if (optionalQuestionBank.isPresent()) {
            QuestionBank questionBank = optionalQuestionBank.get();
            questionBank.setStatus("ARCHIVED");
            questionBank.setUpdateTime(LocalDateTime.now());
            questionBankRepository.save(questionBank);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean activateQuestionBank(Long id) {
        Optional<QuestionBank> optionalQuestionBank = questionBankRepository.findById(id);
        if (optionalQuestionBank.isPresent()) {
            QuestionBank questionBank = optionalQuestionBank.get();
            questionBank.setStatus("ACTIVE");
            questionBank.setUpdateTime(LocalDateTime.now());
            questionBankRepository.save(questionBank);
            return true;
        }
        return false;
    }

    @Override
    public long getQuestionCountInBank(Long questionBankId) {
        return questionBankQuestionRepository.countByQuestionBankId(questionBankId);
    }
    
    private QuestionBankDTO convertToDTO(QuestionBank questionBank) {
        QuestionBankDTO dto = new QuestionBankDTO();
        dto.setId(questionBank.getId());
        dto.setName(questionBank.getName());
        dto.setDescription(questionBank.getDescription());
        dto.setCreatorId(questionBank.getCreatorId());
        
        // 获取创建者名称
        Optional<User> creator = userRepository.findById(questionBank.getCreatorId());
        creator.ifPresent(user -> dto.setCreatorName(user.getRealName()));
        
        dto.setCreateTime(questionBank.getCreateTime());
        dto.setUpdateTime(questionBank.getUpdateTime());
        dto.setStatus(questionBank.getStatus());
        
        // 获取题目数量
        dto.setQuestionCount(getQuestionCountInBank(questionBank.getId()));
        
        return dto;
    }
} 