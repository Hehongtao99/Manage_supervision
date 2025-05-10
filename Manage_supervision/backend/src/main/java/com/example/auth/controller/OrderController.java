package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.OrderDTO;
import com.example.auth.model.dto.PageResult;
import com.example.auth.service.OrderService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 学生创建订单
     */
    @PostMapping("/student/orders")
    @RequireRole("USER")
    public ResponseEntity<?> createOrder(
            @RequestHeader("Authorization") String auth,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "hours", required = false) Integer hours,
            @RequestParam(value = "price", required = false) BigDecimal price,
            @RequestParam(value = "message", required = false) String message) {
        
        try {
            // 参数验证
            if (courseId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "缺少必需参数: courseId"
                ));
            }
            
            if (hours == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "缺少必需参数: hours"
                ));
            }
            
            if (price == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "缺少必需参数: price"
                ));
            }
            
            String token = auth.replace("Bearer ", "");
            Long studentId = jwtUtil.getUserIdFromToken(token);
            logger.info("学生创建订单, 学生ID: {}, 课程ID: {}, 小时数: {}", studentId, courseId, hours);
            
            Long orderId = orderService.createOrder(studentId, courseId, hours, price, message);
            
            if (orderId != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "订单创建成功，等待教师确认");
                response.put("orderId", orderId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单创建失败，请检查输入信息"
                ));
            }
        } catch (Exception e) {
            logger.error("创建订单出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 学生创建订单 (JSON格式)
     */
    @PostMapping("/student/orders/json")
    @RequireRole("USER")
    public ResponseEntity<?> createOrderJson(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Object> orderData) {
        
        try {
            // 从请求体中提取参数
            Object courseIdObj = orderData.get("courseId");
            Object hoursObj = orderData.get("hours");
            Object priceObj = orderData.get("price");
            Object messageObj = orderData.get("message");
            
            // 转换参数
            Long courseId = null;
            Integer hours = null;
            BigDecimal price = null;
            String message = null;
            
            // 处理courseId
            if (courseIdObj != null) {
                if (courseIdObj instanceof Number) {
                    courseId = ((Number) courseIdObj).longValue();
                } else if (courseIdObj instanceof String) {
                    try {
                        courseId = Long.parseLong((String) courseIdObj);
                    } catch (NumberFormatException e) {
                        logger.error("无效的课程ID格式: {}", courseIdObj);
                    }
                }
            }
            
            // 处理hours
            if (hoursObj != null) {
                if (hoursObj instanceof Number) {
                    hours = ((Number) hoursObj).intValue();
                } else if (hoursObj instanceof String) {
                    try {
                        hours = Integer.parseInt((String) hoursObj);
                    } catch (NumberFormatException e) {
                        logger.error("无效的小时数格式: {}", hoursObj);
                    }
                }
            }
            
            // 处理price
            if (priceObj != null) {
                if (priceObj instanceof Number) {
                    price = new BigDecimal(priceObj.toString());
                } else if (priceObj instanceof String) {
                    try {
                        price = new BigDecimal((String) priceObj);
                    } catch (NumberFormatException e) {
                        logger.error("无效的价格格式: {}", priceObj);
                    }
                }
            }
            
            // 处理message
            if (messageObj != null && messageObj instanceof String) {
                message = (String) messageObj;
            }
            
            // 参数验证
            if (courseId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "缺少必需参数: courseId"
                ));
            }
            
            if (hours == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "缺少必需参数: hours"
                ));
            }
            
            if (price == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "缺少必需参数: price"
                ));
            }
            
            String token = auth.replace("Bearer ", "");
            Long studentId = jwtUtil.getUserIdFromToken(token);
            logger.info("学生创建订单(JSON), 学生ID: {}, 课程ID: {}, 小时数: {}", studentId, courseId, hours);
            
            Long orderId = orderService.createOrder(studentId, courseId, hours, price, message);
            
            if (orderId != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "订单创建成功，等待教师确认");
                response.put("orderId", orderId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单创建失败，请检查输入信息"
                ));
            }
        } catch (Exception e) {
            logger.error("创建订单出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取学生订单列表
     */
    @GetMapping("/student/orders")
    @RequireRole("USER")
    public ResponseEntity<List<OrderDTO>> getStudentOrders(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long studentId = jwtUtil.getUserIdFromToken(token);
            logger.info("获取学生订单列表, 学生ID: {}", studentId);
            
            List<OrderDTO> orders = orderService.getOrdersByStudent(studentId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取学生订单列表出错", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * 获取学生订单详情
     */
    @GetMapping("/student/orders/{orderId}")
    @RequireRole("USER")
    public ResponseEntity<?> getStudentOrderDetail(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long studentId = jwtUtil.getUserIdFromToken(token);
            logger.info("获取学生订单详情, 订单ID: {}, 学生ID: {}", orderId, studentId);
            
            OrderDTO order = orderService.getOrderById(orderId);
            
            if (order != null && order.getStudentId().equals(studentId)) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单不存在或无权查看"
                ));
            }
        } catch (Exception e) {
            logger.error("获取学生订单详情出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 学生取消订单
     */
    @PostMapping("/student/orders/{orderId}/cancel")
    @RequireRole("USER")
    public ResponseEntity<?> cancelOrder(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long studentId = jwtUtil.getUserIdFromToken(token);
            logger.info("学生取消订单, 订单ID: {}, 学生ID: {}", orderId, studentId);
            
            boolean success = orderService.cancelOrder(orderId, studentId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "订单取消成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单取消失败，可能是订单状态不允许取消或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("取消订单出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 学生申请退款，带退款理由
     */
    @PostMapping("/student/orders/{orderId}/refund")
    @RequireRole("USER")
    public ResponseEntity<?> refundOrder(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long studentId = jwtUtil.getUserIdFromToken(token);
            
            // 从请求体中获取退款理由
            String refundReason = requestBody.get("refundReason");
            if (refundReason == null || refundReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "退款理由不能为空"
                ));
            }
            
            logger.info("学生申请退款, 订单ID: {}, 学生ID: {}, 退款理由: {}", orderId, studentId, refundReason);
            
            boolean success = orderService.cancelOrderWithReason(orderId, studentId, refundReason);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "退款申请已提交"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "退款申请失败，可能是订单状态不允许退款或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("申请退款出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取教师订单列表
     */
    @GetMapping("/supervisor/orders")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<List<OrderDTO>> getTeacherOrders(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("获取教师订单列表, 教师ID: {}", teacherId);
            
            List<OrderDTO> orders = orderService.getOrdersByTeacher(teacherId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取教师订单列表出错", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * 获取教师订单详情
     */
    @GetMapping("/supervisor/orders/{orderId}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTeacherOrderDetail(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("获取教师订单详情, 订单ID: {}, 教师ID: {}", orderId, teacherId);
            
            OrderDTO order = orderService.getOrderById(orderId);
            
            if (order != null && order.getTeacherId().equals(teacherId)) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单不存在或无权查看"
                ));
            }
        } catch (Exception e) {
            logger.error("获取教师订单详情出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师接受订单
     */
    @PostMapping("/supervisor/orders/{orderId}/accept")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> acceptOrder(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师接受订单, 订单ID: {}, 教师ID: {}", orderId, teacherId);
            
            boolean success = orderService.acceptOrder(orderId, teacherId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "订单已接受"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单接受失败，可能是订单状态不允许接受或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("接受订单出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师拒绝订单
     */
    @PostMapping("/supervisor/orders/{orderId}/reject")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> rejectOrder(
            @PathVariable("orderId") Long orderId,
            @RequestParam("rejectReason") String rejectReason,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师拒绝订单, 订单ID: {}, 教师ID: {}, 拒绝原因: {}", orderId, teacherId, rejectReason);
            
            boolean success = orderService.rejectOrder(orderId, teacherId, rejectReason);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "订单已拒绝"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单拒绝失败，可能是订单状态不允许拒绝或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("拒绝订单出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师完成订单
     */
    @PostMapping("/supervisor/orders/{orderId}/complete")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> completeOrder(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师完成订单, 订单ID: {}, 教师ID: {}", orderId, teacherId);
            
            boolean success = orderService.completeOrder(orderId, teacherId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "订单已完成"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "订单完成失败，可能是订单状态不允许完成或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("完成订单出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师同意退款
     */
    @PostMapping("/supervisor/orders/{orderId}/approve-refund")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> approveRefund(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            logger.info("教师同意退款, 订单ID: {}, 教师ID: {}", orderId, teacherId);
            
            boolean success = orderService.approveRefund(orderId, teacherId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "已同意退款申请"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "同意退款失败，可能是订单状态不允许操作或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("同意退款出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师拒绝退款
     */
    @PostMapping("/supervisor/orders/{orderId}/reject-refund")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> rejectRefund(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            
            // 从请求体中获取拒绝理由
            String rejectReason = requestBody.get("rejectReason");
            logger.info("教师拒绝退款, 订单ID: {}, 教师ID: {}, 拒绝理由: {}", orderId, teacherId, rejectReason);
            
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "拒绝理由不能为空"
                ));
            }
            
            boolean success = orderService.rejectRefund(orderId, teacherId, rejectReason);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "已拒绝退款申请"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "拒绝退款失败，可能是订单状态不允许操作或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("拒绝退款出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }

    /**
     * 获取教师订单列表（分页）
     */
    @GetMapping("/teacher/page")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getTeacherOrdersPaged(
            @RequestHeader("Authorization") String auth,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Integer page, 
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        logger.info("获取教师订单列表（分页） - 状态: {}, 页码: {}, 大小: {}", status, page, size);
        
        // 获取当前登录的教师ID
        Long teacherId = jwtUtil.getUserIdFromToken(auth.replace("Bearer ", ""));
        
        try {
            PageResult<OrderDTO> orders = orderService.getTeacherOrders(teacherId, status, page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取教师订单列表出错", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "获取教师订单列表失败", "message", e.getMessage())
            );
        }
    }

    /**
     * 获取学生订单列表（分页）
     */
    @GetMapping("/student/page")
    @RequireRole("USER")
    public ResponseEntity<?> getStudentOrdersPaged(
            @RequestHeader("Authorization") String auth,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        logger.info("获取学生订单列表（分页） - 状态: {}, 页码: {}, 大小: {}", status, page, size);
        
        // 获取当前登录的学生ID
        Long studentId = jwtUtil.getUserIdFromToken(auth.replace("Bearer ", ""));
        
        try {
            PageResult<OrderDTO> orders = orderService.getStudentOrders(studentId, status, page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取学生订单列表出错", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "获取学生订单列表失败", "message", e.getMessage())
            );
        }
    }

    /**
     * 学生申诉退款
     */
    @PostMapping("/student/orders/{orderId}/appeal")
    @RequireRole("USER")
    public ResponseEntity<?> appealRefund(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long studentId = jwtUtil.getUserIdFromToken(token);
            
            // 从请求体中获取申诉理由
            String appealReason = requestBody.get("appealReason");
            if (appealReason == null || appealReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "申诉理由不能为空"
                ));
            }
            
            logger.info("学生申诉退款, 订单ID: {}, 学生ID: {}, 申诉理由: {}", orderId, studentId, appealReason);
            
            boolean success = orderService.appealRefund(orderId, studentId, appealReason);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "申诉已提交，等待教师回复和管理员处理"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "申诉失败，可能是订单状态不允许申诉或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("申诉退款出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 教师回复申诉
     */
    @PostMapping("/supervisor/orders/{orderId}/respond-appeal")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> respondToAppeal(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long teacherId = jwtUtil.getUserIdFromToken(token);
            
            // 从请求体中获取回复内容
            String teacherResponse = requestBody.get("teacherResponse");
            if (teacherResponse == null || teacherResponse.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "回复内容不能为空"
                ));
            }
            
            logger.info("教师回复申诉, 订单ID: {}, 教师ID: {}, 回复内容: {}", orderId, teacherId, teacherResponse);
            
            boolean success = orderService.respondToAppeal(orderId, teacherId, teacherResponse);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "已回复申诉，等待管理员处理"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "回复申诉失败，可能是订单状态不允许回复或订单不存在"
                ));
            }
        } catch (Exception e) {
            logger.error("回复申诉出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取申诉中的订单列表（管理员）
     */
    @GetMapping("/admin/orders/appeals")
    @RequireRole("ADMIN")
    public ResponseEntity<List<OrderDTO>> getAppealingOrders() {
        try {
            logger.info("获取申诉中的订单列表");
            
            List<OrderDTO> orders = orderService.getAppealingOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取申诉中的订单列表出错", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * 获取申诉中的订单列表（管理员，分页）
     */
    @GetMapping("/admin/orders/appeals/page")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getAppealingOrdersPaged(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        logger.info("获取申诉中订单列表（分页） - 页码: {}, 大小: {}", page, size);
        
        try {
            PageResult<OrderDTO> orders = orderService.getAppealingOrders(page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取申诉中订单列表出错", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "获取申诉中订单列表失败", "message", e.getMessage())
            );
        }
    }
    
    /**
     * 获取已处理的申诉订单列表（管理员，分页）
     */
    @GetMapping("/admin/orders/appeals/processed/page")
    @RequireRole("ADMIN")
    public ResponseEntity<?> getProcessedAppealsOrdersPaged(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        logger.info("获取已处理申诉订单列表（分页） - 页码: {}, 大小: {}", page, size);
        
        try {
            PageResult<OrderDTO> orders = orderService.getProcessedAppeals(page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("获取已处理申诉订单列表出错", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "获取已处理申诉订单列表失败", "message", e.getMessage())
            );
        }
    }
    
    /**
     * 管理员批准申诉
     */
    @PostMapping("/admin/orders/{orderId}/approve-appeal")
    @RequireRole("ADMIN")
    public ResponseEntity<?> approveAppeal(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long adminId = jwtUtil.getUserIdFromToken(token);
            
            // 从请求体中获取决定理由
            String adminDecision = requestBody.get("adminDecision");
            if (adminDecision == null || adminDecision.trim().isEmpty()) {
                adminDecision = "管理员同意退款";
            }
            
            logger.info("管理员批准申诉, 订单ID: {}, 管理员ID: {}, 决定理由: {}", orderId, adminId, adminDecision);
            
            boolean success = orderService.approveAppeal(orderId, adminId, adminDecision);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "申诉已批准，订单将退款"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "批准申诉失败，可能是订单状态不允许操作或教师尚未回复"
                ));
            }
        } catch (Exception e) {
            logger.error("批准申诉出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 管理员拒绝申诉
     */
    @PostMapping("/admin/orders/{orderId}/reject-appeal")
    @RequireRole("ADMIN")
    public ResponseEntity<?> rejectAppeal(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String auth) {
        
        try {
            String token = auth.replace("Bearer ", "");
            Long adminId = jwtUtil.getUserIdFromToken(token);
            
            // 从请求体中获取决定理由
            String adminDecision = requestBody.get("adminDecision");
            if (adminDecision == null || adminDecision.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "决定理由不能为空"
                ));
            }
            
            logger.info("管理员拒绝申诉, 订单ID: {}, 管理员ID: {}, 决定理由: {}", orderId, adminId, adminDecision);
            
            boolean success = orderService.rejectAppeal(orderId, adminId, adminDecision);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "申诉已拒绝，订单不予退款"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "拒绝申诉失败，可能是订单状态不允许操作或教师尚未回复"
                ));
            }
        } catch (Exception e) {
            logger.error("拒绝申诉出错", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
} 