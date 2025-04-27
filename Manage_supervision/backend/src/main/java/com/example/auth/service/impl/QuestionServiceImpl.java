package com.example.auth.service.impl;

import com.example.auth.dto.QuestionDTO;
import com.example.auth.entity.Question;
import com.example.auth.entity.QuestionBankQuestion;
import com.example.auth.entity.User;
import com.example.auth.repository.QuestionBankQuestionRepository;
import com.example.auth.repository.QuestionRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.QuestionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionBankQuestionRepository questionBankQuestionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        Question question = new Question();
        question.setTitle(questionDTO.getTitle());
        question.setContent(questionDTO.getContent());
        question.setType(questionDTO.getType());
        
        try {
            if (questionDTO.getOptions() != null) {
                question.setOptions(objectMapper.writeValueAsString(questionDTO.getOptions()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process options", e);
        }
        
        question.setAnswer(questionDTO.getAnswer());
        question.setAnalysis(questionDTO.getAnalysis());
        question.setDifficulty(questionDTO.getDifficulty());
        question.setCreatorId(questionDTO.getCreatorId());
        
        Question savedQuestion = questionRepository.save(question);
        
        // 将问题添加到题库
        if (questionDTO.getQuestionBankIds() != null && !questionDTO.getQuestionBankIds().isEmpty()) {
            for (Long bankId : questionDTO.getQuestionBankIds()) {
                addQuestionToBank(savedQuestion.getId(), bankId);
            }
        }
        
        return convertToDTO(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionDTO updateQuestion(Long id, QuestionDTO questionDTO) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            
            // 只有创建者可以更新
            if (!question.getCreatorId().equals(questionDTO.getCreatorId())) {
                throw new RuntimeException("You don't have permission to update this question");
            }
            
            question.setTitle(questionDTO.getTitle());
            question.setContent(questionDTO.getContent());
            question.setType(questionDTO.getType());
            
            try {
                if (questionDTO.getOptions() != null) {
                    question.setOptions(objectMapper.writeValueAsString(questionDTO.getOptions()));
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to process options", e);
            }
            
            question.setAnswer(questionDTO.getAnswer());
            question.setAnalysis(questionDTO.getAnalysis());
            question.setDifficulty(questionDTO.getDifficulty());
            question.setUpdateTime(LocalDateTime.now());
            
            Question updatedQuestion = questionRepository.save(question);
            
            // 更新题库关联
            if (questionDTO.getQuestionBankIds() != null) {
                // 获取当前关联的题库
                List<Long> currentBankIds = getQuestionBankIdsByQuestionId(id);
                
                // 需要添加的题库
                List<Long> banksToAdd = questionDTO.getQuestionBankIds().stream()
                        .filter(bankId -> !currentBankIds.contains(bankId))
                        .collect(Collectors.toList());
                
                // 需要移除的题库
                List<Long> banksToRemove = currentBankIds.stream()
                        .filter(bankId -> !questionDTO.getQuestionBankIds().contains(bankId))
                        .collect(Collectors.toList());
                
                // 添加新的关联
                for (Long bankId : banksToAdd) {
                    addQuestionToBank(id, bankId);
                }
                
                // 移除旧的关联
                for (Long bankId : banksToRemove) {
                    removeQuestionFromBank(id, bankId);
                }
            }
            
            return convertToDTO(updatedQuestion);
        }
        
        throw new RuntimeException("Question not found with id: " + id);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        if (questionRepository.existsById(id)) {
            // 删除所有相关的题库-题目关联
            List<QuestionBankQuestion> associations = questionBankQuestionRepository.findByQuestionId(id);
            if (!associations.isEmpty()) {
                questionBankQuestionRepository.deleteAll(associations);
            }
            
            // 删除问题
            questionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Question not found with id: " + id);
        }
    }

    @Override
    public QuestionDTO getQuestion(Long id) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isPresent()) {
            return convertToDTO(optionalQuestion.get());
        }
        
        throw new RuntimeException("Question not found with id: " + id);
    }

    @Override
    public Page<QuestionDTO> getQuestionsByCreator(Long creatorId, Pageable pageable) {
        Page<Question> questions = questionRepository.findByCreatorId(creatorId, pageable);
        return questions.map(this::convertToDTO);
    }

    @Override
    public Page<QuestionDTO> getQuestionsByType(String type, Long creatorId, Pageable pageable) {
        Page<Question> questions = questionRepository.findByTypeAndCreatorId(type, creatorId, pageable);
        return questions.map(this::convertToDTO);
    }

    @Override
    public Page<QuestionDTO> getQuestionsByQuestionBank(Long questionBankId, Pageable pageable) {
        Page<Question> questions = questionRepository.findByQuestionBankId(questionBankId, pageable);
        return questions.map(this::convertToDTO);
    }

    @Override
    public Page<QuestionDTO> getQuestionsByQuestionBankAndType(Long questionBankId, String type, Pageable pageable) {
        Page<Question> questions = questionRepository.findByQuestionBankIdAndType(questionBankId, type, pageable);
        return questions.map(this::convertToDTO);
    }

    @Override
    public Page<QuestionDTO> searchQuestions(String keyword, Long creatorId, Pageable pageable) {
        Page<Question> questions = questionRepository.searchQuestions(keyword, creatorId, pageable);
        return questions.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public boolean addQuestionToBank(Long questionId, Long questionBankId) {
        // 检查是否已存在
        if (questionBankQuestionRepository.existsByQuestionBankIdAndQuestionId(questionBankId, questionId)) {
            return false;
        }
        
        // 创建新的关联
        QuestionBankQuestion association = new QuestionBankQuestion(questionBankId, questionId);
        questionBankQuestionRepository.save(association);
        return true;
    }

    @Override
    @Transactional
    public boolean removeQuestionFromBank(Long questionId, Long questionBankId) {
        if (questionBankQuestionRepository.existsByQuestionBankIdAndQuestionId(questionBankId, questionId)) {
            questionBankQuestionRepository.deleteByQuestionBankIdAndQuestionId(questionBankId, questionId);
            return true;
        }
        return false;
    }

    @Override
    public List<Long> getQuestionBankIdsByQuestionId(Long questionId) {
        List<QuestionBankQuestion> associations = questionBankQuestionRepository.findByQuestionId(questionId);
        return associations.stream()
                .map(QuestionBankQuestion::getQuestionBankId)
                .collect(Collectors.toList());
    }
    
    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        dto.setType(question.getType());
        
        try {
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                List<Map<String, Object>> options = objectMapper.readValue(
                        question.getOptions(), 
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                dto.setOptions(options);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process options", e);
        }
        
        dto.setAnswer(question.getAnswer());
        dto.setAnalysis(question.getAnalysis());
        dto.setDifficulty(question.getDifficulty());
        dto.setCreatorId(question.getCreatorId());
        
        // 获取创建者名称
        Optional<User> creator = userRepository.findById(question.getCreatorId());
        creator.ifPresent(user -> dto.setCreatorName(user.getRealName()));
        
        dto.setCreateTime(question.getCreateTime());
        dto.setUpdateTime(question.getUpdateTime());
        
        // 获取所属题库ID列表
        dto.setQuestionBankIds(getQuestionBankIdsByQuestionId(question.getId()));
        
        return dto;
    }
} 