package com.project.googleSocialLogin.app.controller;

import com.project.googleSocialLogin.app.repository.UserRepository;
import com.project.googleSocialLogin.global.dto.ApiResponseTemplete;
import com.project.googleSocialLogin.global.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserLoginController {


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";


    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseTemplete<String>> logout(HttpServletRequest request, HttpServletResponse response) {

        // 요청에서 액세스 토큰 추출
        String accessToken = tokenService.extractAccessToken(request).orElse(null);

        if (accessToken == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다. (액세스 토큰 없음)")
                            .data(null)
                            .build()
            );
        }

        // 토큰 유효성 검사 (토큰이 만료되었어도 로그아웃은 가능해야 함)
        boolean isValid = tokenService.validateToken(accessToken);

        // 만료된 토큰에서도 로그아웃 처리 가능하도록 수정
        tokenService.extractEmail(accessToken).ifPresent(tokenService::removeRefreshToken);

        // 클라이언트 쿠키/헤더에서 토큰 제거
        response.setHeader("Authorization", "");
        response.setHeader("Refresh-Token", "");

        return ResponseEntity.ok(
                ApiResponseTemplete.<String>builder()
                        .status(200)
                        .success(true)
                        .message("로그아웃 성공")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/login/google")
    public RedirectView redirectToGoogle() {
        return new RedirectView("/oauth2/authorization/google");
    }




}