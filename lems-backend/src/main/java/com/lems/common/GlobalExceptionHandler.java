
package com.lems.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务参数错误
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException e) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(400, e.getMessage()));
    }

    // 处理数据库约束冲突
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(
                        409,
                        "数据冲突：设备编号可能重复，或设备存在关联维修记录"
                ));
    }

    // 处理其他未预期异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e) {

        e.printStackTrace();

        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(
                        500,
                        "服务器内部错误，请联系管理员"
                ));
    }
}
