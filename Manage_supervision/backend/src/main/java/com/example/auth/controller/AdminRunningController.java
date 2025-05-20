package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.RunningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员跑步记录控制器
 */
@RestController
@RequestMapping("/api/admin/running")
public class AdminRunningController {

    @Autowired
    private RunningRecordService runningRecordService;

    /**
     * 管理员获取所有用户的跑步记录（分页）
     *
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选过滤条件）
     * @param startDate 开始日期（可选过滤条件，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选过滤条件，格式：yyyy-MM-dd）
     * @return 分页后的跑步记录列表
     */
    @GetMapping("/records")
    @RequireRole("ADMIN")
    public ResponseVO<PageResponse<Map<String, Object>>> getAllUserRunningRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        PageResponse<Map<String, Object>> response = runningRecordService.getAllUserRunningRecords(
                page, size, username, startDate, endDate);
        
        return ResponseVO.success("获取所有用户跑步记录成功", response);
    }
    
    /**
     * 管理员获取跑步记录总体统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @RequireRole("ADMIN")
    public ResponseVO<Map<String, Object>> getRunningStatistics() {
        Map<String, Object> statistics = runningRecordService.getRunningStatistics();
        return ResponseVO.success("获取跑步统计数据成功", statistics);
    }
    
    /**
     * 管理员删除指定跑步记录
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/record/{id}")
    @RequireRole("ADMIN")
    public ResponseVO<?> deleteRunningRecord(@PathVariable("id") Long id) {
        boolean result = runningRecordService.deleteRecord(id);
        if (result) {
            return ResponseVO.success("删除跑步记录成功");
        } else {
            return ResponseVO.error("删除跑步记录失败，记录可能不存在");
        }
    }
} 