package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 根据教师ID查找课程
     */
    @Select("SELECT * FROM courses WHERE teacher_id = #{teacherId} AND status != 'deleted'")
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据教师ID分页查询课程
     */
    @Select("SELECT * FROM courses WHERE teacher_id = #{teacherId} AND status != 'deleted'")
    IPage<Course> findByTeacherIdPage(Page<Course> page, @Param("teacherId") Long teacherId);
    
    /**
     * 根据课程名称模糊查询
     */
    @Select("SELECT * FROM courses WHERE course_name LIKE CONCAT('%', #{courseName}, '%') AND status != 'deleted'")
    List<Course> findByCourseName(@Param("courseName") String courseName);
    
    /**
     * 根据类别查询课程
     */
    @Select("SELECT * FROM courses WHERE category = #{category} AND status != 'deleted'")
    List<Course> findByCategory(@Param("category") String category);
    
    /**
     * 根据多条件分页查询课程
     */
    @Select("<script>" +
            "SELECT * FROM courses WHERE status != 'deleted'" +
            "<if test='courseName != null and courseName != \"\"'>" +
            " AND course_name LIKE CONCAT('%', #{courseName}, '%')" +
            "</if>" +
            "<if test='teacherId != null'>" +
            " AND teacher_id = #{teacherId}" +
            "</if>" +
            "<if test='category != null and category != \"\"'>" +
            " AND category = #{category}" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            " AND status = #{status}" +
            "</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    IPage<Course> findByConditions(Page<Course> page, 
                                  @Param("courseName") String courseName, 
                                  @Param("teacherId") Long teacherId, 
                                  @Param("category") String category, 
                                  @Param("status") String status);
    
    /**
     * 统计所有课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE status != 'deleted'")
    Long countActiveCourses();
    
    /**
     * 统计某个教师的课程数量
     */
    @Select("SELECT COUNT(*) FROM courses WHERE teacher_id = #{teacherId} AND status != 'deleted'")
    Long countCoursesByTeacherId(@Param("teacherId") Long teacherId);
} 