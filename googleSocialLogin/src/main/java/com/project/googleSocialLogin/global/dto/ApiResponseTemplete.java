package com.project.googleSocialLogin.global.dto;


import com.project.googleSocialLogin.global.exception.ErrorCode;
import com.project.googleSocialLogin.global.exception.SuccessCode;
import lombok.*;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApiResponseTemplete<T> {
    private final int status;
    private final boolean success;
    private final String message;
    private T data;

    public static <T> ResponseEntity<ApiResponseTemplete<T>> success(SuccessCode successCode, T data) {
        return ResponseEntity.ok(ApiResponseTemplete.<T>builder()
                .status(successCode.getHttpStatus().value())
                .success(true)
                .message(successCode.getMessage())
                .data(data)
                .build());
    }

    public static <T> ResponseEntity<ApiResponseTemplete<T>> error(ErrorCode errorCode, T data) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponseTemplete.<T>builder()
                        .status(errorCode.getHttpStatus().value())
                        .success(false)
                        .message(errorCode.getMessage())
                        .data(data)
                        .build());
    }
}
