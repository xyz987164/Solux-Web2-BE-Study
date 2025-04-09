package com.project.googleSocialLogin.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo, String baseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, baseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest requestBase = defaultResolver.resolve(request);
        return customizeRequest(requestBase);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest requestBase = defaultResolver.resolve(request, clientRegistrationId);
        return customizeRequest(requestBase);
    }

    private OAuth2AuthorizationRequest customizeRequest(OAuth2AuthorizationRequest request) {
        if (request == null) return null;

        // ✅ 기존 파라미터 복사 및 추가
        Map<String, Object> additionalParams = new HashMap<>(request.getAdditionalParameters());
        additionalParams.put("access_type", "offline");
        additionalParams.put("prompt", "consent");

        // ✅ attributes에도 동일하게 반영 (일부 Spring Security 버그 우회용)
        Map<String, Object> attributes = new HashMap<>(request.getAttributes());
        attributes.put("access_type", "offline");
        attributes.put("prompt", "consent");

        log.info("✅ Authorization Request 커스터마이징: {}", additionalParams);

        return OAuth2AuthorizationRequest.from(request)
                .attributes(attributes)
                .additionalParameters(additionalParams)
                .build();
    }
}
