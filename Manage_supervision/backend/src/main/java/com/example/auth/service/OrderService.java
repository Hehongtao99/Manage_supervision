package com.example.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.CreateOrderRequest;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.entity.Order;

public interface OrderService {
    
    // 创建订单
    Long createOrder(CreateOrderRequest request, Long playerId);
    
    // 根据ID获取订单详情
    OrderDTO getOrderById(Long orderId);
    
    // 根据ID获取订单实体
    Order getOrderEntity(Long orderId);
    
    // 获取玩家的订单列表
    Page<OrderDTO> getPlayerOrders(Long playerId, String status, int page, int size);
    
    // 获取陪玩的订单列表
    Page<OrderDTO> getCompanionOrders(Long companionId, String status, int page, int size);
    
    // 接受订单
    boolean acceptOrder(Long orderId, Long companionId);
    
    // 完成订单
    boolean completeOrder(Long orderId, Long operatorId);
    
    // 取消订单
    boolean cancelOrder(Long orderId, Long operatorId, String reason);
    
    // 支付订单
    boolean payOrder(Long orderId, Long playerId);
    
    // 申请退款订单（玩家）
    boolean requestRefund(Long orderId, Long playerId, String reason);
    
    // 处理退款申请（陪玩）
    boolean handleRefundRequest(Long orderId, Long companionId, boolean approved, String response);
    
    // 退款订单（已废弃，将被替换为requestRefund）
    @Deprecated
    boolean refundOrder(Long orderId, Long operatorId, String reason);
    
    // 生成订单号
    String generateOrderNumber();
    
    // 管理员获取所有订单（可按状态筛选）
    Page<OrderDTO> getAllOrders(int page, int size, String status);
} 