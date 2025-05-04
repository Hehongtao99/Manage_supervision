package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.mapper.CompanionServiceMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.model.dto.CompanionServiceDTO;
import com.example.auth.model.entity.CompanionService;
import com.example.auth.model.entity.User;
import com.example.auth.service.CompanionServiceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanionServiceServiceImpl implements CompanionServiceService {

    @Autowired
    private CompanionServiceMapper companionServiceMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public Long createService(CompanionService companionService) {
        // 设置创建时间
        companionService.setCreatedTime(LocalDateTime.now());
        companionService.setUpdatedTime(LocalDateTime.now());
        
        // 默认状态为待审核
        companionService.setStatus("inactive");
        companionService.setReviewStatus("pending");
        
        companionServiceMapper.insert(companionService);
        return companionService.getId();
    }
    
    @Override
    public boolean updateService(CompanionService companionService) {
        // 更新时间
        companionService.setUpdatedTime(LocalDateTime.now());
        
        // 如果修改了重要内容，需要重新审核
        CompanionService oldService = companionServiceMapper.selectById(companionService.getId());
        if (oldService != null && hasSignificantChanges(oldService, companionService)) {
            companionService.setReviewStatus("pending");
            companionService.setStatus("inactive");
        }
        
        return companionServiceMapper.updateById(companionService) > 0;
    }
    
    /**
     * 判断是否有重要内容的变更
     */
    private boolean hasSignificantChanges(CompanionService oldService, CompanionService newService) {
        // 检查标题、描述、游戏类型、价格等重要信息是否变更
        return !oldService.getTitle().equals(newService.getTitle())
                || !oldService.getDescription().equals(newService.getDescription())
                || !oldService.getGameTypes().equals(newService.getGameTypes())
                || !oldService.getPrice().equals(newService.getPrice());
    }
    
    @Override
    public CompanionServiceDTO getServiceById(Long serviceId) {
        CompanionService service = companionServiceMapper.selectById(serviceId);
        if (service == null) {
            return null;
        }
        
        return convertToDTO(service);
    }
    
    @Override
    public List<CompanionServiceDTO> getServicesByCompanionId(Long companionId) {
        LambdaQueryWrapper<CompanionService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanionService::getCompanionId, companionId);
        
        List<CompanionService> services = companionServiceMapper.selectList(queryWrapper);
        
        return services.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<CompanionServiceDTO> getActiveServices(int page, int size) {
        // 查询激活状态的服务
        Page<CompanionService> servicePage = new Page<>(page, size);
        LambdaQueryWrapper<CompanionService> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanionService::getStatus, "active");
        
        companionServiceMapper.selectPage(servicePage, queryWrapper);
        
        // 将结果转换为DTO
        Page<CompanionServiceDTO> dtoPage = new Page<>(
                servicePage.getCurrent(),
                servicePage.getSize(),
                servicePage.getTotal()
        );
        
        List<CompanionServiceDTO> records = servicePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
    
    @Override
    public boolean updateServiceStatus(Long serviceId, String status) {
        CompanionService service = new CompanionService();
        service.setId(serviceId);
        service.setStatus(status);
        service.setUpdatedTime(LocalDateTime.now());
        
        return companionServiceMapper.updateById(service) > 0;
    }
    
    @Override
    public boolean deleteService(Long serviceId) {
        return companionServiceMapper.deleteById(serviceId) > 0;
    }
    
    @Override
    public Page<CompanionServiceDTO> getAllServices(int page, int size) {
        Page<CompanionService> servicePage = new Page<>(page, size);
        companionServiceMapper.selectPage(servicePage, null);
        
        return convertToPageDTO(servicePage);
    }
    
    @Override
    public Page<CompanionServiceDTO> getServicesByStatus(int page, int size, String status) {
        Page<CompanionService> servicePage = new Page<>(page, size);
        LambdaQueryWrapper<CompanionService> queryWrapper = new LambdaQueryWrapper<>();
        
        if ("pending".equals(status)) {
            // 待审核状态
            queryWrapper.eq(CompanionService::getReviewStatus, "pending");
        } else {
            // 其他状态
            queryWrapper.eq(CompanionService::getStatus, status);
        }
        
        companionServiceMapper.selectPage(servicePage, queryWrapper);
        
        return convertToPageDTO(servicePage);
    }
    
    @Override
    public boolean approveService(Long serviceId, Long reviewerId) {
        CompanionService service = companionServiceMapper.selectById(serviceId);
        if (service == null) {
            return false;
        }
        
        service.setReviewStatus("approved");
        service.setStatus("active");
        service.setReviewTime(LocalDateTime.now());
        service.setReviewerId(reviewerId);
        service.setUpdatedTime(LocalDateTime.now());
        
        return companionServiceMapper.updateById(service) > 0;
    }
    
    @Override
    public boolean rejectService(Long serviceId, String reason, Long reviewerId) {
        CompanionService service = companionServiceMapper.selectById(serviceId);
        if (service == null) {
            return false;
        }
        
        service.setReviewStatus("rejected");
        service.setStatus("inactive");
        service.setReviewTime(LocalDateTime.now());
        service.setReviewComment(reason);
        service.setReviewerId(reviewerId);
        service.setUpdatedTime(LocalDateTime.now());
        
        return companionServiceMapper.updateById(service) > 0;
    }
    
    /**
     * 将实体转换为DTO
     */
    private CompanionServiceDTO convertToDTO(CompanionService service) {
        CompanionServiceDTO dto = new CompanionServiceDTO();
        BeanUtils.copyProperties(service, dto);
        
        // 获取陪玩信息
        User companion = userMapper.selectById(service.getCompanionId());
        if (companion != null) {
            dto.setCompanionName(companion.getRealName() != null ? companion.getRealName() : companion.getUsername());
            dto.setCompanionAvatar(companion.getAvatar());
        }
        
        // 获取审核人信息
        if (service.getReviewerId() != null) {
            User reviewer = userMapper.selectById(service.getReviewerId());
            if (reviewer != null) {
                dto.setReviewerName(reviewer.getRealName() != null ? reviewer.getRealName() : reviewer.getUsername());
            }
        }
        
        return dto;
    }
    
    /**
     * 将分页实体转换为分页DTO
     */
    private Page<CompanionServiceDTO> convertToPageDTO(Page<CompanionService> servicePage) {
        Page<CompanionServiceDTO> dtoPage = new Page<>(
                servicePage.getCurrent(),
                servicePage.getSize(),
                servicePage.getTotal()
        );
        
        List<CompanionServiceDTO> records = servicePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
} 