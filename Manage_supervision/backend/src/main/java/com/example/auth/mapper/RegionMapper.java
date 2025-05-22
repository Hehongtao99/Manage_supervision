package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 省市区街道Mapper接口
 */
@Mapper
public interface RegionMapper extends BaseMapper<Region> {
    
    /**
     * 查询指定父级下的所有子节点
     */
    @Select("<script>" +
            "SELECT r.*, " +
            "(SELECT COUNT(*) FROM region WHERE parent_id = r.id) > 0 AS has_children " +
            "FROM region r " +
            "WHERE 1=1 " +
            "<if test='parentId != null'>" +
            "  AND r.parent_id = #{parentId}" +
            "</if>" +
            "<if test='parentId == null'>" +
            "  AND r.parent_id IS NULL" +
            "</if>" +
            "ORDER BY r.sort_order, r.id" +
            "</script>")
    List<Region> findByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据级别查询所有区域
     */
    @Select("SELECT * FROM region WHERE level = #{level} ORDER BY sort_order, id")
    List<Region> findByLevel(@Param("level") Integer level);
    
    /**
     * 检查是否有子节点
     */
    @Select("SELECT COUNT(*) FROM region WHERE parent_id = #{id}")
    int countChildrenById(@Param("id") Long id);
    
    /**
     * 查询指定编码是否已存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM region WHERE code = #{code} " +
            "<if test='id != null'>" +
            "  AND id != #{id}" +
            "</if>" +
            "</script>")
    int countByCode(@Param("code") String code, @Param("id") Long id);
    
    /**
     * 获取指定区域的完整路径（从省到当前级别）
     */
    @Select("<script>" +
            "WITH RECURSIVE region_path AS (" +
            "  SELECT id, parent_id, name, level " +
            "  FROM region " +
            "  WHERE id = #{id} " +
            "  UNION ALL " +
            "  SELECT r.id, r.parent_id, r.name, r.level " +
            "  FROM region r " +
            "  JOIN region_path rp ON r.id = rp.parent_id " +
            ") " +
            "SELECT * FROM region_path ORDER BY level" +
            "</script>")
    List<Region> findPath(@Param("id") Long id);
} 