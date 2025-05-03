package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.HotelDTO;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;
    
    /**
     * 分页获取酒店列表
     */
    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<PageResponse<HotelDTO>> getHotelList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Long scenicSpotId,
            @RequestParam(required = false) String status) {
        
        PageResponse<HotelDTO> response = hotelService.getHotelList(
                page, size, name, provinceId, cityId, districtId, level, scenicSpotId, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取酒店详情
     */
    @GetMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<HotelDTO> getHotelDetail(@PathVariable Long id) {
        HotelDTO hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotel);
    }
    
    /**
     * 添加酒店
     */
    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<HotelDTO> addHotel(@RequestBody HotelDTO hotelDTO) {
        try {
            HotelDTO result = hotelService.addHotel(hotelDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新酒店
     */
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<HotelDTO> updateHotel(@PathVariable Long id, @RequestBody HotelDTO hotelDTO) {
        try {
            HotelDTO result = hotelService.updateHotel(id, hotelDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除酒店
     */
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id) {
        try {
            boolean success = hotelService.deleteHotel(id);
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
     * 切换酒店状态
     */
    @PostMapping("/{id}/toggle-status")
    @RequireRole("ADMIN")
    public ResponseEntity<?> toggleHotelStatus(@PathVariable Long id) {
        boolean success = hotelService.toggleHotelStatus(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取景区周边酒店
     */
    @GetMapping("/scenic-spot/{scenicSpotId}")
    @RequireRole("ADMIN")
    public ResponseEntity<List<HotelDTO>> getHotelsByScenicSpot(
            @PathVariable Long scenicSpotId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        List<HotelDTO> hotels = hotelService.getHotelsByScenicSpot(scenicSpotId, limit);
        return ResponseEntity.ok(hotels);
    }
} 