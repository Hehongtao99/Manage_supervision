package com.example.auth.service.impl;

import com.example.auth.dto.BillDTO;
import com.example.auth.dto.BillGenerateDTO;
import com.example.auth.dto.BillPaymentDTO;
import com.example.auth.entity.Bill;
import com.example.auth.entity.Class;
import com.example.auth.entity.ClassStudentRelation;
import com.example.auth.entity.FeeStandard;
import com.example.auth.entity.User;
import com.example.auth.repository.BillRepository;
import com.example.auth.repository.ClassRepository;
import com.example.auth.repository.ClassStudentRelationRepository;
import com.example.auth.repository.ClassTeacherRelationRepository;
import com.example.auth.repository.FeeStandardRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillServiceImpl.class);

    private final BillRepository billRepository;
    private final FeeStandardRepository feeStandardRepository;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final ClassTeacherRelationRepository classTeacherRelationRepository;
    private final ClassStudentRelationRepository classStudentRelationRepository;

    @Autowired
    public BillServiceImpl(BillRepository billRepository,
                           FeeStandardRepository feeStandardRepository,
                           UserRepository userRepository,
                           ClassRepository classRepository,
                           ClassTeacherRelationRepository classTeacherRelationRepository,
                           ClassStudentRelationRepository classStudentRelationRepository) {
        this.billRepository = billRepository;
        this.feeStandardRepository = feeStandardRepository;
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.classTeacherRelationRepository = classTeacherRelationRepository;
        this.classStudentRelationRepository = classStudentRelationRepository;
    }

    @Override
    public Page<BillDTO> getAllBills(Pageable pageable) {
        return billRepository.findAllActive(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public BillDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("账单不存在"));
        
        if (Boolean.TRUE.equals(bill.getIsDeleted())) {
            throw new EntityNotFoundException("账单不存在");
        }
        
        return convertToDTO(bill);
    }

    @Override
    public List<BillDTO> getBillsByStudentId(Long studentId) {
        return billRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BillDTO> getBillsByStudentId(Long studentId, Pageable pageable) {
        return billRepository.findByStudentId(studentId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<BillDTO> getBillsByStatus(Boolean status, Pageable pageable) {
        return billRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public BillDTO updateBillStatus(Long id, Boolean status) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("账单不存在"));
        
        if (Boolean.TRUE.equals(bill.getIsDeleted())) {
            throw new EntityNotFoundException("账单不存在");
        }
        
        bill.setStatus(status);
        if (Boolean.TRUE.equals(status)) {
            bill.setPaymentTime(LocalDateTime.now());
        } else {
            bill.setPaymentTime(null);
        }
        bill.setUpdateTime(LocalDateTime.now());
        
        Bill updatedBill = billRepository.save(bill);
        return convertToDTO(updatedBill);
    }

    @Override
    @Transactional
    public BillDTO payBill(Long id, BillPaymentDTO paymentDTO) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("账单不存在"));
        
        if (Boolean.TRUE.equals(bill.getIsDeleted())) {
            throw new EntityNotFoundException("账单不存在");
        }
        
        if (Boolean.TRUE.equals(bill.getStatus())) {
            throw new IllegalStateException("账单已支付，不能重复支付");
        }
        
        bill.setStatus(true);
        bill.setPaymentTime(LocalDateTime.now());
        bill.setPaymentMethod(paymentDTO.getPaymentMethod());
        bill.setPaymentMessage(paymentDTO.getPaymentMessage());
        bill.setUpdateTime(LocalDateTime.now());
        
        Bill updatedBill = billRepository.save(bill);
        return convertToDTO(updatedBill);
    }

    @Override
    @Transactional
    public List<BillDTO> generateBills(BillGenerateDTO generateDTO) {
        logger.info("开始生成账单: {}", generateDTO);
        
        FeeStandard feeStandard = feeStandardRepository.findById(generateDTO.getFeeStandardId())
                .orElseThrow(() -> new EntityNotFoundException("费用标准不存在"));
        
        if (Boolean.TRUE.equals(feeStandard.getIsDeleted())) {
            throw new EntityNotFoundException("费用标准不存在");
        }
        
        List<Long> existingStudentIds = billRepository.findStudentIdsWithFeeStandard(generateDTO.getFeeStandardId());
        logger.info("已有该费用标准账单的学生数量: {}", existingStudentIds.size());
        
        List<Long> targetStudentIds = new ArrayList<>();
        
        // 如果指定了学生ID列表
        if (generateDTO.getStudentIds() != null && !generateDTO.getStudentIds().isEmpty()) {
            logger.info("按指定学生列表生成账单，学生数量: {}", generateDTO.getStudentIds().size());
            targetStudentIds.addAll(generateDTO.getStudentIds());
        } 
        // 如果指定了班级ID
        else if (generateDTO.getClassId() != null) {
            logger.info("按班级生成账单，班级ID: {}", generateDTO.getClassId());
            List<ClassStudentRelation> classStudentRelations = 
                    classStudentRelationRepository.findByClassId(generateDTO.getClassId());
            
            if (classStudentRelations != null && !classStudentRelations.isEmpty()) {
                targetStudentIds.addAll(classStudentRelations.stream()
                        .map(relation -> relation.getStudent().getId())
                        .collect(Collectors.toList()));
                logger.info("班级内学生数量: {}", targetStudentIds.size());
            } else {
                logger.warn("指定班级内没有学生");
            }
        } 
        // 获取所有学生
        else {
            logger.info("为所有学生生成账单 - 开始筛选学生用户");
            try {
                // 首先尝试使用findByRoleName方法，明确只查找学生角色
                List<User> studentsByRole = userRepository.findByRoleName("ROLE_STUDENT");
                logger.info("方法1 - 通过角色名称查询到的学生数量: {}", studentsByRole != null ? studentsByRole.size() : 0);
                
                if (studentsByRole != null && !studentsByRole.isEmpty()) {
                    logger.info("方法1 - 学生角色查询成功，获取到 {} 名学生", studentsByRole.size());
                    // 记录学生ID和姓名
                    studentsByRole.forEach(student -> 
                        logger.debug("学生ID: {}, 姓名: {}, 学号: {}", 
                                student.getId(), 
                                student.getUsername(), 
                                student.getUserNumber()));
                }
                
                List<Long> studentIds = new ArrayList<>();
                
                // 如果角色查询方法返回了结果，则使用这个结果
                if (studentsByRole != null && !studentsByRole.isEmpty()) {
                    studentIds.addAll(studentsByRole.stream()
                            .map(User::getId)
                            .collect(Collectors.toList()));
                    logger.info("使用方法1: 角色查询找到的学生");
                } else {
                    // 备选方法：使用findAll，但严格筛选只包含学生角色的用户
                    logger.info("方法1未找到学生，使用方法2: 筛选所有用户");
                    List<User> allUsers = userRepository.findAll();
                    logger.info("系统中共有用户数量: {}", allUsers.size());
                    
                    // 记录当前所有角色信息，方便调试
                    allUsers.forEach(user -> {
                        if (user.getRoles() != null) {
                            String roles = user.getRoles().stream()
                                    .map(role -> role.getName())
                                    .collect(Collectors.joining(", "));
                            logger.debug("用户ID: {}, 姓名: {}, 角色: {}", user.getId(), user.getUsername(), roles);
                        }
                    });
                    
                    // 创建一个角色对象用于查询，确保只获取学生角色
                    List<User> students = allUsers.stream()
                            .filter(user -> user.getRoles() != null && !user.getRoles().isEmpty())
                            .filter(user -> user.getRoles().stream()
                                    .anyMatch(role -> "ROLE_STUDENT".equals(role.getName())))
                            // 额外确保不包含教师和管理员角色的用户
                            .filter(user -> user.getRoles().stream()
                                    .noneMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ROLE_TEACHER".equals(role.getName())))
                            .collect(Collectors.toList());
                    
                    logger.info("方法2 - 过滤后学生角色用户数量: {}", students.size());
                    
                    if (!students.isEmpty()) {
                        studentIds.addAll(students.stream()
                                .map(User::getId)
                                .collect(Collectors.toList()));
                        logger.info("使用方法2: 角色过滤找到的学生");
                        
                        // 记录筛选到的学生信息
                        students.forEach(student -> 
                            logger.debug("学生ID: {}, 姓名: {}, 学号: {}", 
                                    student.getId(), 
                                    student.getUsername(), 
                                    student.getUserNumber()));
                    } else {
                        // 第三种方法：检查userNumber字段是否有值，通常学生会有学号
                        logger.info("方法2未找到学生，使用方法3: 根据学号字段判断");
                        List<User> possibleStudents = allUsers.stream()
                                .filter(user -> user.getUserNumber() != null && !user.getUserNumber().isEmpty())
                                // 排除管理员和教师角色
                                .filter(user -> user.getRoles() == null || user.getRoles().isEmpty() || 
                                        user.getRoles().stream()
                                        .noneMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ROLE_TEACHER".equals(role.getName())))
                                .collect(Collectors.toList());
                        
                        logger.info("方法3 - 通过学号字段筛选可能是学生的用户数量: {}", possibleStudents.size());
                        
                        if (!possibleStudents.isEmpty()) {
                            studentIds.addAll(possibleStudents.stream()
                                    .map(User::getId)
                                    .collect(Collectors.toList()));
                            logger.info("使用方法3: 学号字段筛选找到的潜在学生");
                            
                            // 记录筛选到的学生信息
                            possibleStudents.forEach(student -> 
                                logger.debug("学生ID: {}, 姓名: {}, 学号: {}", 
                                        student.getId(), 
                                        student.getUsername(), 
                                        student.getUserNumber()));
                        } else {
                            logger.warn("所有三种方法都未找到学生");
                        }
                    }
                }
                
                // 确保找到的学生ID不重复
                List<Long> uniqueStudentIds = studentIds.stream().distinct().collect(Collectors.toList());
                if (uniqueStudentIds.size() < studentIds.size()) {
                    logger.info("移除了{}个重复的学生ID", studentIds.size() - uniqueStudentIds.size());
                }
                
                if (!uniqueStudentIds.isEmpty()) {
                    targetStudentIds.addAll(uniqueStudentIds);
                    logger.info("最终确定的学生ID列表: {}, 总数: {}", targetStudentIds, targetStudentIds.size());
                } else {
                    logger.warn("系统中未找到任何学生用户");
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                logger.error("获取学生列表失败", e);
                return new ArrayList<>();
            }
        }
        
        // 移除已经有该费用标准账单的学生
        int beforeSize = targetStudentIds.size();
        logger.info("处理前的目标学生数量: {}", beforeSize);
        logger.info("已有账单的学生ID列表: {}", existingStudentIds);
        
        targetStudentIds.removeAll(existingStudentIds);
        logger.info("移除已有账单的学生后，剩余学生数量: {} (移除了{}个)", targetStudentIds.size(), beforeSize - targetStudentIds.size());
        logger.info("需要生成账单的学生ID列表: {}", targetStudentIds);
        
        if (targetStudentIds.isEmpty()) {
            logger.info("没有符合条件的学生需要生成账单");
            return new ArrayList<>();
        }
        
        List<Bill> generatedBills = new ArrayList<>();
        
        try {
            for (Long studentId : targetStudentIds) {
                User student = userRepository.findById(studentId)
                        .orElseThrow(() -> new EntityNotFoundException("学生ID: " + studentId + " 不存在"));
                
                Bill bill = new Bill();
                bill.setStudentId(studentId);
                bill.setFeeStandardId(feeStandard.getId());
                bill.setAmount(feeStandard.getAmount());
                bill.setStatus(false);
                bill.setDueDate(generateDTO.getDueDate());
                bill.setCreateTime(LocalDateTime.now());
                bill.setUpdateTime(LocalDateTime.now());
                bill.setIsDeleted(false);
                
                // 设置关联的费用标准实体
                bill.setFeeStandard(feeStandard);
                
                // 设置关联的学生实体
                bill.setStudent(student);
                
                generatedBills.add(bill);
            }
            
            if (!generatedBills.isEmpty()) {
                logger.info("准备保存 {} 个账单", generatedBills.size());
                List<Bill> savedBills = billRepository.saveAll(generatedBills);
                logger.info("成功保存 {} 个账单", savedBills.size());
                return savedBills.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            } else {
                logger.warn("没有生成任何账单");
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("生成账单失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("账单不存在"));
        
        if (Boolean.TRUE.equals(bill.getStatus())) {
            throw new IllegalStateException("已支付的账单不能删除");
        }
                
        bill.setIsDeleted(true);
        bill.setUpdateTime(LocalDateTime.now());
        billRepository.save(bill);
    }
    
    private BillDTO convertToDTO(Bill bill) {
        BillDTO dto = new BillDTO();
        BeanUtils.copyProperties(bill, dto);
        
        // 设置额外信息
        if (bill.getStudent() != null) {
            dto.setStudentName(bill.getStudent().getUsername());
        }
        
        // 确保费用名称正确设置
        if (bill.getFeeStandard() != null) {
            // 优先使用FeeStandard的feeName，如果为空则使用name字段
            String feeName = bill.getFeeStandard().getFeeName();
            if (feeName == null || feeName.trim().isEmpty()) {
                feeName = bill.getFeeStandard().getName();
            }
            dto.setFeeName(feeName);
            logger.debug("账单ID: {}, 使用关联对象获取费用名称: {}", bill.getId(), feeName);
        } else if (bill.getFeeStandardId() != null) {
            // 如果关联对象为空，则尝试查询
            try {
                FeeStandard feeStandard = feeStandardRepository.findById(bill.getFeeStandardId()).orElse(null);
                if (feeStandard != null) {
                    // 优先使用FeeStandard的feeName，如果为空则使用name字段
                    String feeName = feeStandard.getFeeName();
                    if (feeName == null || feeName.trim().isEmpty()) {
                        feeName = feeStandard.getName();
                    }
                    dto.setFeeName(feeName);
                    logger.debug("账单ID: {}, 使用查询获取费用名称: {}", bill.getId(), feeName);
                } else {
                    logger.warn("未能找到费用标准 ID: {}", bill.getFeeStandardId());
                    // 设置一个默认值，避免前端显示空白
                    dto.setFeeName("未知费用项目");
                }
            } catch (Exception e) {
                logger.error("获取费用标准名称失败", e);
                // 设置一个默认值，避免前端显示空白
                dto.setFeeName("未知费用项目");
            }
        } else {
            // 如果没有费用标准ID，也设置一个默认值
            logger.warn("账单没有关联费用标准 ID, 账单ID: {}", bill.getId());
            dto.setFeeName("未知费用项目");
        }
        
        return dto;
    }

    @Override
    public Page<BillDTO> getBillsByClassId(Long classId, Pageable pageable) {
        logger.info("获取班级ID={}的所有学生账单", classId);
        
        // 检查班级是否存在
        Class classEntity = classRepository.findById(classId)
            .orElseThrow(() -> new EntityNotFoundException("班级不存在"));
        
        // 获取班级内所有学生ID
        List<Long> studentIds = userRepository.findStudentIdsByClassId(classId);
        
        if (studentIds.isEmpty()) {
            logger.info("班级ID={}没有学生", classId);
            return Page.empty(pageable);
        }
        
        logger.info("班级ID={}有{}名学生", classId, studentIds.size());
        
        // 获取这些学生的所有账单
        Page<Bill> billPage = billRepository.findByStudentIdIn(studentIds, pageable);
        
        return billPage.map(this::convertToDTO);
    }

    @Override
    public Page<BillDTO> getBillsByTeacherId(Long teacherId, Pageable pageable) {
        logger.info("获取教师ID={}管理的所有班级学生账单", teacherId);
        
        // 检查教师是否存在
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new EntityNotFoundException("教师不存在"));
        
        // 获取教师管理的所有班级
        List<Class> classes = classTeacherRelationRepository.findByTeacherAndStatus(teacher, "ACTIVE")
            .stream()
            .map(relation -> relation.getClassEntity())
            .collect(Collectors.toList());
        
        if (classes.isEmpty()) {
            logger.info("教师ID={}没有管理的班级", teacherId);
            return Page.empty(pageable);
        }
        
        // 获取这些班级的所有学生ID
        Set<Long> studentIds = new HashSet<>();
        for (Class classEntity : classes) {
            List<Long> classStudentIds = userRepository.findStudentIdsByClassId(classEntity.getId());
            studentIds.addAll(classStudentIds);
        }
        
        if (studentIds.isEmpty()) {
            logger.info("教师ID={}管理的班级没有学生", teacherId);
            return Page.empty(pageable);
        }
        
        logger.info("教师ID={}管理的班级有{}名学生", teacherId, studentIds.size());
        
        // 获取这些学生的所有账单
        Page<Bill> billPage = billRepository.findByStudentIdIn(new ArrayList<>(studentIds), pageable);
        
        return billPage.map(this::convertToDTO);
    }
} 