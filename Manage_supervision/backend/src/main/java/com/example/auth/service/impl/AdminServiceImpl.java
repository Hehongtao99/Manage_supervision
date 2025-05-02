package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.RoleDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.model.entity.Role;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.RoleMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.mapper.UserRoleMapper;
import com.example.auth.service.AdminService;
import com.example.auth.util.PasswordUtils;
import com.example.auth.util.UserNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private UserNumberGenerator userNumberGenerator;
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResponse<UserDTO> getUserList(int page, int size, String username, String role, String status) {
        Page<User> pageParam = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        if (role != null && !role.isEmpty()) {
            // 如果指定了角色，需要联表查询
            Role roleEntity = roleMapper.findByName(role);
            if (roleEntity != null) {
                // 获取该角色下的用户
                Page<User> userPage;
                if (username != null && !username.isEmpty() || status != null && !status.isEmpty()) {
                    // 使用复杂查询
                    userPage = (Page<User>) userMapper.findByConditions(pageParam, username, role, status);
                } else {
                    // 只按角色查询
                    userPage = (Page<User>) userMapper.findByRoleIdPage(pageParam, roleEntity.getId());
                }
                
                // 为每个用户设置角色
                List<UserDTO> userDTOs = new ArrayList<>();
                for (User user : userPage.getRecords()) {
                    List<Role> userRoles = roleMapper.findRolesByUserId(user.getId());
                    user.setRoles(new HashSet<>(userRoles));
                    userDTOs.add(convertToUserDTO(user));
                }
                
                return new PageResponse<>(userDTOs, userPage.getTotal(), (int)pageParam.getCurrent(), (int)pageParam.getSize());
            }
        } else {
            // 不指定角色，直接查询用户表
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            
            if (username != null && !username.isEmpty()) {
                queryWrapper.like(User::getUsername, username)
                           .or()
                           .like(User::getRealName, username);
            }
            
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq(User::getStatus, status);
            }
            
            Page<User> userPage = userMapper.selectPage(pageParam, queryWrapper);
            
            // 为每个用户设置角色
            List<UserDTO> userDTOs = new ArrayList<>();
            for (User user : userPage.getRecords()) {
                List<Role> userRoles = roleMapper.findRolesByUserId(user.getId());
                user.setRoles(new HashSet<>(userRoles));
                userDTOs.add(convertToUserDTO(user));
            }
            
            return new PageResponse<>(userDTOs, userPage.getTotal(), (int)pageParam.getCurrent(), (int)pageParam.getSize());
        }
        
        // 如果没有找到符合条件的数据，返回空结果
        return new PageResponse<>(new ArrayList<>(), 0L, page, size);
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
            String roleName = userDTO.getRoles().get(0);
            // 根据角色生成用户编号
            String userNumber = userNumberGenerator.generateUserNumberByRole(roleName);
            user.setUserNumber(userNumber);
            
            // 保存用户
            userMapper.insert(user);
            
            // 获取角色并建立关联
            List<Role> roles = roleMapper.findByNameIn(userDTO.getRoles());
            for (Role role : roles) {
                userRoleMapper.insertUserRole(user.getId(), role.getId());
            }
            user.setRoles(new HashSet<>(roles));
        } else {
            // 默认为USER角色
            Role userRole = roleMapper.findByName("USER");
            if (userRole == null) {
                userRole = new Role();
                userRole.setName("USER");
                roleMapper.insert(userRole);
            }
            
            // 生成学生编号
            String userNumber = userNumberGenerator.generateStudentNumber();
            user.setUserNumber(userNumber);
            
            // 保存用户
            userMapper.insert(user);
            
            // 建立用户-角色关联
            userRoleMapper.insertUserRole(user.getId(), userRole.getId());
            user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        }
        
        return convertToUserDTO(user);
    }
    
    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userMapper.selectById(id);
        if (user != null) {
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
                List<Role> roles = roleMapper.findByNameIn(Collections.singletonList(newRoleName));
                
                if (!roles.isEmpty()) {
                    Role newRole = roles.get(0);
                    
                    // 获取当前用户的角色
                    List<Role> currentRoles = roleMapper.findRolesByUserId(user.getId());
                    Set<String> currentRoleNames = currentRoles.stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet());
                    
                    // 检查是否需要更新用户编号
                    if (!currentRoleNames.contains(newRoleName) && 
                        userNumberGenerator.needsNumberUpdate(user.getUserNumber(), newRoleName)) {
                        // 生成新的用户编号
                        String newUserNumber = userNumberGenerator.generateUserNumberByRole(newRoleName);
                        user.setUserNumber(newUserNumber);
                    }
                    
                    // 更新用户-角色关联
                    userRoleMapper.deleteUserRoles(user.getId());
                    userRoleMapper.insertUserRole(user.getId(), newRole.getId());
                    
                    user.setRoles(new HashSet<>(Collections.singletonList(newRole)));
                }
            }
            
            // 更新用户信息
            userMapper.updateById(user);
            
            return convertToUserDTO(user);
        }
        return null;
    }
    
    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            // 切换状态
            user.setStatus("active".equals(user.getStatus()) ? "inactive" : "active");
            userMapper.updateById(user);
        }
    }
    
    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user != null) {
            // 使用PasswordUtils加密密码
            user.setPassword(PasswordUtils.encryptPassword(newPassword));
            userMapper.updateById(user);
        }
    }
    
    @Override
    public List<RoleDTO> getAllRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        List<Role> roles = roleMapper.selectList(queryWrapper);
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
        
        roleMapper.insert(role);
        return convertToRoleDTO(role);
    }
    
    @Override
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleMapper.selectById(id);
        if (role != null) {
            role.setDescription(roleDTO.getDescription());
            
            if (roleDTO.getPermissions() != null) {
                role.setPermissions(String.join(",", roleDTO.getPermissions()));
            } else {
                role.setPermissions("");
            }
            
            roleMapper.updateById(role);
            return convertToRoleDTO(role);
        }
        return null;
    }
    
    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleMapper.deleteById(id);
    }
    
    // 添加初始化方法，在服务启动时修复数据
    @PostConstruct
    @Transactional
    public void initializeRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        List<Role> roles = roleMapper.selectList(queryWrapper);
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
            
            if (hasChanges) {
                roleMapper.updateById(role);
            }
        }
        
        // 为没有用户编号的用户生成对应的编号
        generateMissingUserNumbers();
    }
    
    /**
     * 为没有用户编号的用户生成编号
     */
    private void generateMissingUserNumbers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(User::getUserNumber).or().eq(User::getUserNumber, "");
        List<User> users = userMapper.selectList(queryWrapper);
        
        for (User user : users) {
            // 获取用户的主要角色
            List<Role> roles = roleMapper.findRolesByUserId(user.getId());
            String roleName = roles.stream()
                    .findFirst()
                    .map(Role::getName)
                    .orElse("USER");
            
            // 生成对应的用户编号
            String userNumber = userNumberGenerator.generateUserNumberByRole(roleName);
            user.setUserNumber(userNumber);
            userMapper.updateById(user);
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

    // 实现统计数据相关接口
    @Override
    public Long countTotalUsers() {
        return userMapper.selectCount(null);
    }

    @Override
    public Long countActiveUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, "active");
        return userMapper.selectCount(queryWrapper);
    }

    @Override
    public Long countTotalRoles() {
        return roleMapper.selectCount(null);
    }

    @Override
    public Map<String, Long> getUserRoleDistribution() {
        // 获取所有角色
        List<Role> allRoles = roleMapper.selectList(null);
        Map<String, Long> distribution = new HashMap<>();
        
        // 为每个角色查询用户数量
        for (Role role : allRoles) {
            Long userCount = (long) userRoleMapper.countUsersByRoleId(role.getId());
            distribution.put(role.getName(), userCount);
        }
        
        return distribution;
    }

    @Override
    public Map<String, List<Object>> getUserActivityLastWeek() {
        // 获取近7天日期
        List<Object> datesList = new ArrayList<>();
        List<Object> countsList = new ArrayList<>();
        
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d");
        
        // 计算近7天的用户活跃数据
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = today.minusDays(i);
            String formattedDate = date.format(dateFormatter);
            datesList.add(formattedDate);
            
            // 在真实环境中，应该从日志表中查询每天的登录用户数
            // 这里使用模拟数据
            int randomCount = new Random().nextInt(70) + 120; // 生成120-190之间的随机数
            countsList.add(randomCount);
        }
        
        Map<String, List<Object>> result = new HashMap<>();
        result.put("dates", datesList);
        result.put("counts", countsList);
        
        return result;
    }
} 