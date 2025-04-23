package com.example.auth.service;

import com.example.auth.dto.FeeStandardDTO;
import com.example.auth.entity.FeeStandard;

import java.util.List;

public interface FeeStandardService {
    
    List<FeeStandardDTO> getAllFeeStandards();
    
    FeeStandardDTO getFeeStandardById(Long id);
    
    FeeStandardDTO createFeeStandard(FeeStandardDTO feeStandardDTO);
    
    FeeStandardDTO updateFeeStandard(Long id, FeeStandardDTO feeStandardDTO);
    
    void deleteFeeStandard(Long id);
} 