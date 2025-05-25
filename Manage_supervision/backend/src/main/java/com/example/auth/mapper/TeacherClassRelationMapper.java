package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.TeacherClassRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherClassRelationMapper extends BaseMapper<TeacherClassRelation> {
    
    @Select("SELECT tcr.*, u.real_name as teacher_name, c.class_name, col.college_name, m.major_name " +
            "FROM teacher_class_relations tcr " +
            "LEFT JOIN users u ON tcr.teacher_id = u.id " +
            "LEFT JOIN classes c ON tcr.class_id = c.id " +
            "LEFT JOIN colleges col ON c.college_id = col.id " +
            "LEFT JOIN majors m ON c.major_id = m.id " +
            "WHERE tcr.status = 'active' " +
            "AND (#{teacherId} IS NULL OR tcr.teacher_id = #{teacherId}) " +
            "AND (#{classId} IS NULL OR tcr.class_id = #{classId}) " +
            "ORDER BY tcr.assign_time DESC")
    Page<TeacherClassRelation> selectTeacherClassPage(Page<TeacherClassRelation> page, 
                                                     @Param("teacherId") Long teacherId, 
                                                     @Param("classId") Long classId);
} 