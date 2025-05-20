package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.RunnerSupervisorRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RunnerSupervisorMapper extends BaseMapper<RunnerSupervisorRelation> {

    /**
     * 查询未分配给任何管理员的跑步爱好者ID
     */
    @Select("SELECT u.id FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} " +
            "AND u.id NOT IN (SELECT runner_id FROM runner_supervisor_relations WHERE status = 'active')")
    List<Long> findUnassignedRunnerIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询特定管理员的所有活跃跑步爱好者ID
     */
    @Select("SELECT runner_id FROM runner_supervisor_relations " +
            "WHERE supervisor_id = #{supervisorId} AND status = 'active'")
    List<Long> findActiveRunnerIdsBySupervisorId(@Param("supervisorId") Long supervisorId);
    
    /**
     * 根据管理员ID和跑步爱好者ID查询关系记录
     */
    @Select("SELECT * FROM runner_supervisor_relations " +
            "WHERE supervisor_id = #{supervisorId} AND runner_id = #{runnerId} AND status = 'active'")
    RunnerSupervisorRelation findBySupervisorIdAndRunnerId(
            @Param("supervisorId") Long supervisorId, 
            @Param("runnerId") Long runnerId);
    
    /**
     * 更新关系状态
     */
    @Update("UPDATE runner_supervisor_relations SET status = #{status} " +
            "WHERE supervisor_id = #{supervisorId} AND runner_id = #{runnerId}")
    int updateStatus(
            @Param("supervisorId") Long supervisorId, 
            @Param("runnerId") Long runnerId, 
            @Param("status") String status);
    
    /**
     * 删除跑步爱好者的所有关系
     */
    @Delete("DELETE FROM runner_supervisor_relations WHERE runner_id = #{runnerId}")
    int deleteByRunnerId(@Param("runnerId") Long runnerId);
    
    /**
     * 检查管理员和跑步爱好者之间是否存在活跃关系
     */
    @Select("SELECT COUNT(*) > 0 FROM runner_supervisor_relations " +
            "WHERE supervisor_id = #{supervisorId} AND runner_id = #{runnerId} AND status = 'active'")
    boolean existsBySupervisorIdAndRunnerId(
            @Param("supervisorId") Long supervisorId, 
            @Param("runnerId") Long runnerId);
} 