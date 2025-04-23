package com.example.auth.service.impl;

import com.example.auth.dto.FeeStandardDTO;
import com.example.auth.entity.FeeStandard;
import com.example.auth.repository.FeeStandardRepository;
import com.example.auth.service.FeeStandardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeStandardServiceImpl implements FeeStandardService {

    private final FeeStandardRepository feeStandardRepository;

    @Autowired
    public FeeStandardServiceImpl(FeeStandardRepository feeStandardRepository) {
        this.feeStandardRepository = feeStandardRepository;
    }

    @Override
    public List<FeeStandardDTO> getAllFeeStandards() {
        return feeStandardRepository.findAllActive().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FeeStandardDTO getFeeStandardById(Long id) {
        FeeStandard feeStandard = feeStandardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("费用标准不存在"));
        
        if (Boolean.TRUE.equals(feeStandard.getIsDeleted())) {
            throw new EntityNotFoundException("费用标准不存在");
        }
        
        return convertToDTO(feeStandard);
    }

    @Override
    @Transactional
    public FeeStandardDTO createFeeStandard(FeeStandardDTO feeStandardDTO) {
        FeeStandard feeStandard = new FeeStandard();
        BeanUtils.copyProperties(feeStandardDTO, feeStandard);
        feeStandard.setCreateTime(LocalDateTime.now());
        feeStandard.setUpdateTime(LocalDateTime.now());
        feeStandard.setIsDeleted(false);
        
        FeeStandard savedFeeStandard = feeStandardRepository.save(feeStandard);
        return convertToDTO(savedFeeStandard);
    }

    @Override
    @Transactional
    public FeeStandardDTO updateFeeStandard(Long id, FeeStandardDTO feeStandardDTO) {
        FeeStandard existingFeeStandard = feeStandardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("费用标准不存在"));
        
        if (Boolean.TRUE.equals(existingFeeStandard.getIsDeleted())) {
            throw new EntityNotFoundException("费用标准不存在");
        }
        
        existingFeeStandard.setFeeName(feeStandardDTO.getFeeName());
        existingFeeStandard.setAmount(feeStandardDTO.getAmount());
        existingFeeStandard.setDescription(feeStandardDTO.getDescription());
        existingFeeStandard.setUpdateTime(LocalDateTime.now());
        
        FeeStandard updatedFeeStandard = feeStandardRepository.save(existingFeeStandard);
        return convertToDTO(updatedFeeStandard);
    }

    @Override
    @Transactional
    public void deleteFeeStandard(Long id) {
        FeeStandard feeStandard = feeStandardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("费用标准不存在"));
                
        feeStandard.setIsDeleted(true);
        feeStandard.setUpdateTime(LocalDateTime.now());
        feeStandardRepository.save(feeStandard);
    }
    
    private FeeStandardDTO convertToDTO(FeeStandard feeStandard) {
        FeeStandardDTO dto = new FeeStandardDTO();
        BeanUtils.copyProperties(feeStandard, dto);
        return dto;
    }
} 