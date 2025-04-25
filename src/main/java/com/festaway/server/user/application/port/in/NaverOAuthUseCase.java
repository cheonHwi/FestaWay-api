package com.festaway.server.user.application.port.in;

import com.festaway.server.user.adapter.in.dto.AuthResponse;
import org.springframework.http.ResponseEntity;

public interface NaverOAuthUseCase {
    ResponseEntity<AuthResponse> processOAuthCode(String code);
    String getRedirectUri();
}
