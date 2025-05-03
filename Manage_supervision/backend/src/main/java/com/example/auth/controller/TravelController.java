package com.example.auth.controller;

import com.example.auth.model.dto.HotelDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RegionDTO;
import com.example.auth.model.dto.ScenicSpotDTO;
import com.example.auth.service.HotelService;
import com.example.auth.service.RegionService;
import com.example.auth.service.ScenicSpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 旅游推荐控制器 - 用户访问接口
 */
@RestController
@RequestMapping("/api/travel")
public class TravelController {
    
    private static final Logger logger = LoggerFactory.getLogger(TravelController.class);

    @Autowired
    private ScenicSpotService scenicSpotService;
    
    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private RegionService regionService;
    
    /**
     * 根据地区获取景区列表
     */
    @GetMapping("/scenic-spots")
    public ResponseEntity<PageResponse<ScenicSpotDTO>> getScenicSpotsByRegion(
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("获取景区列表请求: provinceId={}, cityId={}, districtId={}, page={}, size={}", 
                provinceId, cityId, districtId, page, size);
        
        try {
            // 只获取启用状态的景区
            PageResponse<ScenicSpotDTO> response = scenicSpotService.getScenicSpotList(
                    page, size, null, provinceId, cityId, districtId, null, "active");
            
            logger.info("成功获取景区列表, 总数: {}", response.getTotal());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取景区列表失败", e);
            return ResponseEntity.status(500).body(new PageResponse<>(Collections.emptyList(), 0L, page, size));
        }
    }
    
    /**
     * 获取热门景区
     */
    @GetMapping("/scenic-spots/hot")
    public ResponseEntity<List<ScenicSpotDTO>> getHotScenicSpots(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        
        logger.info("获取热门景区请求: limit={}", limit);
        
        try {
            List<ScenicSpotDTO> hotSpots = scenicSpotService.getHotScenicSpots(limit);
            logger.info("成功获取热门景区, 数量: {}", hotSpots.size());
            return ResponseEntity.ok(hotSpots);
        } catch (Exception e) {
            logger.error("获取热门景区失败", e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    
    /**
     * 获取景区周边酒店
     */
    @GetMapping("/hotels/scenic-spot/{scenicSpotId}")
    public ResponseEntity<List<HotelDTO>> getHotelsByScenicSpot(
            @PathVariable Long scenicSpotId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        
        logger.info("获取景区周边酒店请求: scenicSpotId={}, limit={}", scenicSpotId, limit);
        
        try {
            List<HotelDTO> hotels = hotelService.getHotelsByScenicSpot(scenicSpotId, limit);
            logger.info("成功获取景区周边酒店, 数量: {}", hotels.size());
            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            logger.error("获取景区周边酒店失败", e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * 获取指定级别的地区数据
     */
    @GetMapping("/regions/level/{level}")
    public ResponseEntity<List<RegionDTO>> getRegionsByLevel(
            @PathVariable Integer level) {
        
        logger.info("获取地区数据请求: level={}", level);
        
        try {
            List<RegionDTO> regions = regionService.getRegionsByLevel(level);
            logger.info("成功获取地区数据, 数量: {}", regions.size());
            return ResponseEntity.ok(regions);
        } catch (Exception e) {
            logger.error("获取地区数据失败", e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    
    /**
     * 获取指定父级ID的子地区
     */
    @GetMapping("/regions/parent/{parentId}")
    public ResponseEntity<List<RegionDTO>> getRegionsByParentId(
            @PathVariable Long parentId) {
        
        logger.info("获取子地区数据请求: parentId={}", parentId);
        
        try {
            List<RegionDTO> regions = regionService.getRegionsByParentId(parentId);
            logger.info("成功获取子地区数据, 数量: {}", regions.size());
            return ResponseEntity.ok(regions);
        } catch (Exception e) {
            logger.error("获取子地区数据失败", e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
} 