package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.auth.mapper.CourseApplicationMapper;
import com.example.auth.mapper.OrderMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.entity.CourseApplication;
import com.example.auth.model.entity.Order;
import com.example.auth.model.entity.User;
import com.example.auth.service.OrderService;
import com.example.auth.model.dto.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CourseApplicationMapper courseApplicationMapper;
    
    @Override
    @Transactional
    public Long createOrder(Long studentId, Long courseId, Integer hours, BigDecimal price, String message) {
        logger.info("创建订单: 学生ID={}, 课程ID={}, 小时数={}, 单价={}", studentId, courseId, hours, price);
        
        // 获取课程信息
        CourseApplication course = courseApplicationMapper.selectById(courseId);
        if (course == null) {
            logger.error("课程不存在: {}", courseId);
            return null;
        }
        
        // 获取教师ID
        Long teacherId = course.getTeacherId();
        
        // 生成订单编号 (格式: yyyyMMddHHmmss + 学生ID后四位)
        String studentIdStr = String.format("%04d", studentId % 10000);
        String orderNumber = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + studentIdStr;
        
        // 计算总金额
        BigDecimal totalAmount = price.multiply(new BigDecimal(hours));
        
        // 创建订单对象
        Order order = new Order();
        order.setStudentId(studentId);
        order.setTeacherId(teacherId);
        order.setCourseId(courseId);
        order.setOrderNumber(orderNumber);
        order.setPrice(price);
        order.setHours(hours);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setMessage(message);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 保存订单
        int rows = orderMapper.insert(order);
        if (rows > 0) {
            logger.info("订单创建成功: ID={}, 订单号={}", order.getId(), orderNumber);
            return order.getId();
        } else {
            logger.error("订单创建失败");
            return null;
        }
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
    public List<OrderDTO> getOrdersByStudent(Long studentId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStudentId, studentId)
               .orderByDesc(Order::getCreateTime);
        
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<OrderDTO> getOrdersByTeacher(Long teacherId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getTeacherId, teacherId)
               .orderByDesc(Order::getCreateTime);
        
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long studentId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getStudentId().equals(studentId)) {
            return false;
        }
        
        // 只有待确认或已接受的订单可以取消
        if (!"PENDING".equals(order.getStatus()) && !"ACCEPTED".equals(order.getStatus())) {
            return false;
        }
        
        order.setStatus("CANCELED");
        order.setUpdateTime(LocalDateTime.now());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean cancelOrderWithReason(Long orderId, Long studentId, String refundReason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getStudentId().equals(studentId)) {
            return false;
        }
        
        // 只有待确认或已接受的订单可以申请退款
        if (!"PENDING".equals(order.getStatus()) && !"ACCEPTED".equals(order.getStatus())) {
            return false;
        }
        
        order.setStatus("REFUND_PENDING"); // 设置为退款申请中状态
        order.setRefundReason(refundReason); // 保存退款理由
        order.setUpdateTime(LocalDateTime.now());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean acceptOrder(Long orderId, Long teacherId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getTeacherId().equals(teacherId)) {
            return false;
        }
        
        // 只有待确认的订单可以接受
        if (!"PENDING".equals(order.getStatus())) {
            return false;
        }
        
        order.setStatus("ACCEPTED");
        order.setUpdateTime(LocalDateTime.now());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean rejectOrder(Long orderId, Long teacherId, String rejectReason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getTeacherId().equals(teacherId)) {
            return false;
        }
        
        // 只有待确认的订单可以拒绝
        if (!"PENDING".equals(order.getStatus())) {
            return false;
        }
        
        order.setStatus("REJECTED");
        order.setRejectReason(rejectReason);
        order.setUpdateTime(LocalDateTime.now());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean completeOrder(Long orderId, Long teacherId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getTeacherId().equals(teacherId)) {
            return false;
        }
        
        // 只有已接受的订单可以完成
        if (!"ACCEPTED".equals(order.getStatus())) {
            return false;
        }
        
        order.setStatus("COMPLETED");
        order.setUpdateTime(LocalDateTime.now());
        
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean approveRefund(Long orderId, Long teacherId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getTeacherId().equals(teacherId)) {
            logger.error("同意退款失败: 订单不存在或教师无权操作");
            return false;
        }
        
        // 只有处于退款申请中的订单才能同意退款
        if (!"REFUND_PENDING".equals(order.getStatus())) {
            logger.error("同意退款失败: 订单状态不是退款申请中");
            return false;
        }
        
        // 更新订单状态为已取消（已退款）
        order.setStatus("CANCELED");
        order.setUpdateTime(LocalDateTime.now());
        
        logger.info("教师ID={}同意了订单ID={}的退款申请", teacherId, orderId);
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean rejectRefund(Long orderId, Long teacherId, String rejectReason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getTeacherId().equals(teacherId)) {
            logger.error("拒绝退款失败: 订单不存在或教师无权操作");
            return false;
        }
        
        // 只有处于退款申请中的订单才能拒绝退款
        if (!"REFUND_PENDING".equals(order.getStatus())) {
            logger.error("拒绝退款失败: 订单状态不是退款申请中");
            return false;
        }
        
        // 验证拒绝理由不为空
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            logger.error("拒绝退款失败: 拒绝理由不能为空");
            return false;
        }
        
        // 更新订单状态为退款已拒绝
        order.setStatus("REFUND_REJECTED");
        order.setRejectReason(rejectReason);
        order.setUpdateTime(LocalDateTime.now());
        
        logger.info("教师ID={}拒绝了订单ID={}的退款申请, 理由: {}", teacherId, orderId, rejectReason);
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean appealRefund(Long orderId, Long studentId, String appealReason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getStudentId().equals(studentId)) {
            logger.error("申诉退款失败: 订单不存在或学生无权操作");
            return false;
        }
        
        // 只有退款被拒绝的订单才能申诉
        if (!"REFUND_REJECTED".equals(order.getStatus())) {
            logger.error("申诉退款失败: 订单状态不是退款已拒绝");
            return false;
        }
        
        // 验证申诉理由不为空
        if (appealReason == null || appealReason.trim().isEmpty()) {
            logger.error("申诉退款失败: 申诉理由不能为空");
            return false;
        }
        
        // 更新订单状态为申诉中
        order.setStatus("APPEALING");
        order.setAppealReason(appealReason);
        order.setAppealTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        logger.info("学生ID={}对订单ID={}提出了退款申诉, 理由: {}", studentId, orderId, appealReason);
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean respondToAppeal(Long orderId, Long teacherId, String teacherResponse) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getTeacherId().equals(teacherId)) {
            logger.error("回复申诉失败: 订单不存在或教师无权操作");
            return false;
        }
        
        // 只有处于申诉中的订单才能回复
        if (!"APPEALING".equals(order.getStatus())) {
            logger.error("回复申诉失败: 订单状态不是申诉中");
            return false;
        }
        
        // 验证回复不为空
        if (teacherResponse == null || teacherResponse.trim().isEmpty()) {
            logger.error("回复申诉失败: 回复内容不能为空");
            return false;
        }
        
        // 更新教师回复
        order.setTeacherResponse(teacherResponse);
        order.setUpdateTime(LocalDateTime.now());
        
        logger.info("教师ID={}回复了订单ID={}的申诉, 回复: {}", teacherId, orderId, teacherResponse);
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean approveAppeal(Long orderId, Long adminId, String adminDecision) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            logger.error("批准申诉失败: 订单不存在");
            return false;
        }
        
        // 验证是否有教师回复
        if (order.getTeacherResponse() == null || order.getTeacherResponse().trim().isEmpty()) {
            logger.error("批准申诉失败: 教师尚未回复申诉");
            return false;
        }
        
        // 只有处于申诉中的订单才能批准
        if (!"APPEALING".equals(order.getStatus())) {
            logger.error("批准申诉失败: 订单状态不是申诉中");
            return false;
        }
        
        // 更新订单状态为申诉批准
        order.setStatus("APPEAL_APPROVED");
        order.setAdminDecision(adminDecision);
        order.setUpdateTime(LocalDateTime.now());
        
        logger.info("管理员ID={}批准了订单ID={}的申诉, 决定: {}", adminId, orderId, adminDecision);
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    @Transactional
    public boolean rejectAppeal(Long orderId, Long adminId, String adminDecision) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            logger.error("拒绝申诉失败: 订单不存在");
            return false;
        }
        
        // 验证是否有教师回复
        if (order.getTeacherResponse() == null || order.getTeacherResponse().trim().isEmpty()) {
            logger.error("拒绝申诉失败: 教师尚未回复申诉");
            return false;
        }
        
        // 只有处于申诉中的订单才能拒绝
        if (!"APPEALING".equals(order.getStatus())) {
            logger.error("拒绝申诉失败: 订单状态不是申诉中");
            return false;
        }
        
        // 更新订单状态为申诉拒绝
        order.setStatus("APPEAL_REJECTED");
        order.setAdminDecision(adminDecision);
        order.setUpdateTime(LocalDateTime.now());
        
        logger.info("管理员ID={}拒绝了订单ID={}的申诉, 决定: {}", adminId, orderId, adminDecision);
        return orderMapper.updateById(order) > 0;
    }
    
    @Override
    public List<OrderDTO> getAppealingOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "APPEALING")
               .orderByDesc(Order::getAppealTime);
        
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public PageResult<OrderDTO> getAppealingOrders(Integer page, Integer size) {
        logger.info("获取申诉中订单列表（分页） - 页码: {}, 大小: {}", page, size);
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, "APPEALING")
                   .orderByDesc(Order::getAppealTime);
            
            // 创建分页对象
            Page<Order> pageParam = new Page<>(page, size);
            // 执行分页查询
            Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
            
            // 将Order转换为OrderDTO
            List<OrderDTO> orderDTOs = orderPage.getRecords().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // 构建分页结果
            PageResult<OrderDTO> pageResult = new PageResult<>();
            pageResult.setRecords(orderDTOs);
            pageResult.setTotal(orderPage.getTotal());
            pageResult.setSize(orderPage.getSize());
            pageResult.setCurrent(orderPage.getCurrent());
            pageResult.setPages(orderPage.getPages());
            
            return pageResult;
        } catch (Exception e) {
            logger.error("获取申诉中订单列表出错", e);
            return new PageResult<>();
        }
    }
    
    @Override
    public PageResult<OrderDTO> getProcessedAppeals(Integer page, Integer size) {
        logger.info("获取已处理申诉订单列表（分页） - 页码: {}, 大小: {}", page, size);
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Order::getStatus, "APPEAL_APPROVED", "APPEAL_REJECTED")
                   .orderByDesc(Order::getUpdateTime);
            
            // 创建分页对象
            Page<Order> pageParam = new Page<>(page, size);
            // 执行分页查询
            Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
            
            // 将Order转换为OrderDTO
            List<OrderDTO> orderDTOs = orderPage.getRecords().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // 构建分页结果
            PageResult<OrderDTO> pageResult = new PageResult<>();
            pageResult.setRecords(orderDTOs);
            pageResult.setTotal(orderPage.getTotal());
            pageResult.setSize(orderPage.getSize());
            pageResult.setCurrent(orderPage.getCurrent());
            pageResult.setPages(orderPage.getPages());
            
            return pageResult;
        } catch (Exception e) {
            logger.error("获取已处理申诉订单列表出错", e);
            return new PageResult<>();
        }
    }
    
    @Override
    public PageResult<OrderDTO> getTeacherOrders(Long teacherId, String status, Integer page, Integer size) {
        logger.info("获取教师订单列表 - 教师ID: {}, 状态: {}, 页码: {}, 大小: {}", teacherId, status, page, size);
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getTeacherId, teacherId);
            
            if (status != null && !status.isEmpty()) {
                wrapper.eq(Order::getStatus, status);
            }
            
            wrapper.orderByDesc(Order::getCreateTime);
            
            // 创建分页对象
            Page<Order> pageParam = new Page<>(page, size);
            // 执行分页查询
            Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
            
            // 将Order转换为OrderDTO
            List<OrderDTO> orderDTOs = orderPage.getRecords().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // 构建分页结果
            PageResult<OrderDTO> pageResult = new PageResult<>();
            pageResult.setRecords(orderDTOs);
            pageResult.setTotal(orderPage.getTotal());
            pageResult.setSize(orderPage.getSize());
            pageResult.setCurrent(orderPage.getCurrent());
            pageResult.setPages(orderPage.getPages());
            
            return pageResult;
        } catch (Exception e) {
            logger.error("获取教师订单列表出错", e);
            return new PageResult<>();
        }
    }
    
    @Override
    public PageResult<OrderDTO> getStudentOrders(Long studentId, String status, Integer page, Integer size) {
        logger.info("获取学生订单列表 - 学生ID: {}, 状态: {}, 页码: {}, 大小: {}", studentId, status, page, size);
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStudentId, studentId);
            
            if (status != null && !status.isEmpty()) {
                wrapper.eq(Order::getStatus, status);
            }
            
            wrapper.orderByDesc(Order::getCreateTime);
            
            // 创建分页对象
            Page<Order> pageParam = new Page<>(page, size);
            // 执行分页查询
            Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
            
            // 将Order转换为OrderDTO
            List<OrderDTO> orderDTOs = orderPage.getRecords().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // 构建分页结果
            PageResult<OrderDTO> pageResult = new PageResult<>();
            pageResult.setRecords(orderDTOs);
            pageResult.setTotal(orderPage.getTotal());
            pageResult.setSize(orderPage.getSize());
            pageResult.setCurrent(orderPage.getCurrent());
            pageResult.setPages(orderPage.getPages());
            
            return pageResult;
        } catch (Exception e) {
            logger.error("获取学生订单列表出错", e);
            return new PageResult<>();
        }
    }
    
    /**
     * 将订单实体转换为DTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        // 获取学生信息
        User student = userMapper.selectById(order.getStudentId());
        if (student != null) {
            dto.setStudentName(student.getRealName() != null ? student.getRealName() : student.getUsername());
        }
        
        // 获取教师信息
        User teacher = userMapper.selectById(order.getTeacherId());
        if (teacher != null) {
            dto.setTeacherName(teacher.getRealName() != null ? teacher.getRealName() : teacher.getUsername());
        }
        
        // 获取课程信息
        CourseApplication course = courseApplicationMapper.selectById(order.getCourseId());
        if (course != null) {
            dto.setCourseTitle(course.getTitle());
            dto.setCourseSubject(course.getSubject());
        }
        
        return dto;
    }
} 