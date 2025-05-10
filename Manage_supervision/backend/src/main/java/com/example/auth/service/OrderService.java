package com.example.auth.service;

import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.entity.Order;
import com.example.auth.model.dto.PageResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param hours 购买小时数
     * @param price 课程单价
     * @param message 学生留言
     * @return 订单ID
     */
    Long createOrder(Long studentId, Long courseId, Integer hours, BigDecimal price, String message);
    
    /**
     * 根据ID获取订单
     * @param orderId 订单ID
     * @return 订单信息
     */
    OrderDTO getOrderById(Long orderId);
    
    /**
     * 获取学生的订单列表
     * @param studentId 学生ID
     * @return 订单列表
     */
    List<OrderDTO> getOrdersByStudent(Long studentId);
    
    /**
     * 获取教师的订单列表
     * @param teacherId 教师ID
     * @return 订单列表
     */
    List<OrderDTO> getOrdersByTeacher(Long teacherId);
    
    /**
     * 取消订单（学生操作）
     * @param orderId 订单ID
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, Long studentId);
    
    /**
     * 取消订单并添加退款理由（学生操作）
     * @param orderId 订单ID
     * @param studentId 学生ID
     * @param refundReason 退款理由
     * @return 是否成功
     */
    boolean cancelOrderWithReason(Long orderId, Long studentId, String refundReason);
    
    /**
     * 接受订单（教师操作）
     * @param orderId 订单ID
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean acceptOrder(Long orderId, Long teacherId);
    
    /**
     * 拒绝订单（教师操作）
     * @param orderId 订单ID
     * @param teacherId 教师ID
     * @param rejectReason 拒绝原因
     * @return 是否成功
     */
    boolean rejectOrder(Long orderId, Long teacherId, String rejectReason);
    
    /**
     * 完成订单
     * @param orderId 订单ID
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean completeOrder(Long orderId, Long teacherId);
    
    /**
     * 同意退款申请（教师操作）
     * @param orderId 订单ID
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean approveRefund(Long orderId, Long teacherId);
    
    /**
     * 拒绝退款申请（教师操作）
     * @param orderId 订单ID
     * @param teacherId 教师ID
     * @param rejectReason 拒绝理由
     * @return 是否成功
     */
    boolean rejectRefund(Long orderId, Long teacherId, String rejectReason);

    /**
     * 获取教师订单列表（分页）
     * @param teacherId 教师ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<OrderDTO> getTeacherOrders(Long teacherId, String status, Integer page, Integer size);

    /**
     * 获取学生订单列表（分页）
     * @param studentId 学生ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<OrderDTO> getStudentOrders(Long studentId, String status, Integer page, Integer size);
} 