package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.CompanionServiceDTO;
import com.example.auth.model.entity.CompanionService;
import com.example.auth.model.entity.User;
import com.example.auth.service.CompanionServiceService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companion/services")
public class CompanionServiceController {

    @Autowired
    private CompanionServiceService companionServiceService;
    
    @Autowired
    private UserContext userContext;
    
    /**
     * 创建陪玩服务
     */
    @PostMapping
    public ResponseEntity<?> createService(@RequestBody CompanionService service) {
        // 获取当前登录用户信息
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 设置服务的陪玩ID为当前用户ID
        service.setCompanionId(currentUser.getId());
        
        Long serviceId = companionServiceService.createService(service);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", serviceId);
        response.put("message", "服务创建成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新陪玩服务
     */
    @PutMapping("/{serviceId}")
    public ResponseEntity<?> updateService(
            @PathVariable Long serviceId,
            @RequestBody CompanionService service) {
        
        // 获取当前登录用户信息
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 获取服务详情，确保是当前用户的服务
        CompanionServiceDTO existingService = companionServiceService.getServiceById(serviceId);
        if (existingService == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证服务所有者
        if (!existingService.getCompanionId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("无权修改此服务");
        }
        
        // 设置ID和陪玩ID
        service.setId(serviceId);
        service.setCompanionId(currentUser.getId());
        
        boolean success = companionServiceService.updateService(service);
        
        if (success) {
            return ResponseEntity.ok("服务更新成功");
        } else {
            return ResponseEntity.status(500).body("服务更新失败");
        }
    }
    
    /**
     * 获取当前陪玩用户的所有服务
     */
    @GetMapping("/my-services")
    public ResponseEntity<List<CompanionServiceDTO>> getMyServices() {
        // 获取当前登录用户信息
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<CompanionServiceDTO> services = companionServiceService.getServicesByCompanionId(currentUser.getId());
        
        return ResponseEntity.ok(services);
    }
    
    /**
     * 获取服务详情
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<CompanionServiceDTO> getServiceById(@PathVariable Long serviceId) {
        CompanionServiceDTO service = companionServiceService.getServiceById(serviceId);
        
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(service);
    }
    
    /**
     * 获取所有激活的服务（分页）
     */
    @GetMapping
    public ResponseEntity<Page<CompanionServiceDTO>> getActiveServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<CompanionServiceDTO> services = companionServiceService.getActiveServices(page, size);
        
        return ResponseEntity.ok(services);
    }
    
    /**
     * 公共API：获取所有激活的服务（分页）
     * 该接口不需要登录即可访问
     */
    @GetMapping("/public")
    public ResponseEntity<Page<CompanionServiceDTO>> getPublicActiveServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<CompanionServiceDTO> services = companionServiceService.getActiveServices(page, size);
        
        return ResponseEntity.ok(services);
    }
    
    /**
     * 更新服务状态
     */
    @PutMapping("/{serviceId}/status")
    public ResponseEntity<?> updateServiceStatus(
            @PathVariable Long serviceId,
            @RequestParam String status) {
        
        // 获取当前登录用户信息
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 获取服务详情，确保是当前用户的服务
        CompanionServiceDTO existingService = companionServiceService.getServiceById(serviceId);
        if (existingService == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证服务所有者
        if (!existingService.getCompanionId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("无权修改此服务状态");
        }
        
        boolean success = companionServiceService.updateServiceStatus(serviceId, status);
        
        if (success) {
            return ResponseEntity.ok("服务状态更新成功");
        } else {
            return ResponseEntity.status(500).body("服务状态更新失败");
        }
    }
    
    /**
     * 删除服务
     */
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable Long serviceId) {
        // 获取当前登录用户信息
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 获取服务详情，确保是当前用户的服务
        CompanionServiceDTO existingService = companionServiceService.getServiceById(serviceId);
        if (existingService == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证服务所有者
        if (!existingService.getCompanionId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("无权删除此服务");
        }
        
        boolean success = companionServiceService.deleteService(serviceId);
        
        if (success) {
            return ResponseEntity.ok("服务删除成功");
        } else {
            return ResponseEntity.status(500).body("服务删除失败");
        }
    }
} 