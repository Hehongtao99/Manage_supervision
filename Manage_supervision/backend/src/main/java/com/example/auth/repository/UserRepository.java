package com.example.auth.repository;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    
    Long countByRolesContaining(Role role);
    
    Long countByStatus(String status);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRolesNameContaining(@Param("roleName") String roleName);
    
    Page<User> findByRolesContaining(Role role, Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND (u.username LIKE %:username% OR u.realName LIKE %:realName%)")
    Page<User> findByRolesContainingAndUsernameContainingOrRealNameContaining(
            @Param("role") Role role, 
            @Param("username") String username, 
            @Param("realName") String realName, 
            Pageable pageable);
    
    List<User> findByRolesContaining(Role role);
    
    /**
     * 根据角色名称查找用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * 根据班级ID获取该班级所有学生的ID列表
     */
    @Query("SELECT csr.student.id FROM ClassStudentRelation csr WHERE csr.classEntity.id = :classId AND csr.status = 'ACTIVE'")
    List<Long> findStudentIdsByClassId(@Param("classId") Long classId);
    
    /**
     * 检查是否是家长和学生的关系
     */
    @Query("SELECT COUNT(pcr) > 0 FROM ParentChildRelation pcr WHERE pcr.parent.id = :parentId AND pcr.child.id = :childId AND pcr.status = 'confirmed'")
    boolean isParentOfStudent(@Param("parentId") Long parentId, @Param("childId") Long childId);
    
    /**
     * 检查教师是否是学生的老师
     */
    @Query("SELECT COUNT(ctr) > 0 FROM ClassTeacherRelation ctr JOIN ClassStudentRelation csr ON ctr.classEntity.id = csr.classEntity.id " +
           "WHERE ctr.teacher.id = :teacherId AND csr.student.id = :studentId AND ctr.status = 'active' AND csr.status = 'active'")
    boolean isTeacherOfStudent(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);
} 