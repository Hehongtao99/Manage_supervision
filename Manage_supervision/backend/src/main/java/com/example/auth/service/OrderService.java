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
     * 学生申诉退款申请（学生操作）
     * @param orderId 订单ID
     * @param studentId 学生ID
     * @param appealReason 申诉理由
     * @return 是否成功
     */
    boolean appealRefund(Long orderId, Long studentId, String appealReason);
    
    /**
     * 教师回复申诉（教师操作）
     * @param orderId 订单ID
     * @param teacherId 教师ID
     * @param teacherResponse 教师回复
     * @return 是否成功
     */
    boolean respondToAppeal(Long orderId, Long teacherId, String teacherResponse);
    
    /**
     * 管理员批准申诉并退款（管理员操作）
     * @param orderId 订单ID
     * @param adminId 管理员ID
     * @param adminDecision 管理员决定
     * @return 是否成功
     */
    boolean approveAppeal(Long orderId, Long adminId, String adminDecision);
    
    /**
     * 管理员拒绝申诉（管理员操作）
     * @param orderId 订单ID
     * @param adminId 管理员ID
     * @param adminDecision 管理员决定
     * @return 是否成功
     */
    boolean rejectAppeal(Long orderId, Long adminId, String adminDecision);
    
    /**
     * 获取管理员待处理申诉订单列表
     * @return 申诉订单列表
     */
    List<OrderDTO> getAppealingOrders();
    
    /**
     * 获取管理员待处理申诉订单列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @return 分页申诉订单列表
     */
    PageResult<OrderDTO> getAppealingOrders(Integer page, Integer size);

    /**
     * 获取管理员已处理申诉订单列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @return 分页已处理申诉订单列表
     */
    PageResult<OrderDTO> getProcessedAppeals(Integer page, Integer size);

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