package com.plink.backend.global.exception;

import org.springframework.http.HttpStatus;

// 공통으로 사용하는 커스텀 예외 클래스
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
