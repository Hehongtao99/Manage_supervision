package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.CompanionServiceDTO;
import com.example.auth.model.entity.User;
import com.example.auth.service.CompanionServiceService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/companion-services")
public class AdminCompanionServiceController {

    @Autowired
    private CompanionServiceService companionServiceService;
    
    @Autowired
    private UserContext userContext;
    
    /**
     * 获取所有陪玩服务（可按状态筛选）
     */
    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<Page<CompanionServiceDTO>> getAllServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<CompanionServiceDTO> services;
        if (status != null && !status.isEmpty()) {
            services = companionServiceService.getServicesByStatus(page, size, status);
        } else {
            services = companionServiceService.getAllServices(page, size);
        }
        
        return ResponseEntity.ok(services);
    }
    
    /**
     * 获取待审核的陪玩服务
     */
    @GetMapping("/pending")
    @RequireRole("ADMIN")
    public ResponseEntity<Page<CompanionServiceDTO>> getPendingServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<CompanionServiceDTO> services = companionServiceService.getServicesByStatus(page, size, "pending");
        
        return ResponseEntity.ok(services);
    }
    
    /**
     * 审核陪玩服务
     */
    @PostMapping("/{serviceId}/review")
    @RequireRole("ADMIN")
    public ResponseEntity<?> reviewService(
            @PathVariable Long serviceId,
            @RequestBody Map<String, Object> reviewData) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        boolean approved = (boolean) reviewData.get("approved");
        String reason = (String) reviewData.getOrDefault("reason", "");
        
        boolean success;
        
        if (approved) {
            // 审核通过
            success = companionServiceService.approveService(serviceId, currentUser.getId());
        } else {
            // 审核拒绝
            success = companionServiceService.rejectService(serviceId, reason, currentUser.getId());
        }
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", approved ? "审核通过成功" : "审核拒绝成功");
        } else {
            response.put("success", false);
            response.put("message", "审核操作失败");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新服务状态（上线/下线）
     */
    @PutMapping("/{serviceId}/status")
    @RequireRole("ADMIN")
    public ResponseEntity<?> updateServiceStatus(
            @PathVariable Long serviceId,
            @RequestParam String status) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 验证状态值
        if (!status.equals("active") && !status.equals("inactive")) {
            return ResponseEntity.badRequest().body("状态值无效，只能为active或inactive");
        }
        
        // 更新状态
        boolean success = companionServiceService.updateServiceStatus(serviceId, status);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", status.equals("active") ? "服务已上线" : "服务已下线");
        } else {
            response.put("success", false);
            response.put("message", "状态更新失败");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取陪玩服务详情
     */
    @GetMapping("/{serviceId}")
    @RequireRole("ADMIN")
    public ResponseEntity<CompanionServiceDTO> getServiceDetail(@PathVariable Long serviceId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        CompanionServiceDTO service = companionServiceService.getServiceById(serviceId);
        
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(service);
    }
} 