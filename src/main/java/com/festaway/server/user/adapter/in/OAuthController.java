package com.festaway.server.user.adapter.in;

import com.festaway.server.user.adapter.in.dto.AuthResponse;
import com.festaway.server.user.application.port.in.GoogleOAuthUseCase;
import com.festaway.server.user.application.port.in.NaverOAuthUseCase;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {
    private final GoogleOAuthUseCase googleOAuthUseCase;
    private final NaverOAuthUseCase naverOAuthUseCase;

    @GetMapping("/login/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleOAuthUseCase.getRedirectUri());
    }

    @GetMapping("/login/naver")
    public void naverLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(naverOAuthUseCase.getRedirectUri());
    }


    @GetMapping("/login/googleCallBack")
    public ResponseEntity<AuthResponse> googleCallback(@RequestParam("code") String code) {
        return googleOAuthUseCase.processOAuthCode(code);
    }

    @GetMapping("/login/naverCallBack")
    public ResponseEntity<AuthResponse> naverCallback(@RequestParam("code") String code) {
        return naverOAuthUseCase.processOAuthCode(code);
    }

}
