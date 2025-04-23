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
} 