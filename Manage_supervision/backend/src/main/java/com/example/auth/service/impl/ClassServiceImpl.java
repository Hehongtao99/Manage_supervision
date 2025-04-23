package com.example.auth.service.impl;

import com.example.auth.dto.ClassDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.Class;
import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.ClassTeacherRelation;
import com.example.auth.entity.StudentClassHistory;
import com.example.auth.entity.User;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.ClassStudentRepository;
import com.example.auth.repository.ClassTeacherRelationRepository;
import com.example.auth.repository.StudentClassHistoryRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ClassService;
import com.example.auth.util.UserContext;
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
    private final StudentClassHistoryRepository studentClassHistoryRepository;
    private final UserContext userContext;

    @Autowired
    public ClassServiceImpl(
            ClassRepository classRepository,
            ClassStudentRepository classStudentRepository,
            UserRepository userRepository,
            ClassTeacherRelationRepository classTeacherRelationRepository,
            StudentClassHistoryRepository studentClassHistoryRepository,
            UserContext userContext) {
        this.classRepository = classRepository;
        this.classStudentRepository = classStudentRepository;
        this.userRepository = userRepository;
        this.classTeacherRelationRepository = classTeacherRelationRepository;
        this.studentClassHistoryRepository = studentClassHistoryRepository;
        this.userContext = userContext;
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
        
        // 获取当前登录用户作为操作者
        User operator = userContext.getCurrentUser();
        if (operator == null) {
            throw new RuntimeException("当前用户未登录或会话已过期");
        }
        
        // 查找学生现有的班级关系
        List<ClassStudentRelation> existingRelations = classStudentRepository.findByStudent(student);
        
        // 如果学生已在目标班级中
        Optional<ClassStudentRelation> targetClassRelation = existingRelations.stream()
                .filter(relation -> relation.getClassEntity().getId().equals(classId))
                .findFirst();
        
        if (targetClassRelation.isPresent()) {
            // 如果学生已经在目标班级且状态为active，不需要任何操作
            ClassStudentRelation relation = targetClassRelation.get();
            if ("active".equals(relation.getStatus())) {
                return true;
            }
            
            // 如果状态不是active，更新为active
            relation.setStatus("active");
            relation.setAssignTime(LocalDateTime.now());
            classStudentRepository.save(relation);
            
            // 记录重新激活的历史
            StudentClassHistory history = new StudentClassHistory();
            history.setStudent(student);
            history.setClassEntity(classEntity);
            history.setOperationType("join");
            history.setOperator(operator);
            history.setRemark("重新激活学生班级关系");
            studentClassHistoryRepository.save(history);
            
            return true;
        }
        
        // 如果学生在其他班级中
        Optional<ClassStudentRelation> otherClassRelation = existingRelations.stream()
                .filter(relation -> !relation.getClassEntity().getId().equals(classId))
                .findFirst();
        
        ClassStudentRelation relationToUpdate = null;
        
        if (otherClassRelation.isPresent()) {
            // 学生在其他班级，记录离开旧班级的历史
            ClassStudentRelation oldRelation = otherClassRelation.get();
            Class oldClass = oldRelation.getClassEntity();
            
            StudentClassHistory leaveHistory = new StudentClassHistory();
            leaveHistory.setStudent(student);
            leaveHistory.setClassEntity(oldClass);
            leaveHistory.setOperationType("leave");
            leaveHistory.setOperator(operator);
            leaveHistory.setRemark("转班离开");
            studentClassHistoryRepository.save(leaveHistory);
            
            System.out.println("学生 " + student.getRealName() + " (ID: " + student.getId() + ") 从班级 " 
                + oldClass.getClassName() + " (ID: " + oldClass.getId() + ") 转出");
            
            // 使用现有关系进行更新，而不是删除再创建
            relationToUpdate = oldRelation;
            relationToUpdate.setClassEntity(classEntity);
            relationToUpdate.setStatus("active");
            relationToUpdate.setAssignTime(LocalDateTime.now());
        } else {
            // 学生没有任何班级关系，创建新的
            relationToUpdate = new ClassStudentRelation();
            relationToUpdate.setClassEntity(classEntity);
            relationToUpdate.setStudent(student);
            relationToUpdate.setStatus("active");
            relationToUpdate.setAssignTime(LocalDateTime.now());
        }
        
        // 保存关系（新建或更新）
        ClassStudentRelation savedRelation = classStudentRepository.save(relationToUpdate);
        
        // 记录加入新班级的历史
        StudentClassHistory joinHistory = new StudentClassHistory();
        joinHistory.setStudent(student);
        joinHistory.setClassEntity(classEntity);
        joinHistory.setOperationType("join");
        joinHistory.setOperator(operator);
        joinHistory.setRemark(otherClassRelation.isPresent() ? "转班加入" : "首次加入班级");
        studentClassHistoryRepository.save(joinHistory);
        
        System.out.println("学生 " + student.getRealName() + " (ID: " + student.getId() + ") 加入班级 " 
            + classEntity.getClassName() + " (ID: " + classEntity.getId() + ")");
        
        return savedRelation != null;
    }
    
    @Override
    @Transactional
    public Map<String, Object> addStudentsToClass(Long classId, List<Long> studentIds) {
        int successCount = 0;
        int failCount = 0;
        Map<String, Object> result = new HashMap<>();
        
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        for (Long studentId : studentIds) {
            try {
                boolean success = addStudentToClass(classId, studentId);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                System.err.println("添加学生 " + studentId + " 到班级 " + classId + " 失败: " + e.getMessage());
            }
        }
        
        result.put("success", true);
        result.put("message", "成功添加 " + successCount + " 名学生，失败 " + failCount + " 名");
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean removeStudentFromClass(Long classId, Long studentId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("班级不存在: " + classId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在: " + studentId));
        
        // 获取当前登录用户作为操作者
        User operator = userContext.getCurrentUser();
        if (operator == null) {
            throw new RuntimeException("当前用户未登录或会话已过期");
        }
        
        Optional<ClassStudentRelation> optionalRelation = 
                classStudentRepository.findByClassEntityAndStudent(classEntity, student);
        
        if (optionalRelation.isPresent()) {
            ClassStudentRelation relation = optionalRelation.get();
            
            // 记录学生离开班级的历史
            StudentClassHistory history = new StudentClassHistory();
            history.setStudent(student);
            history.setClassEntity(classEntity);
            history.setOperationType("leave");
            history.setOperator(operator);
            history.setRemark("从班级移除");
            studentClassHistoryRepository.save(history);
            
            // 删除班级学生关系
            classStudentRepository.delete(relation);
            
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

    @Override
    public List<Map<String, Object>> getAllAvailableStudentsForClass(Long classId) {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            throw new RuntimeException("班级不存在: " + classId);
        }
        
        // 获取所有具有学生角色的用户
        List<User> allStudents = classStudentRepository.findAllAvailableStudentsForClass(classId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (User student : allStudents) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("id", student.getId());
            studentMap.put("username", student.getUsername());
            studentMap.put("realName", student.getRealName());
            studentMap.put("userNumber", student.getUserNumber());
            studentMap.put("email", student.getEmail());
            
            // 获取学生当前所在的班级信息
            Optional<Class> currentClass = classStudentRepository.findActiveClassByStudentId(student.getId());
            if (currentClass.isPresent()) {
                studentMap.put("currentClassId", currentClass.get().getId());
                studentMap.put("currentClassName", currentClass.get().getClassName());
            } else {
                studentMap.put("currentClassId", null);
                studentMap.put("currentClassName", null);
            }
            
            result.add(studentMap);
        }
        
        return result;
    }

    @Override
    public Long getCurrentUserId() {
        User user = userContext.getCurrentUser();
        if (user == null) {
            System.out.println("警告: 无法获取当前用户，可能是认证问题");
            throw new RuntimeException("当前用户未登录或会话已过期");
        }
        System.out.println("当前用户ID: " + user.getId() + ", 用户名: " + user.getUsername() + ", 角色: " + user.getRoles());
        return user.getId();
    }
} 