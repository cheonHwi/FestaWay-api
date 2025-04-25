package com.festaway.server.user.application.port.in;

import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.domain.SocialLoginType;
import org.springframework.http.ResponseEntity;

public interface OAuthUseCase {
    ResponseEntity<AuthResponse> processOAuth(String code, SocialLoginType loginType);
}
