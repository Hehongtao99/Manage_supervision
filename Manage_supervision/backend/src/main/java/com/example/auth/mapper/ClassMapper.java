package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.ClassEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClassMapper extends BaseMapper<ClassEntity> {
    
    @Select("SELECT c.*, col.college_name, m.major_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.id " +
            "LEFT JOIN majors m ON c.major_id = m.id " +
            "WHERE c.status = 'active' " +
            "AND (#{className} IS NULL OR c.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "AND (#{collegeId} IS NULL OR c.college_id = #{collegeId}) " +
            "AND (#{majorId} IS NULL OR c.major_id = #{majorId}) " +
            "ORDER BY c.create_time DESC")
    Page<ClassEntity> selectClassPage(Page<ClassEntity> page, 
                                     @Param("className") String className, 
                                     @Param("collegeId") Long collegeId, 
                                     @Param("majorId") Long majorId);
} 