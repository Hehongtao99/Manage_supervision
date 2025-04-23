package com.example.auth.service;

import com.example.auth.dto.BillDTO;
import com.example.auth.dto.BillGenerateDTO;
import com.example.auth.dto.BillPaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BillService {
    
    Page<BillDTO> getAllBills(Pageable pageable);
    
    BillDTO getBillById(Long id);
    
    List<BillDTO> getBillsByStudentId(Long studentId);
    
    Page<BillDTO> getBillsByStudentId(Long studentId, Pageable pageable);
    
    Page<BillDTO> getBillsByStatus(Boolean status, Pageable pageable);
    
    BillDTO updateBillStatus(Long id, Boolean status);
    
    BillDTO payBill(Long id, BillPaymentDTO paymentDTO);
    
    List<BillDTO> generateBills(BillGenerateDTO generateDTO);
    
    void deleteBill(Long id);
    
    // 根据班级ID获取该班级所有学生的账单
    Page<BillDTO> getBillsByClassId(Long classId, Pageable pageable);
    
    // 根据教师ID获取其管理的班级的所有学生账单
    Page<BillDTO> getBillsByTeacherId(Long teacherId, Pageable pageable);
} 