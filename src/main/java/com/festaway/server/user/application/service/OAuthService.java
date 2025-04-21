package com.festaway.server.user.application.service;

import com.festaway.server.common.util.JwtProvider;
import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import com.festaway.server.user.adapter.out.persistence.MemberRepository;
import com.festaway.server.user.application.port.in.OAuthUseCase;
import com.festaway.server.user.domain.SocialLoginType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuthService implements OAuthUseCase {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Override
    public ResponseEntity<AuthResponse> processOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        MemberEntity member = memberRepository.findByEmail((String) attributes.get("email"))
                .orElseGet(() -> createMember(attributes));

        String accessToken = jwtProvider.generateToken(member);
        return ResponseEntity.ok(AuthResponse.of(accessToken, member));
    }

    @Override
    public String getRedirectUri() {
        String redirectUri = "http://localhost:8080/login/oauth2/code/google";  // GCP에 등록된 URI와 정확히 일치
        return
                "https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=" + clientId +
                        "&redirect_uri=" + redirectUri +
                        "&response_type=code" +
                        "&scope=email%20profile" +
                        "&access_type=offline" +
                        "&prompt=consent";
    }

    private MemberEntity createMember(Map<String, Object> attributes) {
        MemberEntity member = MemberEntity.builder()
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .socialType(SocialLoginType.GOOGLE)
                .socialId((String) attributes.get("sub"))
                .build();

        return memberRepository.save(member);
    }
}