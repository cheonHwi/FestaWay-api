package com.festaway.server.user.application.service;

import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.application.port.in.OAuthUseCase;
import com.festaway.server.user.domain.SocialLoginType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class oAuthService implements OAuthUseCase {
    @Override
    public ResponseEntity<AuthResponse> processOAuth(String code, SocialLoginType loginType) {

        return null;
    }
}
