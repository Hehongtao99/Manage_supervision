package com.example.auth.repository;

import com.example.auth.entity.ParentTeacherMessage;
import com.example.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 家长-教师留言存储库接口
 */
@Repository
public interface ParentTeacherMessageRepository extends JpaRepository<ParentTeacherMessage, Long> {
    /**
     * 查询教师收到的所有留言
     * @param teacher 教师用户
     * @param pageable 分页参数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessage> findByTeacher(User teacher, Pageable pageable);
    
    /**
     * 查询教师收到的关于特定学生的留言
     * @param teacher 教师用户
     * @param student 学生用户
     * @param pageable 分页参数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessage> findByTeacherAndStudent(User teacher, User student, Pageable pageable);
    
    /**
     * 查询教师收到的所有留言
     * @param teacherId 教师ID
     * @param pageable 分页参数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessage> findByTeacherId(Long teacherId, Pageable pageable);
    
    /**
     * 查询教师收到的关于特定学生的留言
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @param pageable 分页参数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessage> findByTeacherIdAndStudentId(Long teacherId, Long studentId, Pageable pageable);
    
    /**
     * 查询家长发送的所有留言
     * @param parentId 家长ID
     * @param pageable 分页参数
     * @return 留言分页结果
     */
    Page<ParentTeacherMessage> findByParentId(Long parentId, Pageable pageable);
    
    /**
     * 将教师收到的所有未读留言标记为已读
     * @param teacherId 教师ID
     * @param status 要更新的状态
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE ParentTeacherMessage m SET m.status = :status WHERE m.teacher.id = :teacherId AND m.status = 'unread'")
    int markAllAsRead(@Param("teacherId") Long teacherId, @Param("status") String status);
    
    /**
     * 将家长收到的所有未读回复标记为已读
     * @param parentId 家长ID
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE ParentTeacherMessage m SET m.replyRead = true WHERE m.parent.id = :parentId AND m.replyRead = false AND m.replyContent IS NOT NULL")
    int markAllRepliesAsReadByParent(@Param("parentId") Long parentId);
} 