package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegionMapper extends BaseMapper<Region> {
    
    /**
     * 根据父ID查询区域
     */
    @Select("SELECT * FROM region WHERE parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<Region> findByParentId(@Param("parentId") Long parentId);
    
    /**
     * 查询顶级区域（省级）
     */
    @Select("SELECT * FROM region WHERE level = 1 ORDER BY sort_order ASC, id ASC")
    List<Region> findProvinces();
    
    /**
     * 根据等级查询区域
     */
    @Select("SELECT * FROM region WHERE level = #{level} ORDER BY sort_order ASC, id ASC")
    List<Region> findByLevel(@Param("level") Integer level);
    
    /**
     * 根据编码查询区域
     */
    @Select("SELECT * FROM region WHERE code = #{code}")
    Region findByCode(@Param("code") String code);
    
    /**
     * 查询区域是否有子区域
     */
    @Select("SELECT COUNT(*) FROM region WHERE parent_id = #{regionId}")
    int countChildren(@Param("regionId") Long regionId);
} 