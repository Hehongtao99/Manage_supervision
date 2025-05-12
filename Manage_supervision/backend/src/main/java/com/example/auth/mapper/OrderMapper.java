package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 查询学生已完成的订单，并关联课程应用信息
     * @param studentId 学生ID
     * @return 订单列表（包含课程标题）
     */
    @Select("SELECT o.*, ca.title as course_title, ca.subject as course_subject " +
            "FROM orders o " +
            "LEFT JOIN course_applications ca ON o.course_id = ca.id " +
            "WHERE o.student_id = #{studentId} AND o.status = 'COMPLETED'")
    List<Order> findCompletedOrdersByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询特定教师和学生的所有订单，并关联课程应用信息
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @return 订单列表（包含课程标题）
     */
    @Select("SELECT o.*, ca.title as course_title, ca.subject as course_subject " +
            "FROM orders o " +
            "LEFT JOIN course_applications ca ON o.course_id = ca.id " +
            "WHERE o.teacher_id = #{teacherId} AND o.student_id = #{studentId}")
    List<Order> findOrdersByTeacherAndStudentId(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);
} 