package com.festaway.server.user.application.service;

import com.festaway.server.common.util.JwtProvider;
import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import com.festaway.server.user.adapter.out.persistence.MemberRepository;
import com.festaway.server.user.application.port.in.GoogleOAuthUseCase;
import com.festaway.server.user.domain.SocialLoginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoogleOAuthService implements GoogleOAuthUseCase {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Override
    public ResponseEntity<AuthResponse> processOAuthCode(String code) {
        String accessToken = getAccessToken(code);
        if (accessToken == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> userAttributes = getUserInfo(accessToken);
        if (userAttributes == null) {
            return ResponseEntity.badRequest().build();
        }

        MemberEntity member = memberRepository.findByEmail((String) userAttributes.get("email"))
                .orElseGet(() -> createMember(userAttributes));

        String jwtToken = jwtProvider.generateToken(member);
        return ResponseEntity.ok(AuthResponse.of(jwtToken, member));
    }

    @Override
    public String getRedirectUri() {
        String redirectUri = "http://localhost:8080/api/auth/login/googleCallBack";
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

    private String getAccessToken(String code) {
        String redirectUri = "http://localhost:8080/api/auth/login/googleCallBack";
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return (String) Objects.requireNonNull(response.getBody()).get("access_token");
        } catch (Exception e) {
            log.error("Google 액세스 토큰 요청 실패", e);
            return null;
        }
    }

    private Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Google 사용자 정보 요청 실패", e);
            return null;
        }
    }


}