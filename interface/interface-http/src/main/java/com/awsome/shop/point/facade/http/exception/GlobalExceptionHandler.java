package com.awsome.shop.point.facade.http.exception;

import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.common.exception.SystemException;
import com.awsome.shop.point.facade.http.response.ErrorDetail;
import com.awsome.shop.point.facade.http.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<List<ErrorDetail>>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("[全局异常处理] 参数验证失败: {} 个字段错误", e.getBindingResult().getFieldErrorCount());
        List<ErrorDetail> errors = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.add(ErrorDetail.of(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400002, "请求参数无效", errors));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("[全局异常处理] 业务异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
        HttpStatus httpStatus = determineHttpStatus(e.getErrorCode());
        Integer httpErrorCode = parseHttpErrorCode(e.getErrorCode());
        return ResponseEntity.status(httpStatus)
                .body(Result.error(httpErrorCode, e.getErrorMessage()));
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Void>> handleSystemException(SystemException e) {
        log.error("[全局异常处理] 系统异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500001, "系统异常，请稍后重试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("[全局异常处理] 未知异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500002, "系统错误，请稍后重试"));
    }

    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return HttpStatus.OK;
        }
        String prefix = errorCode.contains("_") ? errorCode.substring(0, errorCode.indexOf("_")) : errorCode;
        return switch (prefix) {
            case "AUTH" -> HttpStatus.UNAUTHORIZED;
            case "AUTHZ" -> HttpStatus.FORBIDDEN;
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT" -> HttpStatus.CONFLICT;
            case "LOCKED" -> HttpStatus.LOCKED;
            default -> HttpStatus.OK;
        };
    }

    private Integer parseHttpErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return 500000;
        }
        try {
            String[] parts = errorCode.split("_");
            if (parts.length != 2) {
                return 500000;
            }
            String category = parts[0];
            int sequence = Integer.parseInt(parts[1]);
            int httpStatusPrefix = switch (category) {
                case "AUTH" -> 401;
                case "AUTHZ" -> 403;
                case "PARAM" -> 400;
                case "NOT_FOUND" -> 404;
                case "CONFLICT" -> 409;
                case "BIZ" -> 200;
                case "SYS" -> 500;
                default -> 500;
            };
            return httpStatusPrefix * 1000 + sequence;
        } catch (Exception e) {
            return 500000;
        }
    }
}
