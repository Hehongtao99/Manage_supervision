package com.example.auth.repository;

import com.example.auth.entity.ParentChildRelation;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentChildRepository extends JpaRepository<ParentChildRelation, Long> {
    
    // 查找某个家长的所有子女关系
    List<ParentChildRelation> findByParent(User parent);
    
    // 查找某个学生的所有家长关系
    List<ParentChildRelation> findByChild(User child);
    
    // 根据状态查找关系
    List<ParentChildRelation> findByStatus(String status);
    
    // 查找特定的家长-子女关系
    Optional<ParentChildRelation> findByParentAndChild(User parent, User child);
    
    // 根据状态查找家长的子女关系
    List<ParentChildRelation> findByParentAndStatus(User parent, String status);
    
    // 根据状态查找学生的家长关系
    List<ParentChildRelation> findByChildAndStatus(User child, String status);
    
    // 自定义查询：通过家长ID查找已确认的所有子女用户信息
    @Query("SELECT pcr.child FROM ParentChildRelation pcr WHERE pcr.parent.id = :parentId AND pcr.status = 'confirmed'")
    List<User> findConfirmedChildrenByParentId(@Param("parentId") Long parentId);
    
    // 自定义查询：通过子女ID查找已确认的所有家长用户信息
    @Query("SELECT pcr.parent FROM ParentChildRelation pcr WHERE pcr.child.id = :childId AND pcr.status = 'confirmed'")
    List<User> findConfirmedParentsByChildId(@Param("childId") Long childId);
    
    // 自定义查询：检查是否存在指定家长和子女的关系
    @Query("SELECT COUNT(pcr) > 0 FROM ParentChildRelation pcr WHERE pcr.parent.id = :parentId AND pcr.child.id = :childId")
    boolean existsByParentIdAndChildId(@Param("parentId") Long parentId, @Param("childId") Long childId);
} 