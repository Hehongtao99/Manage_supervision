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
     * 根据父级ID获取子级地区列表
     */
    @Select("SELECT * FROM regions WHERE parent_id = #{parentId} AND status = 'active' ORDER BY sort ASC, id ASC")
    List<Region> findByParentId(@Param("parentId") Long parentId);
    
    /**
     * 获取所有省份
     */
    @Select("SELECT * FROM regions WHERE level = 1 AND status = 'active' ORDER BY sort ASC, id ASC")
    List<Region> findAllProvinces();
    
    /**
     * 根据级别获取地区列表
     */
    @Select("SELECT * FROM regions WHERE level = #{level} AND status = 'active' ORDER BY sort ASC, id ASC")
    List<Region> findByLevel(@Param("level") Integer level);
    
    /**
     * 获取所有地区数据
     */
    @Select("SELECT * FROM regions WHERE status = 'active' ORDER BY level ASC, sort ASC, id ASC")
    List<Region> findAllRegions();
    
    /**
     * 根据地区编码查询
     */
    @Select("SELECT * FROM regions WHERE code = #{code} LIMIT 1")
    Region findByCode(@Param("code") String code);
    
    /**
     * 根据ID查询子地区数量
     */
    @Select("SELECT COUNT(*) FROM regions WHERE parent_id = #{parentId}")
    Integer countByParentId(@Param("parentId") Long parentId);
} 