package com.example.auth.common.exception;

import com.example.auth.model.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseVO<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ResponseVO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseVO<?> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        
        for (FieldError error : fieldErrors) {
            errorMsg.append(error.getField()).append(": ").append(error.getDefaultMessage()).append(", ");
        }
        
        String message = errorMsg.length() > 0 ? errorMsg.substring(0, errorMsg.length() - 2) : "参数校验失败";
        log.error("参数校验异常: {}", message);
        return ResponseVO.error(400, message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseVO<?> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        
        for (FieldError error : fieldErrors) {
            errorMsg.append(error.getField()).append(": ").append(error.getDefaultMessage()).append(", ");
        }
        
        String message = errorMsg.length() > 0 ? errorMsg.substring(0, errorMsg.length() - 2) : "参数绑定失败";
        log.error("参数绑定异常: {}", message);
        return ResponseVO.error(400, message);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseVO<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseVO.error(500, "服务器内部错误: " + e.getMessage());
    }
} 