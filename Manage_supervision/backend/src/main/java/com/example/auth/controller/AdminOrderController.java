package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.entity.User;
import com.example.auth.service.OrderService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员订单管理控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserContext userContext;
    
    /**
     * 获取所有订单（可按状态筛选）
     */
    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 这里需要在OrderService中实现getAllOrders方法
        // 暂时使用已有的方法
        Page<OrderDTO> orders = orderService.getAllOrders(page, size, status);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @RequireRole("ADMIN")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        OrderDTO order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(order);
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    @RequireRole("ADMIN")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> requestData) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        String reason = requestData.getOrDefault("reason", "管理员取消");
        
        boolean success = orderService.cancelOrder(orderId, currentUser.getId(), reason);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "订单取消成功");
        } else {
            response.put("success", false);
            response.put("message", "订单取消失败");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 完成订单
     */
    @PostMapping("/{orderId}/complete")
    @RequireRole("ADMIN")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        boolean success = orderService.completeOrder(orderId, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "订单完成成功");
        } else {
            response.put("success", false);
            response.put("message", "订单完成失败");
        }
        
        return ResponseEntity.ok(response);
    }
} 