package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.entity.AdvertisementApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface AdvertisementApplicationMapper extends BaseMapper<AdvertisementApplication> {
    
    /**
     * 根据条件查询广告申请记录
     */
    @Select("<script>" +
            "SELECT a.* FROM advertisement_applications a " +
            "LEFT JOIN users u ON a.applicant_id = u.id " +
            "WHERE 1=1 " +
            "<if test='applicationNumber != null and applicationNumber != \"\"'>" +
            "  AND a.application_number LIKE CONCAT('%', #{applicationNumber}, '%') " +
            "</if>" +
            "<if test='area != null and area != \"\"'>" +
            "  AND a.area LIKE CONCAT('%', #{area}, '%') " +
            "</if>" +
            "<if test='location != null and location != \"\"'>" +
            "  AND a.location LIKE CONCAT('%', #{location}, '%') " +
            "</if>" +
            "<if test='adType != null and adType != \"\"'>" +
            "  AND a.ad_type = #{adType} " +
            "</if>" +
            "<if test='adNature != null and adNature != \"\"'>" +
            "  AND a.ad_nature = #{adNature} " +
            "</if>" +
            "<if test='minSize != null'>" +
            "  AND a.size >= #{minSize} " +
            "</if>" +
            "<if test='maxSize != null'>" +
            "  AND a.size &lt;= #{maxSize} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "  AND a.status = #{status} " +
            "</if>" +
            "ORDER BY a.create_time DESC" +
            "</script>")
    IPage<AdvertisementApplication> findByConditions(
            IPage<AdvertisementApplication> page,
            @Param("applicationNumber") String applicationNumber,
            @Param("area") String area,
            @Param("location") String location,
            @Param("adType") String adType,
            @Param("adNature") String adNature,
            @Param("minSize") BigDecimal minSize,
            @Param("maxSize") BigDecimal maxSize,
            @Param("status") String status);
    
    /**
     * 获取申请编号的最大值
     */
    @Select("SELECT MAX(application_number) FROM advertisement_applications")
    String getMaxApplicationNumber();
} 