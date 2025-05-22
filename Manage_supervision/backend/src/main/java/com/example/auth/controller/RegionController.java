package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 省市区街道管理控制器
 */
@RestController
@RequestMapping("/api/regions")
public class RegionController {

    @Autowired
    private RegionService regionService;
    
    /**
     * 获取区域树
     */
    @GetMapping("/tree")
    public ResponseEntity<List<RegionDTO>> getRegionTree(
            @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(regionService.getRegionTree(parentId));
    }
    
    /**
     * 获取区域列表（根据父ID查询）
     */
    @GetMapping("/list")
    public ResponseEntity<List<RegionDTO>> getRegionList(
            @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(regionService.getRegionList(parentId));
    }
    
    /**
     * 获取区域详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<RegionDTO> getRegionDetail(@PathVariable Long id) {
        RegionDTO region = regionService.getRegionById(id);
        if (region == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(region);
    }
    
    /**
     * 新增或更新区域
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<RegionDTO> saveRegion(@RequestBody RegionDTO regionDTO) {
        return ResponseEntity.ok(regionService.saveRegion(regionDTO));
    }
    
    /**
     * 删除区域
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
        boolean success = regionService.deleteRegion(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取区域路径
     */
    @GetMapping("/{id}/path")
    public ResponseEntity<List<RegionDTO>> getRegionPath(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.getRegionPath(id));
    }
    
    /**
     * 上传街道图片
     */
    @PostMapping("/{id}/image")
    @RequireRole("ADMIN")
    public ResponseEntity<String> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = regionService.uploadStreetImage(file, id);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * 获取指定父级ID下的所有地区
     *
     * @param parentId 父级ID，如果为0则获取省级数据
     * @return 地区列表
     */
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<RegionDTO>> getRegionsByParentId(@PathVariable Long parentId) {
        List<RegionDTO> regions = regionService.getRegionList(parentId == 0 ? null : parentId);
        return ResponseEntity.ok(regions);
    }
} 