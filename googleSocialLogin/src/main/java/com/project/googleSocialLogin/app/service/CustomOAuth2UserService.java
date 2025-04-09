package com.project.googleSocialLogin.app.service;


import com.project.googleSocialLogin.app.domain.Provider;
import com.project.googleSocialLogin.app.domain.RoleType;
import com.project.googleSocialLogin.app.domain.User;
import com.project.googleSocialLogin.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // OIDC 기본 서비스로 사용자 정보 로드
        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName(); // 또는 getGivenName(), getName() 등
        String sub = oidcUser.getSubject();   // providerId

        log.info("✅ OIDC 로그인 요청 감지: {}", email);

        User user = userRepository.findByEmail(email).orElseGet(() ->
                User.builder()
                        .email(email)
                        .userName(name)
                        .userNickName(name)
                        .emailVerified(true)
                        .provider(Provider.GOOGLE)
                        .providerId(sub)
                        .roleType(RoleType.USER)
                        .build()
        );

        // 존재 여부 관계없이 저장 (업데이트 용도 포함)
        userRepository.save(user);

        // Spring Security가 이해할 수 있는 사용자 객체 반환
        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleType().name())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}
