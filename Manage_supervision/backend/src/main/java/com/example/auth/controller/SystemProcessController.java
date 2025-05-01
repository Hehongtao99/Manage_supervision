package com.example.auth.controller;

import com.example.auth.entity.SystemProcess;
import com.example.auth.service.SystemProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 系统进程控制器
 */
@RestController
@RequestMapping("/api/system/process")
public class SystemProcessController {

    private final SystemProcessService systemProcessService;

    @Autowired
    public SystemProcessController(SystemProcessService systemProcessService) {
        this.systemProcessService = systemProcessService;
    }

    /**
     * 获取所有进程列表
     * @return 进程列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<SystemProcess>> getAllProcesses() {
        return ResponseEntity.ok(systemProcessService.getProcessList());
    }
    
    /**
     * 分页获取进程列表
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 进程列表
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getProcessesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<SystemProcess> processes = systemProcessService.getProcessListPaged(page, size);
        // 获取总进程数量
        int totalCount = systemProcessService.getProcessList().size();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("processes", processes);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalItems", totalCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取Top N个进程
     * @param limit 最大返回数量
     * @param sortBy 排序字段（cpu或memory）
     * @return 进程列表
     */
    @GetMapping("/top")
    public ResponseEntity<List<SystemProcess>> getTopProcesses(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "cpu") String sortBy) {
        boolean sortByCpu = !"memory".equalsIgnoreCase(sortBy);
        return ResponseEntity.ok(systemProcessService.getTopProcesses(limit, sortByCpu));
    }

    /**
     * 根据进程ID获取进程详情
     * @param pid 进程ID
     * @return 进程详情
     */
    @GetMapping("/{pid}")
    public ResponseEntity<SystemProcess> getProcessById(@PathVariable Long pid) {
        SystemProcess process = systemProcessService.getProcessById(pid);
        if (process == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(process);
    }

    /**
     * 搜索进程
     * @param name 进程名称
     * @return 符合条件的进程列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<SystemProcess>> searchProcesses(@RequestParam String name) {
        return ResponseEntity.ok(systemProcessService.searchProcessByName(name));
    }
} 