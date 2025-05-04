package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.mapper.OrderStatusHistoryMapper;
import com.example.auth.model.dto.CompanionServiceDTO;
import com.example.auth.model.dto.CreateOrderRequest;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.OrderStatusHistory;
import com.example.auth.model.entity.User;
import com.example.auth.model.enums.OperatorType;
import com.example.auth.model.enums.OrderStatus;
import com.example.auth.model.enums.PaymentStatus;
import com.example.auth.service.CompanionServiceService;
import com.example.auth.service.OrderService;
import com.example.auth.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusHistoryMapper statusHistoryMapper;

    @Autowired
    private CompanionServiceService companionServiceService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Long createOrder(CreateOrderRequest request, Long playerId) {
        // 获取服务信息
        CompanionServiceDTO serviceDTO = companionServiceService.getServiceById(request.getServiceId());
        if (serviceDTO == null) {
            throw new RuntimeException("服务不存在或已下架");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setPlayerId(playerId);
        order.setCompanionId(serviceDTO.getCompanionId());
        order.setServiceId(serviceDTO.getId());
        order.setGameType(serviceDTO.getGameTypes());
        order.setHours(request.getHours());
        order.setPrice(serviceDTO.getPrice());
        order.setTotalAmount(serviceDTO.getPrice().multiply(new BigDecimal(request.getHours())));
        order.setAppointedTime(request.getAppointedTime());
        order.setStatus(OrderStatus.PENDING.name());
        order.setPaymentStatus(PaymentStatus.UNPAID.name());
        order.setCreateTime(LocalDateTime.now());
        order.setRemark(request.getRemark());

        // 保存订单
        orderMapper.insert(order);

        // 添加订单状态历史
        addOrderStatusHistory(order.getId(), OrderStatus.PENDING.name(), playerId, OperatorType.PLAYER.name(), "创建订单");

        return order.getId();
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        
        return convertToDTO(order);
    }

    @Override
    public Order getOrderEntity(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public Page<OrderDTO> getPlayerOrders(Long playerId, String status, int page, int size) {
        // 创建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getPlayerId, playerId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        // 执行分页查询
        Page<Order> orderPage = new Page<>(page, size);
        Page<Order> resultPage = orderMapper.selectPage(orderPage, queryWrapper);
        
        // 转换结果为DTO
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : resultPage.getRecords()) {
            orderDTOList.add(convertToDTO(order));
        }
        
        // 创建返回结果
        Page<OrderDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        dtoPage.setRecords(orderDTOList);
        
        return dtoPage;
    }

    @Override
    public Page<OrderDTO> getCompanionOrders(Long companionId, String status, int page, int size) {
        // 创建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getCompanionId, companionId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        // 执行分页查询
        Page<Order> orderPage = new Page<>(page, size);
        Page<Order> resultPage = orderMapper.selectPage(orderPage, queryWrapper);
        
        // 转换结果为DTO
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : resultPage.getRecords()) {
            orderDTOList.add(convertToDTO(order));
        }
        
        // 创建返回结果
        Page<OrderDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        dtoPage.setRecords(orderDTOList);
        
        return dtoPage;
    }

    @Override
    @Transactional
    public boolean acceptOrder(Long orderId, Long companionId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证陪玩身份
        if (!order.getCompanionId().equals(companionId)) {
            throw new RuntimeException("您不是该订单的陪玩");
        }

        // 验证订单状态
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法接受");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.ACCEPTED.name());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 添加订单状态历史
        addOrderStatusHistory(orderId, OrderStatus.ACCEPTED.name(), companionId, OperatorType.COMPANION.name(), "接受订单");

        return true;
    }

    @Override
    @Transactional
    public boolean completeOrder(Long orderId, Long operatorId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态
        if (!OrderStatus.ACCEPTED.name().equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法完成");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 添加订单状态历史
        String operatorType = order.getCompanionId().equals(operatorId) ? 
                OperatorType.COMPANION.name() : OperatorType.PLAYER.name();
        addOrderStatusHistory(orderId, OrderStatus.COMPLETED.name(), operatorId, operatorType, "完成订单");

        return true;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long operatorId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态，只有待接单或已接单的订单可以取消
        if (!OrderStatus.PENDING.name().equals(order.getStatus()) && 
            !OrderStatus.ACCEPTED.name().equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法取消");
        }

        // 验证操作人身份
        boolean isPlayer = order.getPlayerId().equals(operatorId);
        boolean isCompanion = order.getCompanionId().equals(operatorId);
        if (!isPlayer && !isCompanion) {
            throw new RuntimeException("您没有权限取消此订单");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 添加订单状态历史
        String operatorType = isPlayer ? OperatorType.PLAYER.name() : OperatorType.COMPANION.name();
        addOrderStatusHistory(orderId, OrderStatus.CANCELLED.name(), operatorId, operatorType, reason);

        return true;
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId, Long playerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证玩家身份
        if (!order.getPlayerId().equals(playerId)) {
            throw new RuntimeException("您不是该订单的玩家");
        }

        // 验证订单状态
        if (!PaymentStatus.UNPAID.name().equals(order.getPaymentStatus())) {
            throw new RuntimeException("订单已支付或已退款");
        }

        // 更新订单支付状态
        order.setPaymentStatus(PaymentStatus.PAID.name());
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 添加订单状态历史
        addOrderStatusHistory(orderId, order.getStatus(), playerId, OperatorType.PLAYER.name(), "支付订单");

        return true;
    }

    @Override
    @Transactional
    @Deprecated
    public boolean refundOrder(Long orderId, Long operatorId, String reason) {
        // 该方法已废弃，请使用requestRefund替代
        // 为了向后兼容，调用新方法
        return requestRefund(orderId, operatorId, reason);
    }
    
    @Override
    @Transactional
    public boolean requestRefund(Long orderId, Long playerId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单支付状态
        if (!PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
            throw new RuntimeException("订单未支付或已退款");
        }
        
        // 验证订单状态
        if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new RuntimeException("只有已完成的订单才能申请退款");
        }
        
        // 验证是否为订单的玩家
        if (!order.getPlayerId().equals(playerId)) {
            throw new RuntimeException("只有订单的玩家才能申请退款");
        }

        // 更新订单状态为退款待审核
        order.setStatus(OrderStatus.REFUND_PENDING.name());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 添加订单状态历史
        addOrderStatusHistory(orderId, OrderStatus.REFUND_PENDING.name(), playerId, OperatorType.PLAYER.name(), reason);

        return true;
    }
    
    @Override
    @Transactional
    public boolean handleRefundRequest(Long orderId, Long companionId, boolean approved, String response) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态
        if (!OrderStatus.REFUND_PENDING.name().equals(order.getStatus())) {
            throw new RuntimeException("该订单不在退款审核状态");
        }
        
        // 验证是否为订单的陪玩
        if (!order.getCompanionId().equals(companionId)) {
            throw new RuntimeException("只有订单的陪玩才能处理退款申请");
        }

        if (approved) {
            // 同意退款
            order.setStatus(OrderStatus.REFUNDED.name());
            order.setPaymentStatus(PaymentStatus.REFUNDED.name());
            addOrderStatusHistory(orderId, OrderStatus.REFUNDED.name(), companionId, OperatorType.COMPANION.name(), "同意退款：" + response);
        } else {
            // 拒绝退款，恢复订单状态为已完成
            order.setStatus(OrderStatus.COMPLETED.name());
            addOrderStatusHistory(orderId, OrderStatus.COMPLETED.name(), companionId, OperatorType.COMPANION.name(), "拒绝退款：" + response);
        }
        
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return true;
    }

    @Override
    public String generateOrderNumber() {
        // 生成订单编号: 年月日时分秒+6位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timeStr = LocalDateTime.now().format(formatter);
        String randomStr = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        return timeStr + randomStr;
    }

    // 添加订单状态历史记录
    private void addOrderStatusHistory(Long orderId, String status, Long operatorId, String operatorType, String remark) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(orderId);
        history.setStatus(status);
        history.setOperatorId(operatorId);
        history.setOperatorType(operatorType);
        history.setCreateTime(LocalDateTime.now());
        history.setRemark(remark);
        statusHistoryMapper.insert(history);
    }

    // 将Order实体转换为OrderDTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        // 获取玩家信息
        User player = userService.getUserById(order.getPlayerId());
        if (player != null) {
            dto.setPlayerName(player.getUsername());
        }
        
        // 获取陪玩信息
        User companion = userService.getUserById(order.getCompanionId());
        if (companion != null) {
            dto.setCompanionName(companion.getUsername());
        }
        
        // 获取服务信息
        CompanionServiceDTO service = companionServiceService.getServiceById(order.getServiceId());
        if (service != null) {
            dto.setServiceTitle(service.getTitle());
        }
        
        return dto;
    }

    @Override
    public Page<OrderDTO> getAllOrders(int page, int size, String status) {
        // 创建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        // 执行分页查询
        Page<Order> orderPage = new Page<>(page, size);
        Page<Order> resultPage = orderMapper.selectPage(orderPage, queryWrapper);
        
        // 转换结果为DTO
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : resultPage.getRecords()) {
            orderDTOList.add(convertToDTO(order));
        }
        
        // 创建返回结果
        Page<OrderDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        dtoPage.setRecords(orderDTOList);
        
        return dtoPage;
    }
} 