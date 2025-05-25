package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {
    
    @Select("SELECT m.*, c.college_name " +
            "FROM majors m " +
            "LEFT JOIN colleges c ON m.college_id = c.id " +
            "WHERE m.status = 'active' " +
            "AND (#{majorName} IS NULL OR m.major_name LIKE CONCAT('%', #{majorName}, '%')) " +
            "AND (#{collegeId} IS NULL OR m.college_id = #{collegeId}) " +
            "ORDER BY m.create_time DESC")
    Page<Major> selectMajorPage(Page<Major> page, @Param("majorName") String majorName, @Param("collegeId") Long collegeId);
} 