package com.example.auth.service.impl;

import com.example.auth.entity.ParentChildRelation;
import com.example.auth.repository.ParentChildRelationRepository;
import com.example.auth.service.ParentChildRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 家长与子女关系服务实现类
 */
@Service
@Slf4j
public class ParentChildRelationServiceImpl implements ParentChildRelationService {

    @Autowired
    private ParentChildRelationRepository parentChildRelationRepository;

    @Override
    public boolean isParentOf(Long parentId, Long childId) {
        Optional<ParentChildRelation> relation = parentChildRelationRepository.findByParentIdAndChildIdAndStatus(
                parentId, childId, "active");
        return relation.isPresent();
    }

    @Override
    public ParentChildRelation getRelation(Long parentId, Long childId) {
        Optional<ParentChildRelation> relation = parentChildRelationRepository.findByParentIdAndChildIdAndStatus(
                parentId, childId, "active");
        return relation.orElse(null);
    }

    @Override
    public List<ParentChildRelation> getConfirmedRelationsByParentId(Long parentId) {
        return parentChildRelationRepository.findByParentIdAndStatus(parentId, "active");
    }
    
    @Override
    public boolean hasRelation(Long parentId, Long childId) {
        return parentChildRelationRepository.existsByParentIdAndChildId(parentId, childId);
    }
} 