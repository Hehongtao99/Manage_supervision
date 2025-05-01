package com.example.auth.controller;

import com.example.auth.annotation.ApiDescription;
import com.example.auth.dto.ApiEndpointDTO;
import com.example.auth.service.ApiEndpointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/endpoints")
public class ApiEndpointController {
    
    @Autowired
    private ApiEndpointService apiEndpointService;
    
    /**
     * 获取所有API接口
     */
    @GetMapping
    @ApiDescription(value = "获取所有API接口列表", requiredRoles = "ADMIN,USER")
    public ResponseEntity<List<ApiEndpointDTO>> getAllApiEndpoints() {
        return ResponseEntity.ok(apiEndpointService.getAllApiEndpoints());
    }
    
    /**
     * 按控制器分组获取API接口
     */
    @GetMapping("/by-controller")
    @ApiDescription(value = "按控制器分组获取API接口", requiredRoles = "ADMIN,USER")
    public ResponseEntity<Map<String, List<ApiEndpointDTO>>> getApiEndpointsByController() {
        return ResponseEntity.ok(apiEndpointService.getApiEndpointsByController());
    }
    
    /**
     * 获取所有控制器名称
     */
    @GetMapping("/controllers")
    @ApiDescription(value = "获取所有控制器名称", requiredRoles = "ADMIN,USER")
    public ResponseEntity<List<String>> getAllControllerNames() {
        return ResponseEntity.ok(apiEndpointService.getAllControllerNames());
    }
    
    /**
     * 根据控制器名称获取API接口
     */
    @GetMapping("/by-controller/{controllerName}")
    @ApiDescription(value = "根据控制器名称获取API接口", requiredRoles = "ADMIN,USER")
    public ResponseEntity<List<ApiEndpointDTO>> getApiEndpointsByControllerName(
            @PathVariable String controllerName) {
        return ResponseEntity.ok(apiEndpointService.getApiEndpointsByControllerName(controllerName));
    }
    
    /**
     * 获取API访问统计信息
     */
    @GetMapping("/stats")
    @ApiDescription(value = "获取API访问统计信息", requiredRoles = "ADMIN,USER")
    public ResponseEntity<Map<String, Long>> getApiAccessStats() {
        return ResponseEntity.ok(apiEndpointService.getApiAccessStats());
    }
} 