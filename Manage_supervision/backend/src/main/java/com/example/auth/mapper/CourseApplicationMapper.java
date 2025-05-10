package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.CourseApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程申请Mapper接口
 */
@Mapper
public interface CourseApplicationMapper extends BaseMapper<CourseApplication> {
    
    /**
     * 获取课程申请，同时关联教师姓名
     */
    @Select("SELECT ca.*, COALESCE(u.real_name, u.nickname, u.username) as teacher_name FROM course_applications ca " +
            "LEFT JOIN users u ON ca.teacher_id = u.id " +
            "WHERE ca.id = #{id}")
    CourseApplication getApplicationWithTeacherName(@Param("id") Long id);
    
    /**
     * 获取指定教师的所有课程申请
     */
    @Select("SELECT ca.*, COALESCE(u.real_name, u.nickname, u.username) as teacher_name FROM course_applications ca " +
            "LEFT JOIN users u ON ca.teacher_id = u.id " +
            "WHERE ca.teacher_id = #{teacherId} " +
            "ORDER BY ca.create_time DESC")
    List<CourseApplication> getApplicationsByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 获取所有课程申请，带教师姓名
     */
    @Select("SELECT ca.*, COALESCE(u.real_name, u.nickname, u.username) as teacher_name FROM course_applications ca " +
            "LEFT JOIN users u ON ca.teacher_id = u.id " +
            "ORDER BY ca.create_time DESC")
    List<CourseApplication> getAllApplicationsWithTeacherName();
    
    /**
     * 获取所有已审核通过的课程申请，带教师姓名
     */
    @Select("SELECT ca.*, COALESCE(u.real_name, u.nickname, u.username) as teacher_name FROM course_applications ca " +
            "LEFT JOIN users u ON ca.teacher_id = u.id " +
            "WHERE ca.status = 'APPROVED' " +
            "ORDER BY ca.create_time DESC")
    List<CourseApplication> getAllApprovedApplicationsWithTeacherName();
} 