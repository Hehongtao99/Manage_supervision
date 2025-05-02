package com.example.auth.controller;

import com.example.auth.model.dto.request.RunningPreferenceRequest;
import com.example.auth.model.dto.response.RunningPreferenceResponse;
import com.example.auth.model.vo.ResponseVO;
import com.example.auth.service.RunningPreferenceService;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/running")
public class RunningPreferenceController {
    
    @Autowired
    private RunningPreferenceService runningPreferenceService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取当前用户的跑步偏好
     */
    @GetMapping("/preference")
    public ResponseVO<RunningPreferenceResponse> getPreference(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        RunningPreferenceResponse preference = runningPreferenceService.getPreferenceByUserId(userId);
        return ResponseVO.success(preference);
    }
    
    /**
     * 保存或更新用户的跑步偏好
     */
    @PostMapping("/preference")
    public ResponseVO<RunningPreferenceResponse> savePreference(
            @RequestHeader("Authorization") String token,
            @RequestBody RunningPreferenceRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        RunningPreferenceResponse savedPreference = runningPreferenceService.saveOrUpdatePreference(userId, request);
        return ResponseVO.success(savedPreference);
    }
    
    /**
     * 保存个人宣言
     */
    @PostMapping("/motto")
    public ResponseVO<Map<String, String>> saveMotto(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        
        // 获取当前的跑步偏好
        RunningPreferenceResponse preference = runningPreferenceService.getPreferenceByUserId(userId);
        
        // 创建请求对象
        RunningPreferenceRequest preferenceRequest = new RunningPreferenceRequest();
        
        if (preference != null) {
            // 复制现有数据
            preferenceRequest.setFrequency(preference.getFrequency());
            preferenceRequest.setPace(preference.getPace());
            preferenceRequest.setPreferredDistance(preference.getPreferredDistance());
            preferenceRequest.setEnvironment(preference.getEnvironment());
        }
        
        // 更新个人宣言
        String motto = request.get("motto");
        preferenceRequest.setMotto(motto);
        
        // 保存数据
        runningPreferenceService.saveOrUpdatePreference(userId, preferenceRequest);
        
        // 返回更新后的宣言
        Map<String, String> response = new HashMap<>();
        response.put("motto", motto);
        return ResponseVO.success(response);
    }
} 