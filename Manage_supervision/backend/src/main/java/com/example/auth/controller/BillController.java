package com.example.auth.controller;

import com.example.auth.dto.BillDTO;
import com.example.auth.dto.BillGenerateDTO;
import com.example.auth.dto.BillPaymentDTO;
import com.example.auth.service.BillService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);
    private final BillService billService;

    @Autowired
    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public ResponseEntity<Page<BillDTO>> getAllBills(Pageable pageable) {
        Page<BillDTO> bills = billService.getAllBills(pageable);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id) {
        BillDTO bill = billService.getBillById(id);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<BillDTO>> getBillsByStudentId(@PathVariable Long studentId) {
        List<BillDTO> bills = billService.getBillsByStudentId(studentId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/student/{studentId}/page")
    public ResponseEntity<Page<BillDTO>> getBillsByStudentIdPaged(
            @PathVariable Long studentId,
            Pageable pageable) {
        Page<BillDTO> bills = billService.getBillsByStudentId(studentId, pageable);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BillDTO>> getBillsByStatus(
            @PathVariable Boolean status,
            Pageable pageable) {
        Page<BillDTO> bills = billService.getBillsByStatus(status, pageable);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<Page<BillDTO>> getBillsByClassId(
            @PathVariable Long classId,
            Pageable pageable) {
        try {
            logger.info("获取班级ID={}的所有学生账单", classId);
            Page<BillDTO> bills = billService.getBillsByClassId(classId, pageable);
            return ResponseEntity.ok(bills);
        } catch (EntityNotFoundException e) {
            logger.warn("班级不存在: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Page.empty(pageable));
        } catch (Exception e) {
            logger.error("获取班级账单失败: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Page.empty(pageable));
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Page<BillDTO>> getBillsByTeacherId(
            @PathVariable Long teacherId,
            Pageable pageable) {
        try {
            logger.info("获取教师ID={}管理的所有班级学生账单", teacherId);
            Page<BillDTO> bills = billService.getBillsByTeacherId(teacherId, pageable);
            return ResponseEntity.ok(bills);
        } catch (EntityNotFoundException e) {
            logger.warn("教师不存在: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Page.empty(pageable));
        } catch (Exception e) {
            logger.error("获取教师管理班级账单失败: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Page.empty(pageable));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BillDTO> updateBillStatus(
            @PathVariable Long id,
            @RequestParam Boolean status) {
        BillDTO updatedBill = billService.updateBillStatus(id, status);
        return ResponseEntity.ok(updatedBill);
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payBill(
            @PathVariable Long id,
            @RequestBody BillPaymentDTO paymentDTO) {
        logger.info("接收到账单支付请求: 账单ID={}, 支付方式={}", id, paymentDTO.getPaymentMethod());
        try {
            BillDTO updatedBill = billService.payBill(id, paymentDTO);
            return ResponseEntity.ok(updatedBill);
        } catch (IllegalStateException e) {
            logger.warn("支付账单时出现业务异常: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        } catch (EntityNotFoundException e) {
            logger.warn("尝试支付不存在的账单: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("支付账单时发生异常: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("支付账单失败：" + e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateBills(@RequestBody BillGenerateDTO generateDTO) {
        logger.info("接收到账单生成请求: {}", generateDTO);
        try {
            if (generateDTO.getFeeStandardId() == null) {
                logger.error("费用标准ID不能为空");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("费用标准ID不能为空"));
            }
            
            if (generateDTO.getDueDate() == null) {
                logger.error("账单截止日期不能为空");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("账单截止日期不能为空"));
            }
            
            List<BillDTO> generatedBills = billService.generateBills(generateDTO);
            logger.info("成功生成账单数量: {}", generatedBills.size());
            
            return new ResponseEntity<>(generatedBills, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("生成账单失败: {}", e.getMessage(), e);
            String errorMessage = "账单生成失败";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMessage += ": " + e.getMessage();
            }
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(errorMessage));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable Long id) {
        try {
            billService.deleteBill(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            logger.warn("删除账单时出现业务异常: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        } catch (EntityNotFoundException e) {
            logger.warn("尝试删除不存在的账单: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("删除账单时发生异常: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("删除账单失败：" + e.getMessage()));
        }
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
} 