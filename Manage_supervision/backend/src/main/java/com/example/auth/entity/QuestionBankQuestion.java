package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_bank_questions")
public class QuestionBankQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long questionBankId;
    
    @Column(nullable = false)
    private Long questionId;
    
    @Column(nullable = false)
    private LocalDateTime addTime;

    public QuestionBankQuestion() {
        this.addTime = LocalDateTime.now();
    }
    
    public QuestionBankQuestion(Long questionBankId, Long questionId) {
        this.questionBankId = questionBankId;
        this.questionId = questionId;
        this.addTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionBankId() {
        return questionBankId;
    }

    public void setQuestionBankId(Long questionBankId) {
        this.questionBankId = questionBankId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }
} 