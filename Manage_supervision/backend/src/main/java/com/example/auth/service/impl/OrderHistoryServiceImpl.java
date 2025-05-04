package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.OrderStatusHistoryMapper;
import com.example.auth.model.dto.OrderStatusHistoryDTO;
import com.example.auth.model.entity.OrderStatusHistory;
import com.example.auth.model.entity.User;
import com.example.auth.service.OrderHistoryService;
import com.example.auth.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单历史记录服务实现类
 */
@Service
public class OrderHistoryServiceImpl implements OrderHistoryService {

    @Autowired
    private OrderStatusHistoryMapper orderStatusHistoryMapper;
    
    @Autowired
    private UserService userService;
    
    @Override
    public List<OrderStatusHistoryDTO> getOrderHistory(Long orderId) {
        // 查询条件：按订单ID查询，并按时间降序排序
        LambdaQueryWrapper<OrderStatusHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderStatusHistory::getOrderId, orderId);
        queryWrapper.orderByDesc(OrderStatusHistory::getCreateTime);
        
        // 执行查询
        List<OrderStatusHistory> historyList = orderStatusHistoryMapper.selectList(queryWrapper);
        
        // 转换为DTO
        List<OrderStatusHistoryDTO> dtoList = new ArrayList<>();
        for (OrderStatusHistory history : historyList) {
            OrderStatusHistoryDTO dto = new OrderStatusHistoryDTO();
            BeanUtils.copyProperties(history, dto);
            
            // 获取操作者名称
            if (history.getOperatorId() != null) {
                User operator = userService.getUserById(history.getOperatorId());
                if (operator != null) {
                    dto.setOperatorName(operator.getUsername());
                }
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
} 