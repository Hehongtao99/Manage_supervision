package com.example.auth.service.impl;

import com.example.auth.dto.ClassDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.Class;
import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.ClassTeacherRelation;
import com.example.auth.entity.User;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.ClassStudentRepository;
import com.example.auth.repository.ClassTeacherRelationRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserRepository userRepository;
    private final ClassTeacherRelationRepository classTeacherRelationRepository;

    @Autowired
    public ClassServiceImpl(
            ClassRepository classRepository,
            ClassStudentRepository classStudentRepository,
            UserRepository userRepository,
            ClassTeacherRelationRepository classTeacherRelationRepository) {
        this.classRepository = classRepository;
        this.classStudentRepository = classStudentRepository;
        this.userRepository = userRepository;
        this.classTeacherRelationRepository = classTeacherRelationRepository;
    }

    @Override
    @Transactional
    public ClassDTO createClass(ClassDTO classDTO) {
        Class classEntity = classDTO.toEntity();
        classEntity = classRepository.save(classEntity);
        return new ClassDTO(classEntity);
    }

    @Override
    @Transactional
    public ClassDTO updateClass(Long id, ClassDTO classDTO) {
        Class existingClass = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + id));
        
        existingClass.setClassName(classDTO.getClassName());
        existingClass.setGrade(classDTO.getGrade());
        existingClass.setDescription(classDTO.getDescription());
        
        existingClass = classRepository.save(existingClass);
        return new ClassDTO(existingClass);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        classRepository.deleteById(id);
    }

    @Override
    public ClassDTO getClassById(Long id) {
        Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + id));
        ClassDTO classDTO = new ClassDTO(classEntity);
        
        // 获取班级的学生数量
        Long studentCount = (long) classStudentRepository.findActiveStudentsByClassId(id).size();
        classDTO.setStudentCount(studentCount.intValue());
        
        return classDTO;
    }

    @Override
    public List<ClassDTO> getAllClasses() {
        List<ClassDTO> classDTOs = classRepository.findAll().stream()
                .map(ClassDTO::new)
                .collect(Collectors.toList());
        
        // 为每个班级设置学生数量
        for (ClassDTO classDTO : classDTOs) {
            Long studentCount = (long) classStudentRepository.findActiveStudentsByClassId(classDTO.getId()).size();
            classDTO.setStudentCount(studentCount.intValue());
        }
        
        return classDTOs;
    }

    @Override
    public List<ClassDTO> searchClasses(String keyword) {
        List<ClassDTO> classDTOs = classRepository.findByClassNameContaining(keyword).stream()
                .map(ClassDTO::new)
                .collect(Collectors.toList());
        
        // 为每个班级设置学生数量
        for (ClassDTO classDTO : classDTOs) {
            Long studentCount = (long) classStudentRepository.findActiveStudentsByClassId(classDTO.getId()).size();
            classDTO.setStudentCount(studentCount.intValue());
        }
        
        return classDTOs;
    }
    
    // 班级-学生关系的实现
    
    @Override
    public List<UserDTO> getStudentsByClassId(Long classId) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            throw new RuntimeException("班级不存在: " + classId);
        }
        
        List<User> students = classStudentRepository.findActiveStudentsByClassId(classId);
        return students.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserDTO> getUnassignedStudentsByClassId(Long classId) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            throw new RuntimeException("班级不存在: " + classId);
        }
        
        List<User> unassignedStudents = classStudentRepository.findUnassignedStudentsByClassId(classId);
        return unassignedStudents.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean addStudentToClass(Long classId, Long studentId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在: " + studentId));
        
        // 检查学生是否已经在班级中
        if (classStudentRepository.existsByClassEntityAndStudent(classEntity, student)) {
            // 如果关系已存在但状态为inactive，则更新为active
            Optional<ClassStudentRelation> existingRelation = 
                    classStudentRepository.findByClassEntityAndStudent(classEntity, student);
            
            if (existingRelation.isPresent()) {
                ClassStudentRelation relation = existingRelation.get();
                if (!"active".equals(relation.getStatus())) {
                    relation.setStatus("active");
                    relation.setAssignTime(LocalDateTime.now());
                    classStudentRepository.save(relation);
                }
            }
            
            return true;
        }
        
        // 检查学生是否已经在其他班级中
        List<ClassStudentRelation> existingRelations = classStudentRepository.findByStudentAndStatus(student, "active");
        if (!existingRelations.isEmpty()) {
            // 学生已经在其他班级中，不允许添加
            return false;
        }
        
        // 创建新的班级-学生关系
        ClassStudentRelation relation = new ClassStudentRelation();
        relation.setClassEntity(classEntity);
        relation.setStudent(student);
        relation.setStatus("active");
        relation.setAssignTime(LocalDateTime.now());
        
        classStudentRepository.save(relation);
        return true;
    }
    
    @Override
    @Transactional
    public int addStudentsToClass(Long classId, List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return 0;
        }
        
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        List<User> students = userRepository.findAllById(studentIds);
        if (students.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (User student : students) {
            // 使用更新后的addStudentToClass方法，会检查学生是否已在其他班级
            boolean success = addStudentToClass(classId, student.getId());
            if (success) {
                count++;
            }
        }
        
        return count;
    }
    
    @Override
    @Transactional
    public boolean removeStudentFromClass(Long classId, Long studentId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在: " + studentId));
        
        Optional<ClassStudentRelation> optionalRelation = 
                classStudentRepository.findByClassEntityAndStudent(classEntity, student);
        
        if (optionalRelation.isPresent()) {
            ClassStudentRelation relation = optionalRelation.get();
            relation.setStatus("inactive");
            classStudentRepository.save(relation);
            return true;
        }
        
        return false;
    }
    
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setUserNumber(user.getUserNumber());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime().toString());
        
        List<String> roles = new ArrayList<>();
        if (user.getRoles() != null) {
            roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());
        }
        dto.setRoles(roles);
        
        return dto;
    }

    /**
     * 实现获取教师教授的班级列表
     */
    @Override
    public List<Map<String, Object>> getClassesByTeacherId(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("教师不存在"));
        
        List<ClassTeacherRelation> relations = classTeacherRelationRepository.findByTeacherAndStatus(teacher, "active");
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ClassTeacherRelation relation : relations) {
            Class classEntity = relation.getClassEntity();
            Map<String, Object> classMap = new HashMap<>();
            classMap.put("id", classEntity.getId());
            classMap.put("className", classEntity.getClassName());
            classMap.put("grade", classEntity.getGrade());
            classMap.put("description", classEntity.getDescription());
            classMap.put("createTime", classEntity.getCreateTime());
            classMap.put("updateTime", classEntity.getUpdateTime());
            classMap.put("status", classEntity.getStatus());
            
            // 查询班级学生数量
            long studentCount = classStudentRepository.findByClassEntityAndStatus(classEntity, "active").size();
            classMap.put("studentCount", studentCount);
            
            result.add(classMap);
        }
        
        return result;
    }

    /**
     * 获取学生所在的班级列表
     */
    @Override
    public List<Map<String, Object>> getClassesByStudentId(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        List<ClassStudentRelation> relations = classStudentRepository.findByStudentAndStatus(student, "active");
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ClassStudentRelation relation : relations) {
            Class classEntity = relation.getClassEntity();
            Map<String, Object> classMap = new HashMap<>();
            classMap.put("id", classEntity.getId());
            classMap.put("className", classEntity.getClassName());
            classMap.put("grade", classEntity.getGrade());
            classMap.put("description", classEntity.getDescription());
            classMap.put("createTime", classEntity.getCreateTime());
            classMap.put("updateTime", classEntity.getUpdateTime());
            classMap.put("status", classEntity.getStatus());
            
            // 查询班级学生数量
            long studentCount = classStudentRepository.findByClassEntityAndStatus(classEntity, "active").size();
            classMap.put("studentCount", studentCount);
            
            // 加入分配时间
            classMap.put("assignTime", relation.getAssignTime());
            
            result.add(classMap);
        }
        
        return result;
    }

    /**
     * 实现获取班级学生列表(详细信息)
     */
    @Override
    public List<Map<String, Object>> getClassStudentsDetail(Long classId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        List<ClassStudentRelation> relations = classStudentRepository.findByClassEntityAndStatus(classEntity, "active");
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ClassStudentRelation relation : relations) {
            User student = relation.getStudent();
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("id", student.getId());
            studentMap.put("username", student.getUsername());
            studentMap.put("realName", student.getRealName());
            studentMap.put("nickname", student.getNickname());
            studentMap.put("userNumber", student.getUserNumber());
            studentMap.put("email", student.getEmail());
            studentMap.put("phone", student.getPhone());
            studentMap.put("avatar", student.getAvatar());
            studentMap.put("assignTime", relation.getAssignTime());
            
            result.add(studentMap);
        }
        
        return result;
    }

    /**
     * 验证教师是否被分配到班级
     */
    @Override
    public boolean isTeacherAssignedToClass(Long teacherId, Long classId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("教师不存在"));
        
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        return classTeacherRelationRepository.existsByClassEntityAndTeacherAndStatus(classEntity, teacher, "active");
    }
} 