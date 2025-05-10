package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.mapper.CourseApplicationMapper;
import com.example.auth.mapper.SubjectMapper;
import com.example.auth.model.dto.SubjectDTO;
import com.example.auth.model.entity.CourseApplication;
import com.example.auth.model.entity.Subject;
import com.example.auth.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程科目服务实现
 */
@Service
public class SubjectServiceImpl implements SubjectService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);
    
    @Autowired
    private SubjectMapper subjectMapper;
    
    @Autowired
    private CourseApplicationMapper courseApplicationMapper;
    
    // 时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<SubjectDTO> getAllSubjects() {
        logger.info("获取所有科目列表");
        
        try {
            LambdaQueryWrapper<Subject> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(Subject::getName);
            
            List<Subject> subjects = subjectMapper.selectList(queryWrapper);
            return convertToDTOList(subjects);
        } catch (Exception e) {
            logger.error("获取科目列表出错", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public SubjectDTO getSubjectById(Long id) {
        logger.info("根据ID获取科目: {}", id);
        
        try {
            Subject subject = subjectMapper.selectById(id);
            return convertToDTO(subject);
        } catch (Exception e) {
            logger.error("获取科目出错", e);
            return null;
        }
    }
    
    @Override
    public Long createSubject(String name) {
        logger.info("创建新科目: {}", name);
        
        try {
            // 检查科目名称是否已存在
            LambdaQueryWrapper<Subject> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Subject::getName, name);
            
            if (subjectMapper.selectCount(queryWrapper) > 0) {
                logger.warn("科目名称已存在: {}", name);
                return null;
            }
            
            Subject subject = new Subject();
            subject.setName(name);
            subject.setCreateTime(LocalDateTime.now());
            subject.setUpdateTime(LocalDateTime.now());
            
            subjectMapper.insert(subject);
            logger.info("科目创建成功: {}", subject.getId());
            
            return subject.getId();
        } catch (Exception e) {
            logger.error("创建科目出错", e);
            return null;
        }
    }
    
    @Override
    public boolean updateSubject(Long id, String name) {
        logger.info("更新科目 - ID: {}, 新名称: {}", id, name);
        
        try {
            Subject subject = subjectMapper.selectById(id);
            if (subject == null) {
                logger.warn("科目不存在: {}", id);
                return false;
            }
            
            // 检查名称是否已存在(排除自身)
            LambdaQueryWrapper<Subject> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Subject::getName, name).ne(Subject::getId, id);
            
            if (subjectMapper.selectCount(queryWrapper) > 0) {
                logger.warn("科目名称已存在: {}", name);
                return false;
            }
            
            subject.setName(name);
            subject.setUpdateTime(LocalDateTime.now());
            
            subjectMapper.updateById(subject);
            logger.info("科目更新成功: {}", id);
            
            return true;
        } catch (Exception e) {
            logger.error("更新科目出错", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteSubject(Long id) {
        logger.info("删除科目 - ID: {}", id);
        
        try {
            Subject subject = subjectMapper.selectById(id);
            if (subject == null) {
                logger.warn("科目不存在: {}", id);
                return false;
            }
            
            // 检查是否有课程申请使用了该科目
            LambdaQueryWrapper<CourseApplication> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseApplication::getSubject, subject.getName());
            
            if (courseApplicationMapper.selectCount(queryWrapper) > 0) {
                logger.warn("科目已被课程申请使用，无法删除: {}", id);
                return false;
            }
            
            subjectMapper.deleteById(id);
            logger.info("科目删除成功: {}", id);
            
            return true;
        } catch (DataIntegrityViolationException e) {
            logger.error("科目已被引用，无法删除: {}", id, e);
            return false;
        } catch (Exception e) {
            logger.error("删除科目出错", e);
            return false;
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private SubjectDTO convertToDTO(Subject subject) {
        if (subject == null) {
            return null;
        }
        
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        
        if (subject.getCreateTime() != null) {
            dto.setCreateTime(subject.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        
        if (subject.getUpdateTime() != null) {
            dto.setUpdateTime(subject.getUpdateTime().format(DATE_TIME_FORMATTER));
        }
        
        return dto;
    }
    
    /**
     * 将实体列表转换为DTO列表
     */
    private List<SubjectDTO> convertToDTOList(List<Subject> subjects) {
        List<SubjectDTO> dtoList = new ArrayList<>();
        
        if (subjects != null) {
            for (Subject subject : subjects) {
                SubjectDTO dto = convertToDTO(subject);
                if (dto != null) {
                    dtoList.add(dto);
                }
            }
        }
        
        return dtoList;
    }
} 