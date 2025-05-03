package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
    
    /**
     * 根据条件查询酒店列表（带分页）
     */
    @Select("<script>" +
            "SELECT h.*, " +
            "r1.name as province_name, " +
            "r2.name as city_name, " +
            "r3.name as district_name, " +
            "s.name as scenic_spot_name " +
            "FROM hotels h " +
            "LEFT JOIN regions r1 ON h.province_id = r1.id " +
            "LEFT JOIN regions r2 ON h.city_id = r2.id " +
            "LEFT JOIN regions r3 ON h.district_id = r3.id " +
            "LEFT JOIN scenic_spots s ON h.scenic_spot_id = s.id " +
            "<where> " +
            "<if test='name != null and name != \"\"'> " +
            "   AND h.name LIKE CONCAT('%', #{name}, '%') " +
            "</if> " +
            "<if test='provinceId != null'> " +
            "   AND h.province_id = #{provinceId} " +
            "</if> " +
            "<if test='cityId != null'> " +
            "   AND h.city_id = #{cityId} " +
            "</if> " +
            "<if test='districtId != null'> " +
            "   AND h.district_id = #{districtId} " +
            "</if> " +
            "<if test='level != null and level != \"\"'> " +
            "   AND h.level = #{level} " +
            "</if> " +
            "<if test='scenicSpotId != null'> " +
            "   AND h.scenic_spot_id = #{scenicSpotId} " +
            "</if> " +
            "<if test='status != null and status != \"\"'> " +
            "   AND h.status = #{status} " +
            "</if> " +
            "</where> " +
            "ORDER BY h.sort ASC, h.id ASC" +
            "</script>")
    Page<Hotel> findByConditions(Page<Hotel> page, 
                                @Param("name") String name,
                                @Param("provinceId") Long provinceId, 
                                @Param("cityId") Long cityId,
                                @Param("districtId") Long districtId,
                                @Param("level") String level,
                                @Param("scenicSpotId") Long scenicSpotId,
                                @Param("status") String status);
    
    /**
     * 获取酒店详情（包含地区名称和景区名称）
     */
    @Select("SELECT h.*, " +
            "r1.name as province_name, " +
            "r2.name as city_name, " +
            "r3.name as district_name, " +
            "s.name as scenic_spot_name " +
            "FROM hotels h " +
            "LEFT JOIN regions r1 ON h.province_id = r1.id " +
            "LEFT JOIN regions r2 ON h.city_id = r2.id " +
            "LEFT JOIN regions r3 ON h.district_id = r3.id " +
            "LEFT JOIN scenic_spots s ON h.scenic_spot_id = s.id " +
            "WHERE h.id = #{id}")
    Hotel findDetailById(@Param("id") Long id);
    
    /**
     * 获取指定景区周边的酒店
     */
    @Select("SELECT h.*, " +
            "r1.name as province_name, " +
            "r2.name as city_name, " +
            "r3.name as district_name, " +
            "s.name as scenic_spot_name " +
            "FROM hotels h " +
            "LEFT JOIN regions r1 ON h.province_id = r1.id " +
            "LEFT JOIN regions r2 ON h.city_id = r2.id " +
            "LEFT JOIN regions r3 ON h.district_id = r3.id " +
            "LEFT JOIN scenic_spots s ON h.scenic_spot_id = s.id " +
            "WHERE h.scenic_spot_id = #{scenicSpotId} AND h.status = 'active' " +
            "ORDER BY h.distance_to_spot ASC, h.sort ASC " +
            "LIMIT #{limit}")
    List<Hotel> findHotelsByScenicSpot(@Param("scenicSpotId") Long scenicSpotId, @Param("limit") Integer limit);
    
    /**
     * 根据区域获取酒店数量
     */
    @Select("SELECT COUNT(*) FROM hotels WHERE province_id = #{provinceId}")
    Integer countByProvinceId(@Param("provinceId") Long provinceId);
} 