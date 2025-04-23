package com.example.auth.repository;

import com.example.auth.entity.FeeStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeStandardRepository extends JpaRepository<FeeStandard, Long> {
    
    @Query("SELECT f FROM FeeStandard f WHERE f.isDeleted = false ORDER BY f.createTime DESC")
    List<FeeStandard> findAllActive();
} 