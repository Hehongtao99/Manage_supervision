package com.example.auth.controller;

import com.example.auth.dto.HostDTO;
import com.example.auth.dto.PageRequestDTO;
import com.example.auth.dto.PageResponseDTO;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.service.HostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 主机管理控制器
 */
@RestController
@RequestMapping("/api/hosts")
public class HostController {

    @Autowired
    private HostService hostService;
    
    /**
     * 获取主机列表（分页）
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<HostDTO>> getHosts(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder
    ) {
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(page);
        pageRequest.setPageSize(pageSize);
        pageRequest.setQuery(query);
        pageRequest.setSortBy(sortBy);
        pageRequest.setSortOrder(sortOrder);
        
        return ResponseEntity.ok(hostService.getHosts(pageRequest));
    }
    
    /**
     * 获取主机详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<HostDTO> getHostById(@PathVariable Long id) {
        if (id == null) {
            System.err.println("Received request for host with null ID.");
            return ResponseEntity.badRequest().build(); 
        }
        
        try {
            // 如果ID为负数，这可能是前端的特殊标记，直接返回404
            if (id < 0) {
                return ResponseEntity.notFound().build();
            }
            
            HostDTO hostDTO = hostService.getHostById(id);
            return ResponseEntity.ok(hostDTO);
        } catch (ResourceNotFoundException e) {
            // 记录错误
            System.err.println("Host not found: " + e.getMessage());
            
            // 如果ID明显是时间戳格式 (超过10位) - 这段逻辑可能不再需要，因为前端已修复
            // 但保留以防万一
            if (id.toString().length() > 10) {
                try {
                    // 尝试获取所有主机的第一个
                    PageRequestDTO pageRequest = new PageRequestDTO();
                    pageRequest.setPage(1);
                    pageRequest.setPageSize(1);
                    PageResponseDTO<HostDTO> hosts = hostService.getHosts(pageRequest);
                    if (hosts.getData() != null && !hosts.getData().isEmpty()) {
                        // 返回第一个主机
                        System.out.println("Returning first host instead of timestamp ID: " + id);
                        return ResponseEntity.ok(hosts.getData().get(0));
                    }
                } catch (Exception ex) {
                    System.err.println("Error while trying to get first host: " + ex.getMessage());
                }
            }
            
            // 如果仍然无法获取任何主机，返回404
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 捕获其他所有异常
            System.err.println("Unexpected error getting host by ID " + id + ": " + e.getMessage());
            // 打印堆栈跟踪，方便调试
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取当前主机的真实信息
     */
    @GetMapping("/current")
    public ResponseEntity<HostDTO> getCurrentHostInfo() {
        return ResponseEntity.ok(hostService.getCurrentHostInfo());
    }
    
    /**
     * 添加主机
     */
    @PostMapping
    public ResponseEntity<HostDTO> addHost(@RequestBody HostDTO hostDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hostService.addHost(hostDTO));
    }
    
    /**
     * 更新主机信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<HostDTO> updateHost(@PathVariable Long id, @RequestBody HostDTO hostDTO) {
        return ResponseEntity.ok(hostService.updateHost(id, hostDTO));
    }
    
    /**
     * 删除主机
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHost(@PathVariable Long id) {
        hostService.deleteHost(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 根据用户ID获取主机列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HostDTO>> getHostsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(hostService.getHostsByUserId(userId));
    }
    
    /**
     * 根据状态获取主机列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<HostDTO>> getHostsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(hostService.getHostsByStatus(status));
    }
    
    /**
     * 检查主机名是否存在
     */
    @GetMapping("/check/hostname/{hostname}")
    public ResponseEntity<Boolean> isHostnameExists(@PathVariable String hostname) {
        return ResponseEntity.ok(hostService.isHostnameExists(hostname));
    }
    
    /**
     * 检查IP是否存在
     */
    @GetMapping("/check/ip/{ip}")
    public ResponseEntity<Boolean> isIpExists(@PathVariable String ip) {
        return ResponseEntity.ok(hostService.isIpExists(ip));
    }
} 