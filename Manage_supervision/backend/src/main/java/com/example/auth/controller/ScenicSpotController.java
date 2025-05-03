package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ScenicSpotDTO;
import com.example.auth.service.ScenicSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/scenic-spots")
public class ScenicSpotController {

    @Autowired
    private ScenicSpotService scenicSpotService;
    
    /**
     * 分页获取景区列表
     */
    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<ScenicSpotDTO>> getScenicSpotList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status) {
        
        PageResponse<ScenicSpotDTO> response = scenicSpotService.getScenicSpotList(
                page, size, name, provinceId, cityId, districtId, level, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取景区详情
     */
    @GetMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<ScenicSpotDTO> getScenicSpotDetail(@PathVariable Long id) {
        ScenicSpotDTO scenicSpot = scenicSpotService.getScenicSpotById(id);
        return ResponseEntity.ok(scenicSpot);
    }
    
    /**
     * 添加景区
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<ScenicSpotDTO> addScenicSpot(@RequestBody ScenicSpotDTO scenicSpotDTO) {
        try {
            ScenicSpotDTO result = scenicSpotService.addScenicSpot(scenicSpotDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新景区
     */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<ScenicSpotDTO> updateScenicSpot(@PathVariable Long id, @RequestBody ScenicSpotDTO scenicSpotDTO) {
        try {
            ScenicSpotDTO result = scenicSpotService.updateScenicSpot(id, scenicSpotDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除景区
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteScenicSpot(@PathVariable Long id) {
        try {
            boolean success = scenicSpotService.deleteScenicSpot(id);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 切换景区状态
     */
    @PostMapping("/{id}/toggle-status")
    @RequireRole("ADMIN")
    public ResponseEntity<?> toggleScenicSpotStatus(@PathVariable Long id) {
        boolean success = scenicSpotService.toggleScenicSpotStatus(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取热门景区
     */
    @GetMapping("/hot")
    @RequireRole("ADMIN")
    public ResponseEntity<List<ScenicSpotDTO>> getHotScenicSpots(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<ScenicSpotDTO> hotSpots = scenicSpotService.getHotScenicSpots(limit);
        return ResponseEntity.ok(hotSpots);
    }
} 