package com.project.googleSocialLogin.global.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.googleSocialLogin.app.domain.User;
import com.project.googleSocialLogin.app.repository.UserRepository;
import com.project.googleSocialLogin.app.service.GoogleTokenService;
import com.project.googleSocialLogin.global.dto.ApiResponseTemplete;
import com.project.googleSocialLogin.global.security.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final GoogleTokenService googleTokenService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();

            log.info("🔐 OAuth2User attributes: {}", oAuth2User.getAttributes());

            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                log.error("❌ 이메일 정보 없음. attributes: {}", oAuth2User.getAttributes());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "이메일 정보를 찾을 수 없습니다.");
                return;
            }

            log.info("✅ OAuth2 로그인 성공 - 사용자 이메일: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("유저 정보를 찾을 수 없습니다."));

            // ✅ Spring Security가 저장한 OAuth2AuthorizedClient에서 refresh_token 추출
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
                String googleRefreshToken = authorizedClient.getRefreshToken().getTokenValue();
                user.setGoogleRefreshToken(googleRefreshToken);
                log.info("✅ Google refresh_token 저장 완료: {}", googleRefreshToken);
            } else {
                log.warn("❌ Google refresh_token이 존재하지 않습니다.");
            }

            // ✅ JWT 발급
            String jwtAccessToken = tokenService.createAccessToken(user.getEmail());
            String jwtRefreshToken = tokenService.createRefreshToken();

            user.updateRefreshToken(jwtRefreshToken);
            userRepository.save(user);

            log.info("🎫 JWT Access Token 발급 완료: {}", jwtAccessToken);
            log.info("🔄 JWT Refresh Token 발급 완료: {}", jwtRefreshToken);

            Map<String, String> tokenMap = Map.of(
                    "email", user.getEmail(),
                    "accessToken", jwtAccessToken
            );

            ApiResponseTemplete<Map<String, String>> apiResponse = ApiResponseTemplete.<Map<String, String>>builder()
                    .status(200)
                    .success(true)
                    .message("OAuth2 로그인 성공")
                    .data(tokenMap)
                    .build();

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        } catch (Exception e) {
            log.error("💥 OAuth2 로그인 처리 중 예외 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 로그인 처리 중 오류 발생");
        }
    }
}
