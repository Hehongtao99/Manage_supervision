package com.example.auth.controller;

import com.example.auth.model.dto.OrderStatusHistoryDTO;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.User;
import com.example.auth.service.OrderHistoryService;
import com.example.auth.service.OrderService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单历史记录控制器
 */
@RestController
@RequestMapping("/api/order-history")
public class OrderHistoryController {

    @Autowired
    private OrderHistoryService orderHistoryService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserContext userContext;
    
    /**
     * 获取订单历史记录
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderStatusHistoryDTO>> getOrderHistory(@PathVariable Long orderId) {
        // 获取当前用户，这里不要直接返回401，而是先尝试获取订单信息
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 获取订单信息
        Order order = orderService.getOrderEntity(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证权限：只有订单相关的玩家、陪玩和管理员可以查看订单历史
        boolean isRelated = order.getPlayerId().equals(currentUser.getId()) || 
                           order.getCompanionId().equals(currentUser.getId()) ||
                           currentUser.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()));
        
        if (!isRelated) {
            return ResponseEntity.status(403).build();
        }
        
        List<OrderStatusHistoryDTO> historyList = orderHistoryService.getOrderHistory(orderId);
        return ResponseEntity.ok(historyList);
    }
} 