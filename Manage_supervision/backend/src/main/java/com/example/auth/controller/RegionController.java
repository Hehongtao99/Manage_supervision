package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.entity.Region;
import com.example.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    @Autowired
    private RegionService regionService;
    
    /**
     * 获取省市区树形结构
     */
    @GetMapping("/tree")
    public ResponseEntity<List<RegionDTO>> getRegionTree() {
        List<RegionDTO> tree = regionService.getRegionTree();
        return ResponseEntity.ok(tree);
    }
    
    /**
     * 根据父ID获取子区域
     */
    @GetMapping("/children")
    public ResponseEntity<List<RegionDTO>> getRegionsByParentId(
            @RequestParam(required = false) Long parentId) {
        List<RegionDTO> regions = regionService.getRegionsByParentId(parentId != null ? parentId : 0L);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * 根据ID获取区域
     */
    @GetMapping("/{id}")
    public ResponseEntity<RegionDTO> getRegionById(@PathVariable Long id) {
        RegionDTO region = regionService.getRegionById(id);
        return ResponseEntity.ok(region);
    }
    
    /**
     * 添加区域（需要管理员权限）
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<RegionDTO> addRegion(@RequestBody Region region) {
        RegionDTO createdRegion = regionService.addRegion(region);
        return ResponseEntity.ok(createdRegion);
    }
    
    /**
     * 更新区域（需要管理员权限）
     */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<RegionDTO> updateRegion(
            @PathVariable Long id, 
            @RequestBody Region region) {
        RegionDTO updatedRegion = regionService.updateRegion(id, region);
        return ResponseEntity.ok(updatedRegion);
    }
    
    /**
     * 删除区域（需要管理员权限）
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteRegion(@PathVariable Long id) {
        boolean success = regionService.deleteRegion(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "该区域下存在子区域，无法删除"));
        }
    }
    
    /**
     * 获取完整地址路径
     */
    @GetMapping("/address-path")
    public ResponseEntity<String> getFullAddressPath(
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long streetId) {
        String addressPath = regionService.getFullAddressPath(provinceId, cityId, districtId, streetId);
        return ResponseEntity.ok(addressPath);
    }
} 