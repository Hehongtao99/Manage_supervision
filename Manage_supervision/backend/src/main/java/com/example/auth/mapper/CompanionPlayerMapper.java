package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.CompanionPlayerRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CompanionPlayerMapper extends BaseMapper<CompanionPlayerRelation> {
    
    /**
     * 根据陪玩ID查询关系
     */
    @Select("SELECT * FROM companion_player_relations WHERE companion_id = #{companionId}")
    List<CompanionPlayerRelation> findByCompanionId(@Param("companionId") Long companionId);
    
    /**
     * 根据玩家ID查询关系
     */
    @Select("SELECT * FROM companion_player_relations WHERE player_id = #{playerId}")
    List<CompanionPlayerRelation> findByPlayerId(@Param("playerId") Long playerId);
    
    /**
     * 检查陪玩和玩家是否存在关系
     */
    @Select("SELECT COUNT(*) > 0 FROM companion_player_relations " +
            "WHERE companion_id = #{companionId} AND player_id = #{playerId}")
    boolean existsByCompanionIdAndPlayerId(@Param("companionId") Long companionId, @Param("playerId") Long playerId);
    
    /**
     * 根据陪玩ID和玩家ID查找关系
     */
    @Select("SELECT * FROM companion_player_relations " +
            "WHERE companion_id = #{companionId} AND player_id = #{playerId} LIMIT 1")
    CompanionPlayerRelation findByCompanionIdAndPlayerId(@Param("companionId") Long companionId, @Param("playerId") Long playerId);
    
    /**
     * 查询特定陪玩的所有有效玩家ID
     */
    @Select("SELECT player_id FROM companion_player_relations " +
            "WHERE companion_id = #{companionId} AND status = 'active'")
    List<Long> findActivePlayerIdsByCompanionId(@Param("companionId") Long companionId);
    
    /**
     * 查询特定玩家的所有有效陪玩ID
     */
    @Select("SELECT companion_id FROM companion_player_relations " +
            "WHERE player_id = #{playerId} AND status = 'active'")
    List<Long> findActiveCompanionIdsByPlayerId(@Param("playerId") Long playerId);

    /**
     * 查询未分配陪玩的玩家ID列表
     */
    @Select("SELECT u.id FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} " +
            "AND u.id NOT IN (SELECT player_id FROM companion_player_relations WHERE status = 'active')")
    List<Long> findUnassignedPlayerIdsByRoleId(@Param("roleId") Long roleId);
} 