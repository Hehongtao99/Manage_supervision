package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.AdvertisementApplicationDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/advertisement")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;
    
    /**
     * 获取广告申请列表
     */
    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<AdvertisementApplicationDTO>> getAdvertisementList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String applicationNumber,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String adType,
            @RequestParam(required = false) String adNature,
            @RequestParam(required = false) BigDecimal minSize,
            @RequestParam(required = false) BigDecimal maxSize,
            @RequestParam(required = false) String status) {
        
        PageResponse<AdvertisementApplicationDTO> response = 
                advertisementService.getAdvertisementApplicationList(
                        page, size, applicationNumber, area, location, 
                        adType, adNature, minSize, maxSize, status);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建广告申请
     */
    @PostMapping
    public ResponseEntity<AdvertisementApplicationDTO> createAdvertisement(
            @RequestBody AdvertisementApplicationDTO dto) {
        
        AdvertisementApplicationDTO createdDto = 
                advertisementService.createAdvertisementApplication(dto);
        
        return ResponseEntity.ok(createdDto);
    }
    
    /**
     * 更新广告申请
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementApplicationDTO> updateAdvertisement(
            @PathVariable Long id,
            @RequestBody AdvertisementApplicationDTO dto) {
        
        AdvertisementApplicationDTO updatedDto = 
                advertisementService.updateAdvertisementApplication(id, dto);
        
        return ResponseEntity.ok(updatedDto);
    }
    
    /**
     * 审核广告申请
     */
    @PostMapping("/{id}/review")
    @RequireRole("ADMIN")
    public ResponseEntity<AdvertisementApplicationDTO> reviewAdvertisement(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String status = request.get("status");
        String remark = request.get("remark");
        
        AdvertisementApplicationDTO updatedDto = 
                advertisementService.reviewAdvertisementApplication(id, status, remark);
        
        return ResponseEntity.ok(updatedDto);
    }
    
    /**
     * 获取广告申请详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementApplicationDTO> getAdvertisementDetail(
            @PathVariable Long id) {
        
        AdvertisementApplicationDTO dto = 
                advertisementService.getAdvertisementApplicationById(id);
        
        return ResponseEntity.ok(dto);
    }
} 