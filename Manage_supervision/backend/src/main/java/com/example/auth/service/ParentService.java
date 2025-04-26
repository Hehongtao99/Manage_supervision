package com.example.auth.service;

import com.example.auth.entity.ParentChildRelation;
import com.example.auth.entity.User;

import java.util.List;
import java.util.Map;

public interface ParentService {
    
    /**
     * 创建家长-子女关系
     * @param parentId 家长ID
     * @param childIdentifier 子女标识符（用户名或学号）
     * @param relationType 关系类型（父亲/母亲/监护人）
     * @return 创建的关系对象
     */
    ParentChildRelation createRelation(Long parentId, String childIdentifier, String relationType);
    
    /**
     * 更新关系状态
     * @param relationId 关系ID
     * @param status 新状态
     * @return 更新后的关系对象
     */
    ParentChildRelation updateRelationStatus(Long relationId, String status);
    
    /**
     * 获取家长的所有子女关系
     * @param parentId 家长ID
     * @return 子女关系列表
     */
    List<ParentChildRelation> getChildRelations(Long parentId);
    
    /**
     * 获取家长的所有已确认子女关系
     * @param parentId 家长ID
     * @return 已确认的子女关系列表
     */
    List<ParentChildRelation> getConfirmedChildRelations(Long parentId);
    
    /**
     * 获取学生的所有家长关系
     * @param childId 学生ID
     * @return 家长关系列表
     */
    List<ParentChildRelation> getParentRelations(Long childId);
    
    /**
     * 获取学生的所有已确认家长关系
     * @param childId 学生ID
     * @return 已确认的家长关系列表
     */
    List<ParentChildRelation> getConfirmedParentRelations(Long childId);
    
    /**
     * 获取子女详细信息（包括班级、任务等）
     * @param childId 子女ID
     * @return 子女详细信息
     */
    Map<String, Object> getChildDetails(Long childId);
    
    /**
     * 获取子女的课程和任务信息
     * @param childId 子女ID
     * @return 课程和任务信息
     */
    Map<String, Object> getChildCourseAndTasks(Long childId);
    
    /**
     * 获取特定家长的通知
     * @param parentId 家长ID
     * @return 通知列表
     */
    List<Map<String, Object>> getParentNotifications(Long parentId);
    
    /**
     * 解除家长-子女关系
     * @param relationId 关系ID
     * @return 操作是否成功
     */
    boolean removeRelation(Long relationId);
    
    /**
     * 检查是否存在家长和子女的关系
     * @param parentId 家长ID
     * @param childId 子女ID
     * @return 是否存在关系
     */
    boolean relationExists(Long parentId, Long childId);
    
    /**
     * 获取子女的统计信息（考勤率、作业完成率、平均成绩等）
     * @param childId 子女ID
     * @return 子女统计信息
     */
    Map<String, Object> getChildStats(Long childId);
} 