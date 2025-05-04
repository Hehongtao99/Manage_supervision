package com.example.auth.service;

import com.example.auth.model.dto.OrderStatusHistoryDTO;

import java.util.List;

/**
 * 订单历史记录服务接口
 */
public interface OrderHistoryService {

    /**
     * 获取订单历史记录
     *
     * @param orderId 订单ID
     * @return 历史记录列表
     */
    List<OrderStatusHistoryDTO> getOrderHistory(Long orderId);
} 