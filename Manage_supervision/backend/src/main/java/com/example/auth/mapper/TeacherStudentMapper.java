package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.TeacherStudentRelation;
import com.example.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherStudentMapper extends BaseMapper<TeacherStudentRelation> {
    
    /**
     * 根据教师ID查询关系
     */
    @Select("SELECT * FROM teacher_student_relations WHERE teacher_id = #{teacherId}")
    List<TeacherStudentRelation> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据学生ID查询关系
     */
    @Select("SELECT * FROM teacher_student_relations WHERE student_id = #{studentId}")
    List<TeacherStudentRelation> findByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 检查教师和学生是否存在关系
     */
    @Select("SELECT COUNT(*) > 0 FROM teacher_student_relations " +
            "WHERE teacher_id = #{teacherId} AND student_id = #{studentId}")
    boolean existsByTeacherIdAndStudentId(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);
    
    /**
     * 根据教师ID和学生ID查找关系
     */
    @Select("SELECT * FROM teacher_student_relations " +
            "WHERE teacher_id = #{teacherId} AND student_id = #{studentId} LIMIT 1")
    TeacherStudentRelation findByTeacherIdAndStudentId(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);
    
    /**
     * 查询特定教师的所有有效学生ID
     */
    @Select("SELECT student_id FROM teacher_student_relations " +
            "WHERE teacher_id = #{teacherId} AND status = 'active'")
    List<Long> findActiveStudentIdsByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 查询特定学生的所有有效教师ID
     */
    @Select("SELECT teacher_id FROM teacher_student_relations " +
            "WHERE student_id = #{studentId} AND status = 'active'")
    List<Long> findActiveTeacherIdsByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询未分配老师的学生ID列表
     */
    @Select("SELECT u.id FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} " +
            "AND u.id NOT IN (SELECT student_id FROM teacher_student_relations WHERE status = 'active')")
    List<Long> findUnassignedStudentIdsByRoleId(@Param("roleId") Long roleId);
} 