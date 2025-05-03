package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScenicSpotMapper extends BaseMapper<ScenicSpot> {
    
    /**
     * 根据条件查询景区列表（带分页）
     */
    @Select("<script>" +
            "SELECT s.*, " +
            "r1.name as province_name, " +
            "r2.name as city_name, " +
            "r3.name as district_name " +
            "FROM scenic_spots s " +
            "LEFT JOIN regions r1 ON s.province_id = r1.id " +
            "LEFT JOIN regions r2 ON s.city_id = r2.id " +
            "LEFT JOIN regions r3 ON s.district_id = r3.id " +
            "<where> " +
            "<if test='name != null and name != \"\"'> " +
            "   AND s.name LIKE CONCAT('%', #{name}, '%') " +
            "</if> " +
            "<if test='provinceId != null'> " +
            "   AND s.province_id = #{provinceId} " +
            "</if> " +
            "<if test='cityId != null'> " +
            "   AND s.city_id = #{cityId} " +
            "</if> " +
            "<if test='districtId != null'> " +
            "   AND s.district_id = #{districtId} " +
            "</if> " +
            "<if test='level != null and level != \"\"'> " +
            "   AND s.level = #{level} " +
            "</if> " +
            "<if test='status != null and status != \"\"'> " +
            "   AND s.status = #{status} " +
            "</if> " +
            "</where> " +
            "ORDER BY s.sort ASC, s.id ASC" +
            "</script>")
    Page<ScenicSpot> findByConditions(Page<ScenicSpot> page, 
                                     @Param("name") String name,
                                     @Param("provinceId") Long provinceId, 
                                     @Param("cityId") Long cityId,
                                     @Param("districtId") Long districtId,
                                     @Param("level") String level,
                                     @Param("status") String status);
    
    /**
     * 获取景区详情（包含地区名称）
     */
    @Select("SELECT s.*, " +
            "r1.name as province_name, " +
            "r2.name as city_name, " +
            "r3.name as district_name " +
            "FROM scenic_spots s " +
            "LEFT JOIN regions r1 ON s.province_id = r1.id " +
            "LEFT JOIN regions r2 ON s.city_id = r2.id " +
            "LEFT JOIN regions r3 ON s.district_id = r3.id " +
            "WHERE s.id = #{id}")
    ScenicSpot findDetailById(@Param("id") Long id);
    
    /**
     * 按省份统计景区数量
     */
    @Select("SELECT COUNT(*) FROM scenic_spots WHERE province_id = #{provinceId}")
    Integer countByProvinceId(@Param("provinceId") Long provinceId);
    
    /**
     * 获取热门景区（按排序）
     */
    @Select("SELECT s.*, " +
            "r1.name as province_name, " +
            "r2.name as city_name, " +
            "r3.name as district_name " +
            "FROM scenic_spots s " +
            "LEFT JOIN regions r1 ON s.province_id = r1.id " +
            "LEFT JOIN regions r2 ON s.city_id = r2.id " +
            "LEFT JOIN regions r3 ON s.district_id = r3.id " +
            "WHERE s.status = 'active' " +
            "ORDER BY s.sort ASC, s.id ASC " +
            "LIMIT #{limit}")
    List<ScenicSpot> findHotSpots(@Param("limit") Integer limit);
} 