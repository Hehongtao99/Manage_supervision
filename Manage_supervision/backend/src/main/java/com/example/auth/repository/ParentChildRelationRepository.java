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
public interface ParentChildRelationRepository extends JpaRepository<ParentChildRelation, Long> {
    List<ParentChildRelation> findByParent(User parent);
    
    List<ParentChildRelation> findByChild(User child);
    
    Optional<ParentChildRelation> findByParentAndChild(User parent, User child);
    
    Optional<ParentChildRelation> findByParentIdAndChildId(Long parentId, Long childId);
    
    @Query("SELECT pcr FROM ParentChildRelation pcr WHERE pcr.parent.id = :parentId AND pcr.child.id = :childId AND pcr.status = :status")
    Optional<ParentChildRelation> findByParentIdAndChildIdAndStatus(
            @Param("parentId") Long parentId, 
            @Param("childId") Long childId,
            @Param("status") String status);
    
    @Query("SELECT pcr FROM ParentChildRelation pcr WHERE pcr.parent.id = :parentId AND pcr.status = :status")
    List<ParentChildRelation> findByParentIdAndStatus(@Param("parentId") Long parentId, @Param("status") String status);
    
    boolean existsByParentIdAndChildId(Long parentId, Long childId);
} 