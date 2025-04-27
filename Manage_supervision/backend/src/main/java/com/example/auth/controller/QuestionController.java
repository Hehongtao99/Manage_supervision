package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.dto.QuestionDTO;
import com.example.auth.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(@RequestBody QuestionDTO questionDTO) {
        QuestionDTO createdQuestion = questionService.createQuestion(questionDTO);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable Long id, @RequestBody QuestionDTO questionDTO) {
        QuestionDTO updatedQuestion = questionService.updateQuestion(id, questionDTO);
        return ResponseEntity.ok(updatedQuestion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id) {
        QuestionDTO question = questionService.getQuestion(id);
        return ResponseEntity.ok(question);
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<Map<String, Object>> getQuestionsByCreator(
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QuestionDTO> questionsPage = questionService.getQuestionsByCreator(creatorId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", questionsPage.getContent());
        response.put("currentPage", questionsPage.getNumber());
        response.put("totalItems", questionsPage.getTotalElements());
        response.put("totalPages", questionsPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}/creator/{creatorId}")
    public ResponseEntity<Map<String, Object>> getQuestionsByType(
            @PathVariable String type,
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QuestionDTO> questionsPage = questionService.getQuestionsByType(type, creatorId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", questionsPage.getContent());
        response.put("currentPage", questionsPage.getNumber());
        response.put("totalItems", questionsPage.getTotalElements());
        response.put("totalPages", questionsPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bank/{bankId}")
    public ResponseEntity<Result> getQuestionsByBank(
            @PathVariable Long bankId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QuestionDTO> questionsPage = questionService.getQuestionsByQuestionBank(bankId, pageable);
        
        Map<String, Object> data = new HashMap<>();
        data.put("content", questionsPage.getContent());
        data.put("currentPage", questionsPage.getNumber());
        data.put("totalItems", questionsPage.getTotalElements());
        data.put("totalPages", questionsPage.getTotalPages());
        
        return ResponseEntity.ok(Result.success(data));
    }

    @GetMapping("/bank/{bankId}/type/{type}")
    public ResponseEntity<Map<String, Object>> getQuestionsByBankAndType(
            @PathVariable Long bankId,
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QuestionDTO> questionsPage = questionService.getQuestionsByQuestionBankAndType(bankId, type, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", questionsPage.getContent());
        response.put("currentPage", questionsPage.getNumber());
        response.put("totalItems", questionsPage.getTotalElements());
        response.put("totalPages", questionsPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QuestionDTO> questionsPage = questionService.searchQuestions(keyword, creatorId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", questionsPage.getContent());
        response.put("currentPage", questionsPage.getNumber());
        response.put("totalItems", questionsPage.getTotalElements());
        response.put("totalPages", questionsPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{questionId}/bank/{bankId}")
    public ResponseEntity<Boolean> addQuestionToBank(
            @PathVariable Long questionId,
            @PathVariable Long bankId) {
        boolean result = questionService.addQuestionToBank(questionId, bankId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{questionId}/bank/{bankId}")
    public ResponseEntity<Boolean> removeQuestionFromBank(
            @PathVariable Long questionId,
            @PathVariable Long bankId) {
        boolean result = questionService.removeQuestionFromBank(questionId, bankId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{questionId}/banks")
    public ResponseEntity<List<Long>> getQuestionBankIds(@PathVariable Long questionId) {
        List<Long> bankIds = questionService.getQuestionBankIdsByQuestionId(questionId);
        return ResponseEntity.ok(bankIds);
    }
} 