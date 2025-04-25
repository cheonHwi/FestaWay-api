package com.festaway.server.user.application.service;

import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import com.festaway.server.user.application.port.in.NaverOAuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService implements NaverOAuthUseCase {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
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
        System.out.println(clientId);
        String redirectUri= "http://localhost:8080/api/auth/login/naverCallBack";
        return
                "https://nid.naver.com/oauth2.0/authorize" +
                        "?response_type=code" +
                        "&client_id=" + clientId +
                        "&redirect_uri=" + redirectUri +
                        "&state=STATE_STRING";
    }

    private String getNaverAccessToken(String code) {
        String redirectUri = "http://localhost:8080/api/auth/login/naverCallBack";
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);  // redirectUri 파라미터 추가
        params.add("state", "STATE_STRING");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            System.out.println(response.getBody());
            return (String) Objects.requireNonNull(response.getBody()).get("access_token");
        } catch (Exception e) {
            log.error("Naver 액세스 토큰 요청 실패", e);  // 에러 메시지 수정
            return null;
        }
    }


}