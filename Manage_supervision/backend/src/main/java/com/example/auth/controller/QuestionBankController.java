package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.dto.QuestionBankDTO;
import com.example.auth.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/question-banks")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    @PostMapping
    public ResponseEntity<QuestionBankDTO> createQuestionBank(@RequestBody QuestionBankDTO questionBankDTO) {
        QuestionBankDTO createdQuestionBank = questionBankService.createQuestionBank(questionBankDTO);
        return new ResponseEntity<>(createdQuestionBank, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionBankDTO> updateQuestionBank(@PathVariable Long id, @RequestBody QuestionBankDTO questionBankDTO) {
        QuestionBankDTO updatedQuestionBank = questionBankService.updateQuestionBank(id, questionBankDTO);
        return ResponseEntity.ok(updatedQuestionBank);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionBank(@PathVariable Long id) {
        questionBankService.deleteQuestionBank(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionBankDTO> getQuestionBank(@PathVariable Long id) {
        QuestionBankDTO questionBank = questionBankService.getQuestionBank(id);
        return ResponseEntity.ok(questionBank);
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<Result> getQuestionBanksByCreator(
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QuestionBankDTO> questionBanksPage = questionBankService.getQuestionBanksByCreator(creatorId, status, pageable);
        
        Map<String, Object> data = new HashMap<>();
        data.put("content", questionBanksPage.getContent());
        data.put("currentPage", questionBanksPage.getNumber());
        data.put("totalItems", questionBanksPage.getTotalElements());
        data.put("totalPages", questionBanksPage.getTotalPages());
        
        return ResponseEntity.ok(Result.success(data));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchQuestionBanks(
            @RequestParam String keyword,
            @RequestParam Long creatorId,
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<QuestionBankDTO> questionBanksPage = questionBankService.searchQuestionBanks(keyword, creatorId, status, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", questionBanksPage.getContent());
        response.put("currentPage", questionBanksPage.getNumber());
        response.put("totalItems", questionBanksPage.getTotalElements());
        response.put("totalPages", questionBanksPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Boolean> archiveQuestionBank(@PathVariable Long id) {
        boolean result = questionBankService.archiveQuestionBank(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Boolean> activateQuestionBank(@PathVariable Long id) {
        boolean result = questionBankService.activateQuestionBank(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/question-count")
    public ResponseEntity<Long> getQuestionCountInBank(@PathVariable Long id) {
        long count = questionBankService.getQuestionCountInBank(id);
        return ResponseEntity.ok(count);
    }
} 