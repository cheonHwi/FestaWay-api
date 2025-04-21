package com.festaway.server.user.adapter.in;

import com.festaway.server.common.util.JwtProvider;
import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import com.festaway.server.user.application.port.in.OAuthUseCase;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthUseCase oAuthUseCase;

    @GetMapping("/login/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(oAuthUseCase.getRedirectUri());
    }

    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<AuthResponse> googleCallback(@AuthenticationPrincipal OAuth2User oauth2User) {
        return oAuthUseCase.processOAuth2User(oauth2User);
    }

}
