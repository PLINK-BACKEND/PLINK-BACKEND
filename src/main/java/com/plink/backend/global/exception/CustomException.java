package com.plink.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 비즈니스 예외에서 공통적으로 사용할 커스텀 예외 클래스
 */
public class CustomException extends RuntimeException {
    private final HttpStatus status;

    public CustomException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
