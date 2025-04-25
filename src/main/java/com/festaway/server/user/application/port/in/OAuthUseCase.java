package com.festaway.server.user.application.port.in;

import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuthUseCase {
    ResponseEntity<AuthResponse> processOAuth2User(OAuth2User oauth2User);

    String getRedirectUri();
}