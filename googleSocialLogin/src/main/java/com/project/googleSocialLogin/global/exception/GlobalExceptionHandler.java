package com.project.googleSocialLogin.global.exception;


import com.project.googleSocialLogin.global.dto.ApiResponseTemplete;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        // 예외 메시지에서 "Unknown status"를 포함하고 있는 경우 처리
        if (e.getMessage().contains("Unknown status")) {
            // 특정 'Unknown status' 메시지를 사용자 정의 에러로 처리
            return ApiResponseTemplete.error(ErrorCode.INVALID_ENUM_VALUE, "Invalid status value provided: " + extractInvalidStatus(e));
        }
        // 다른 HttpMessageNotReadableException 처리
        return ApiResponseTemplete.error(ErrorCode.INVALID_REQUEST, "Malformed request: " + e.getMessage());
    }

    // 예외 메시지에서 잘못된 Status 값만 추출하는 유틸리티 메서드
    private String extractInvalidStatus(HttpMessageNotReadableException e) {
        String message = e.getMessage();
        // "Unknown status: ddd" 형식의 메시지를 처리
        if (message.contains("Unknown status")) {
            return message.split(":")[1].trim(); // "ddd"
        }
        return "Unknown status";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        // 기타 IllegalArgumentException 처리 (예: 리소스가 없을 경우)
        return ApiResponseTemplete.error(ErrorCode.RESOURCE_NOT_FOUND, "Resource not found: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleGeneralException(Exception e) {
        // 기타 예외 처리
        return ApiResponseTemplete.error(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error occurred: " + e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseTemplete<String>> handleMissingParamException(MissingServletRequestParameterException e) {
        return ApiResponseTemplete.error(ErrorCode.LOGIN_USER_FAILED,  e.getMessage() );

    }
}