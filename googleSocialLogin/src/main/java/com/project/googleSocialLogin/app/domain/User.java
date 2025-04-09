package com.project.googleSocialLogin.app.domain;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "USER_EMAIL", nullable = false, unique = true)
    private String email;

    private String password;

    @Setter
    private String userName;

    @Setter
    @Column(nullable = false)
    private String userNickName;

    @Setter
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;


    @Column(nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING) // ENUM 사용
    @Column(nullable = false)
    private Provider provider;  // "Google", "Normal"

    @Column(nullable = true, unique = true)
    private String providerId;  // OAuth2 제공자의 고유 ID (일반 회원가입은 NULL)

    @Setter
    private String googleRefreshToken;

    // 이메일 인증 여부 반환
    public Boolean getEmailVerified() { return emailVerified; }
    // 이메일 인증 여부 설정
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    // 유저 권한 정보 반환
    public void authorizeUser() { this.roleType = RoleType.USER; }
    // 비밀번호 암호화 후 반환
    public void passwordEncode(PasswordEncoder passwordEncoder) { this.password = passwordEncoder.encode(this.password); }
    // 비밀번호 암호화 후 재설정
    public void setEncodedPassword(PasswordEncoder passwordEncoder, String newPassword) { this.password = passwordEncoder.encode(newPassword); }
    // 리프레쉬 토큰 반환
    public void updateRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public void updateGoogleRefreshToken(String googleRefreshToken) { this.googleRefreshToken = googleRefreshToken;}

    @Builder
    public User(Long id,
                String email, String password,
                String userName, String userNickName,
                String refreshToken, Boolean emailVerified,
                RoleType roleType, Provider provider, String providerId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.userNickName = userNickName;
        this.refreshToken = refreshToken;
        this.emailVerified = emailVerified;
        this.roleType = roleType;
        this.provider = provider;
        this.providerId = providerId;
    }
}
