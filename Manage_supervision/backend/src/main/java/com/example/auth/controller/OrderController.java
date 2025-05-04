package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.CreateOrderRequest;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.entity.User;
import com.example.auth.model.enums.OrderStatus;
import com.example.auth.service.OrderService;
import com.example.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserContext userContext;
    
    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        // 获取当前用户
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 手动校验请求参数
        if (request.getServiceId() == null) {
            return ResponseEntity.badRequest().body("服务ID不能为空");
        }
        
        if (request.getHours() == null || request.getHours() < 1) {
            return ResponseEntity.badRequest().body("小时数必须大于0");
        }
        
        Long orderId = orderService.createOrder(request, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("message", "订单创建成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        OrderDTO order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证权限：只有订单相关的玩家、陪玩和管理员可以查看订单
        boolean isRelated = order.getPlayerId().equals(currentUser.getId()) || 
                           order.getCompanionId().equals(currentUser.getId()) ||
                           currentUser.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()));
        
        if (!isRelated) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(order);
    }
    
    /**
     * 获取我的订单列表（玩家视角）
     */
    @GetMapping("/my-player-orders")
    public ResponseEntity<Page<OrderDTO>> getMyPlayerOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<OrderDTO> orders = orderService.getPlayerOrders(currentUser.getId(), status, page, size);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 获取我的订单列表（陪玩视角）
     */
    @GetMapping("/my-companion-orders")
    public ResponseEntity<Page<OrderDTO>> getMyCompanionOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 验证是否为陪玩
        boolean isCompanion = currentUser.getRoles().stream()
                .anyMatch(r -> "SUPERVISOR".equals(r.getName()));
        
        if (!isCompanion) {
            return ResponseEntity.status(403).body(null);
        }
        
        Page<OrderDTO> orders = orderService.getCompanionOrders(currentUser.getId(), status, page, size);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * 接受订单（陪玩）
     */
    @PostMapping("/{orderId}/accept")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        // 验证是否为陪玩
        boolean isCompanion = currentUser.getRoles().stream()
                .anyMatch(r -> "SUPERVISOR".equals(r.getName()));
        
        if (!isCompanion) {
            return ResponseEntity.status(403).body("您不是陪玩，无法接受订单");
        }
        
        try {
            boolean success = orderService.acceptOrder(orderId, currentUser.getId());
            if (success) {
                return ResponseEntity.ok("订单接受成功");
            } else {
                return ResponseEntity.status(500).body("订单接受失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 完成订单
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        try {
            // 获取订单详情
            OrderDTO order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 验证权限：只有订单相关的玩家和陪玩可以完成订单
            boolean isRelated = order.getPlayerId().equals(currentUser.getId()) || 
                                order.getCompanionId().equals(currentUser.getId());
            
            if (!isRelated) {
                return ResponseEntity.status(403).body("您没有权限完成此订单");
            }
            
            boolean success = orderService.completeOrder(orderId, currentUser.getId());
            if (success) {
                return ResponseEntity.ok("订单完成成功");
            } else {
                return ResponseEntity.status(500).body("订单完成失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "取消订单") String reason) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        try {
            // 获取订单详情
            OrderDTO order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 验证权限：只有订单相关的玩家和陪玩可以取消订单
            boolean isRelated = order.getPlayerId().equals(currentUser.getId()) || 
                                order.getCompanionId().equals(currentUser.getId());
            
            if (!isRelated) {
                return ResponseEntity.status(403).body("您没有权限取消此订单");
            }
            
            boolean success = orderService.cancelOrder(orderId, currentUser.getId(), reason);
            if (success) {
                return ResponseEntity.ok("订单取消成功");
            } else {
                return ResponseEntity.status(500).body("订单取消失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long orderId) {
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        try {
            boolean success = orderService.payOrder(orderId, currentUser.getId());
            if (success) {
                return ResponseEntity.ok("订单支付成功");
            } else {
                return ResponseEntity.status(500).body("订单支付失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 申请退款
     */
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> requestRefund(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "申请退款") String reason) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        try {
            // 获取订单详情
            OrderDTO order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 验证权限：只有订单相关的玩家可以申请退款
            boolean isPlayer = order.getPlayerId().equals(currentUser.getId());
            
            if (!isPlayer) {
                return ResponseEntity.status(403).body("只有玩家才能申请退款");
            }
            
            boolean success = orderService.requestRefund(orderId, currentUser.getId(), reason);
            if (success) {
                return ResponseEntity.ok("退款申请已提交，等待陪玩审核");
            } else {
                return ResponseEntity.status(500).body("退款申请提交失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 处理退款申请
     */
    @PostMapping("/{orderId}/handle-refund")
    public ResponseEntity<?> handleRefundRequest(
            @PathVariable Long orderId,
            @RequestParam boolean approved,
            @RequestParam(required = false, defaultValue = "") String response) {
        
        User currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("未登录或登录已过期");
        }
        
        try {
            // 获取订单详情
            OrderDTO order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 验证权限：只有订单相关的陪玩可以处理退款申请
            boolean isCompanion = order.getCompanionId().equals(currentUser.getId());
            
            if (!isCompanion) {
                return ResponseEntity.status(403).body("只有陪玩才能处理退款申请");
            }
            
            boolean success = orderService.handleRefundRequest(orderId, currentUser.getId(), approved, response);
            if (success) {
                if (approved) {
                    return ResponseEntity.ok("已同意退款申请");
                } else {
                    return ResponseEntity.ok("已拒绝退款申请");
                }
            } else {
                return ResponseEntity.status(500).body("处理退款申请失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /**
     * 获取所有订单状态
     */
    @GetMapping("/status-options")
    public ResponseEntity<List<Map<String, String>>> getOrderStatusOptions() {
        List<Map<String, String>> options = Arrays.stream(OrderStatus.values())
                .map(status -> {
                    Map<String, String> option = new HashMap<>();
                    option.put("value", status.name());
                    option.put("label", status.getDescription());
                    return option;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(options);
    }
} 