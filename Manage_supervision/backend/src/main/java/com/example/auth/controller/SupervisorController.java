package com.example.auth.controller;

import com.example.auth.annotation.RequireRole;
import com.example.auth.model.dto.RunnerDTO;
import com.example.auth.model.dto.RunnerDetailDTO;
import com.example.auth.model.dto.UserDTO;
import com.example.auth.service.UserService;
import com.example.auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling supervisor-related requests
 */
@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    private static final Logger logger = LoggerFactory.getLogger(SupervisorController.class);

    @Autowired
    private UserService userService;

    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all runners list
     */
    @GetMapping("/runners")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getAllRunners() {
        logger.info("Supervisor requested all runners list");
        try {
            List<RunnerDTO> runners = userService.getAllRunners();
            return ResponseEntity.ok(runners);
        } catch (Exception e) {
            logger.error("Failed to get runners list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get runners list: " + e.getMessage()));
        }
    }

    /**
     * Get runners assigned to supervisor
     */
    @GetMapping("/runners/assigned")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getAssignedRunners(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replace("Bearer ", "");
            Long supervisorId = jwtUtil.getUserIdFromToken(token);
            logger.info("Supervisor requested their assigned runners list, ID: {}", supervisorId);
            
            List<UserDTO> runners = userService.getRunnersBySupervisor(supervisorId);
            if (!runners.isEmpty()) {
                logger.info("跑步爱好者数据示例: id={}, name={}, userNumber={}", 
                            runners.get(0).getId(), 
                            runners.get(0).getRealName(), 
                            runners.get(0).getUserNumber());
            } else {
                logger.info("未找到分配给管理员ID: {}的跑步爱好者", supervisorId);
            }
            return ResponseEntity.ok(runners);
        } catch (Exception e) {
            logger.error("Failed to get assigned runners list", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get assigned runners list: " + e.getMessage()));
        }
    }

    /**
     * Get runner details
     */
    @GetMapping("/runners/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> getRunnerDetails(@PathVariable Long id) {
        logger.info("Supervisor requested runner details, ID: {}", id);
        try {
            RunnerDetailDTO runner = userService.getRunnerDetails(id);
            return ResponseEntity.ok(runner);
        } catch (Exception e) {
            logger.error("Failed to get runner details", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to get runner details: " + e.getMessage()));
        }
    }

    /**
     * Update runner status
     */
    @PutMapping("/runners/{id}/status")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> updateRunnerStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        String status = statusData.get("status");
        logger.info("Supervisor requested to update runner status, ID: {}, New status: {}", id, status);
        
        if (status == null || (!status.equals("active") && !status.equals("inactive"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status value, must be 'active' or 'inactive'"));
        }
        
        try {
            boolean success = userService.updateRunnerStatus(id, status);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Runner status has been updated to: " + status,
                    "status", status
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to update runner status, runner may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to update runner status", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update runner status: " + e.getMessage()));
        }
    }

    /**
     * Delete runner
     */
    @DeleteMapping("/runners/{id}")
    @RequireRole("SUPERVISOR")
    public ResponseEntity<?> deleteRunner(@PathVariable Long id) {
        logger.info("Supervisor requested to delete runner, ID: {}", id);
        try {
            boolean success = userService.deleteRunner(id);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Runner has been successfully deleted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete runner, runner may not exist"));
            }
        } catch (Exception e) {
            logger.error("Failed to delete runner", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete runner: " + e.getMessage()));
        }
    }
}