package com.festaway.server.common.security;

import com.festaway.server.common.util.JwtProvider;
import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import com.festaway.server.user.adapter.out.persistence.MemberRepository;
import com.festaway.server.user.domain.SocialLoginType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .formLogin(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**", "/login/oauth2/code/**").permitAll()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
//                    .defaultSuccessUrl("/api/auth/login/googleCallBack")
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService)
                )
                .successHandler(oauthSuccessHandler())
                .failureHandler(oauthFailureHandler())
            );
            
        return http.build();
    }
    
    @Bean
    public AuthenticationFailureHandler oauthFailureHandler() {
        return (request, response, exception) -> {
            // 실패 원인을 자세히 로깅
            System.err.println("OAuth2 인증 실패: " + exception.getMessage());
            exception.printStackTrace();
            
            // 세부 예외 정보 추출
            String errorMessage = "인증에 실패했습니다.";
            String errorCode = "auth_error";
            
            if (exception instanceof OAuth2AuthenticationException) {
                OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
                errorCode = error.getErrorCode();
                
                if (error.getDescription() != null) {
                    errorMessage = error.getDescription();
                }
            }
            
            // 실패 원인에 따른 상세 메시지 설정
            if (exception.getCause() != null) {
                System.err.println("원인: " + exception.getCause().getMessage());
            }
            
            // JSON 응답으로 오류 정보 전송
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            
            String jsonError = String.format(
                "{\"error\":\"%s\",\"error_description\":\"%s\"}",
                errorCode, errorMessage.replace("\"", "\\\"")
            );
            
            response.getWriter().write(jsonError);
        };
    }
    
    // 기존 성공 핸들러 유지
    @Bean
    public AuthenticationSuccessHandler oauthSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

            // 사용자 정보 처리
            String email = (String) attributes.get("email");
            MemberEntity member = memberRepository.findByEmail(email)
                    .orElseGet(() -> {
                        MemberEntity newMember = MemberEntity.builder()
                                .email(email)
                                .name((String) attributes.get("name"))
                                .picture((String) attributes.get("picture"))
                                .socialType(SocialLoginType.GOOGLE)
                                .socialId((String) attributes.get("sub"))
                                .build();
                        return memberRepository.save(newMember);
                    });

            // JWT 토큰 생성
            String accessToken = jwtProvider.generateToken(member);

            // JSON 응답 설정
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK

            // 응답 JSON 생성
            AuthResponse authResponse = AuthResponse.of(accessToken, member);
            String jsonResponse = String.format(
                    "{\"accessToken\":\"%s\",\"id\":%d,\"email\":\"%s\",\"name\":\"%s\",\"picture\":\"%s\"}",
                    authResponse.getAccessToken(), member.getId(), member.getEmail(), member.getName(), member.getPicture()
            );

            // JSON 응답 전송
            response.getWriter().write(jsonResponse);
        };
    }

}