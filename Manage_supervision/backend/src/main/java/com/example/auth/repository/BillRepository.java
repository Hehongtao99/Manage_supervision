package com.example.auth.repository;

import com.example.auth.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false AND b.studentId = :studentId ORDER BY b.createTime DESC")
    List<Bill> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false ORDER BY b.createTime DESC")
    Page<Bill> findAllActive(Pageable pageable);
    
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false AND b.status = :status ORDER BY b.createTime DESC")
    Page<Bill> findByStatus(@Param("status") Boolean status, Pageable pageable);
    
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false AND b.studentId = :studentId ORDER BY b.createTime DESC")
    Page<Bill> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);
    
    @Query("SELECT DISTINCT b.studentId FROM Bill b WHERE b.feeStandardId = :feeStandardId AND b.isDeleted = false")
    List<Long> findStudentIdsWithFeeStandard(@Param("feeStandardId") Long feeStandardId);
    
    @Query("SELECT b FROM Bill b WHERE b.isDeleted = false AND b.studentId IN :studentIds ORDER BY b.createTime DESC")
    Page<Bill> findByStudentIdIn(@Param("studentIds") List<Long> studentIds, Pageable pageable);
} 