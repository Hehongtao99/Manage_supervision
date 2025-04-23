package com.example.auth.controller;

import com.example.auth.dto.FeeStandardDTO;
import com.example.auth.service.FeeStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fee-standards")
public class FeeStandardController {

    private final FeeStandardService feeStandardService;

    @Autowired
    public FeeStandardController(FeeStandardService feeStandardService) {
        this.feeStandardService = feeStandardService;
    }

    @GetMapping
    public ResponseEntity<List<FeeStandardDTO>> getAllFeeStandards() {
        List<FeeStandardDTO> feeStandards = feeStandardService.getAllFeeStandards();
        return ResponseEntity.ok(feeStandards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeeStandardDTO> getFeeStandardById(@PathVariable Long id) {
        FeeStandardDTO feeStandard = feeStandardService.getFeeStandardById(id);
        return ResponseEntity.ok(feeStandard);
    }

    @PostMapping
    public ResponseEntity<FeeStandardDTO> createFeeStandard(@RequestBody FeeStandardDTO feeStandardDTO) {
        FeeStandardDTO createdFeeStandard = feeStandardService.createFeeStandard(feeStandardDTO);
        return new ResponseEntity<>(createdFeeStandard, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeeStandardDTO> updateFeeStandard(
            @PathVariable Long id,
            @RequestBody FeeStandardDTO feeStandardDTO) {
        FeeStandardDTO updatedFeeStandard = feeStandardService.updateFeeStandard(id, feeStandardDTO);
        return ResponseEntity.ok(updatedFeeStandard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeeStandard(@PathVariable Long id) {
        feeStandardService.deleteFeeStandard(id);
        return ResponseEntity.noContent().build();
    }
} 