package com.plink.backend.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// 프로젝트 전역 예외 핸들러
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리하는 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of("message", ex.getMessage()));
    }

    // 예상치 못한 예외 처리하는 로직
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        ex.printStackTrace(); // 로그 확인용
        return ResponseEntity
                .internalServerError()
                .body(Map.of("message", "서버 내부 오류가 발생했습니다."));
    }
}
