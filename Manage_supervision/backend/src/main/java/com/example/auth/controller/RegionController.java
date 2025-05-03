package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/regions")
public class RegionController {

    @Autowired
    private RegionService regionService;
    
    /**
     * 获取地区树形结构
     */
    @GetMapping("/tree")
    @RequireRole("ADMIN")
    public ResponseEntity<List<RegionDTO>> getRegionTree() {
        List<RegionDTO> regionTree = regionService.getRegionTree();
        return ResponseEntity.ok(regionTree);
    }
    
    /**
     * 根据级别获取地区列表
     */
    @GetMapping("/level/{level}")
    @RequireRole("ADMIN")
    public ResponseEntity<List<RegionDTO>> getRegionsByLevel(@PathVariable Integer level) {
        List<RegionDTO> regions = regionService.getRegionsByLevel(level);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * 获取子地区列表 - 原有接口
     */
    @GetMapping("/children/{parentId}")
    @RequireRole("ADMIN")
    public ResponseEntity<List<RegionDTO>> getChildRegions(@PathVariable Long parentId) {
        List<RegionDTO> regions = regionService.getRegionsByParentId(parentId);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * 获取子地区列表 - 新增接口，与前端请求匹配
     */
    @GetMapping("/parent/{parentId}")
    @RequireRole("ADMIN")
    public ResponseEntity<List<RegionDTO>> getRegionsByParent(@PathVariable Long parentId) {
        List<RegionDTO> regions = regionService.getRegionsByParentId(parentId);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * 获取地区详情
     */
    @GetMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<RegionDTO> getRegionDetail(@PathVariable Long id) {
        RegionDTO region = regionService.getRegionById(id);
        return ResponseEntity.ok(region);
    }
    
    /**
     * 添加地区
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<RegionDTO> addRegion(@RequestBody RegionDTO regionDTO) {
        RegionDTO result = regionService.addRegion(regionDTO);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 更新地区
     */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<RegionDTO> updateRegion(@PathVariable Long id, @RequestBody RegionDTO regionDTO) {
        RegionDTO result = regionService.updateRegion(id, regionDTO);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 删除地区
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteRegion(@PathVariable Long id) {
        try {
            boolean success = regionService.deleteRegion(id);
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
     * 切换地区状态
     */
    @PostMapping("/{id}/toggle-status")
    @RequireRole("ADMIN")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        boolean success = regionService.toggleRegionStatus(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取地区路径
     */
    @GetMapping("/{id}/path")
    @RequireRole("ADMIN")
    public ResponseEntity<List<RegionDTO>> getRegionPath(@PathVariable Long id) {
        List<RegionDTO> path = regionService.getRegionPath(id);
        return ResponseEntity.ok(path);
    }
} 