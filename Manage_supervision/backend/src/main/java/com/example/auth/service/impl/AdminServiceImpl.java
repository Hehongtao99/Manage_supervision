package com.example.auth.service.impl;

import com.example.auth.dto.PageResponse;
import com.example.auth.dto.RoleDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AdminService;
import com.example.auth.util.PasswordUtils;
import com.example.auth.util.UserNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserNumberGenerator userNumberGenerator;
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResponse<UserDTO> getUserList(int page, int size, String username, String role, String status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 这里应该根据条件进行查询，为简化示例，先返回所有用户
        Page<User> users = userRepository.findAll(pageable);
        
        List<UserDTO> userDTOs = users.getContent().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(userDTOs, users.getTotalElements(), page, size);
    }
    
    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO, String password) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        // 使用PasswordUtils加密密码
        user.setPassword(PasswordUtils.encryptPassword(password != null ? password : ""));
        user.setRealName(userDTO.getRealName());
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setStatus("active"); // 确保新创建的用户默认状态为active
        user.setCreateTime(LocalDateTime.now());
        
        // 设置角色
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByNameIn(userDTO.getRoles());
            user.setRoles(new HashSet<>(roles));
            
            // 根据角色生成用户编号
            String role = userDTO.getRoles().get(0);
            String userNumber = userNumberGenerator.generateUserNumberByRole(role);
            user.setUserNumber(userNumber);
        } else {
            // 默认为USER角色
            Role userRole = roleRepository.findByName("USER");
            if (userRole == null) {
                userRole = new Role();
                userRole.setName("USER");
                roleRepository.save(userRole);
            }
            user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
            
            // 生成学生编号
            String userNumber = userNumberGenerator.generateStudentNumber();
            user.setUserNumber(userNumber);
        }
        
        User savedUser = userRepository.save(user);
        return convertToUserDTO(savedUser);
    }
    
    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // 只更新非空字段
            if (userDTO.getRealName() != null) {
                user.setRealName(userDTO.getRealName());
            }
            
            if (userDTO.getNickname() != null) {
                user.setNickname(userDTO.getNickname());
            }
            
            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
            }
            
            if (userDTO.getPhone() != null) {
                user.setPhone(userDTO.getPhone());
            }
            
            // 更新角色
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                String newRoleName = userDTO.getRoles().get(0);
                List<Role> roles = roleRepository.findByNameIn(Collections.singletonList(newRoleName));
                user.setRoles(new HashSet<>(roles));
                
                // 检查是否需要更新用户编号
                if (userNumberGenerator.needsNumberUpdate(user.getUserNumber(), newRoleName)) {
                    // 生成新的用户编号
                    String newUserNumber = userNumberGenerator.generateUserNumberByRole(newRoleName);
                    user.setUserNumber(newUserNumber);
                }
            }
            
            User updatedUser = userRepository.save(user);
            return convertToUserDTO(updatedUser);
        }
        return null;
    }
    
    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 切换状态
            user.setStatus("active".equals(user.getStatus()) ? "inactive" : "active");
            userRepository.save(user);
        }
    }
    
    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 使用PasswordUtils加密密码
            user.setPassword(PasswordUtils.encryptPassword(newPassword));
            userRepository.save(user);
        }
    }
    
    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        
        if (roleDTO.getPermissions() != null) {
            role.setPermissions(String.join(",", roleDTO.getPermissions()));
        } else {
            role.setPermissions("");
        }
        
        role.setCreateTime(LocalDateTime.now());
        
        Role savedRole = roleRepository.save(role);
        return convertToRoleDTO(savedRole);
    }
    
    @Override
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            role.setDescription(roleDTO.getDescription());
            
            if (roleDTO.getPermissions() != null) {
                role.setPermissions(String.join(",", roleDTO.getPermissions()));
            } else {
                role.setPermissions("");
            }
            
            Role updatedRole = roleRepository.save(role);
            return convertToRoleDTO(updatedRole);
        }
        return null;
    }
    
    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
    
    // 添加初始化方法，在服务启动时修复数据
    @PostConstruct
    @Transactional
    public void initializeRoles() {
        List<Role> roles = roleRepository.findAll();
        boolean hasChanges = false;
        
        for (Role role : roles) {
            if (role.getPermissions() == null) {
                role.setPermissions(""); // 设置默认值为空字符串
                hasChanges = true;
            }
            
            if (role.getCreateTime() == null) {
                role.setCreateTime(LocalDateTime.now()); // 为空的createTime设置当前时间
                hasChanges = true;
            }
        }
        
        // 只有在有修改时才保存
        if (hasChanges) {
            roleRepository.saveAll(roles);
        }
        
        // 为没有用户编号的用户生成对应的编号
        generateMissingUserNumbers();
    }
    
    /**
     * 为没有用户编号的用户生成编号
     */
    private void generateMissingUserNumbers() {
        List<User> users = userRepository.findAll();
        boolean hasChanges = false;
        
        for (User user : users) {
            if (user.getUserNumber() == null || user.getUserNumber().isEmpty()) {
                // 获取用户的主要角色
                String roleName = user.getRoles().stream()
                        .findFirst()
                        .map(Role::getName)
                        .orElse("USER");
                
                // 生成对应的用户编号
                String userNumber = userNumberGenerator.generateUserNumberByRole(roleName);
                user.setUserNumber(userNumber);
                hasChanges = true;
            }
        }
        
        if (hasChanges) {
            userRepository.saveAll(users);
        }
    }
    
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setUserNumber(user.getUserNumber());
        
        // 格式化创建时间
        if (user.getCreateTime() != null) {
            dto.setCreateTime(user.getCreateTime().format(formatter));
        }
        
        // 获取角色名称列表
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        dto.setRoles(roleNames);
        
        return dto;
    }
    
    private RoleDTO convertToRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        
        // 将权限字符串转换为列表
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            dto.setPermissions(List.of(role.getPermissions().split(",")));
        } else {
            dto.setPermissions(new ArrayList<>());
        }
        
        return dto;
    }
} 