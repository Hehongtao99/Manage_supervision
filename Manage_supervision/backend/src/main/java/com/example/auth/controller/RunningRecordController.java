package com.example.auth.controller;

import com.example.auth.util.JwtUtil;
import com.example.auth.model.dto.request.RunningRecordRequest;
import com.example.auth.model.dto.response.RunningRecordResponse;
import com.example.auth.model.dto.response.RunningStatsResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.RunningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跑步记录控制器
 */
@RestController
@RequestMapping("/api/runner/running")
public class RunningRecordController {

    @Autowired
    private RunningRecordService runningRecordService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 添加跑步记录
     *
     * @param token JWT令牌
     * @param request 跑步记录请求
     * @return 添加的跑步记录
     */
    @PostMapping("/record")
    public ResponseVO<RunningRecordResponse> addRecord(
            @RequestHeader("Authorization") String token,
            @RequestBody RunningRecordRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        RunningRecordResponse response = runningRecordService.addRecord(userId, request);
        return ResponseVO.success("添加跑步记录成功", response);
    }

    /**
     * 获取当前用户的跑步记录列表
     *
     * @param token JWT令牌
     * @return 跑步记录列表
     */
    @GetMapping("/records")
    public ResponseVO<List<RunningRecordResponse>> getRecords(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<RunningRecordResponse> records = runningRecordService.getRecordsByUserId(userId);
        return ResponseVO.success("获取跑步记录列表成功", records);
    }

    /**
     * 获取特定ID的跑步记录
     *
     * @param token JWT令牌
     * @param id 记录ID
     * @return 跑步记录
     */
    @GetMapping("/record/{id}")
    public ResponseVO<RunningRecordResponse> getRecordById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id) {
        RunningRecordResponse record = runningRecordService.getRecordById(id);
        if (record == null) {
            return ResponseVO.error("跑步记录不存在");
        }
        return ResponseVO.success("获取跑步记录成功", record);
    }
    
    /**
     * 获取当前用户的跑步统计数据
     *
     * @param token JWT令牌
     * @return 跑步统计数据
     */
    @GetMapping("/stats")
    public ResponseVO<RunningStatsResponse> getRunningStats(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        RunningStatsResponse stats = runningRecordService.getUserRunningStats(userId);
        return ResponseVO.success("获取跑步统计数据成功", stats);
    }
} 