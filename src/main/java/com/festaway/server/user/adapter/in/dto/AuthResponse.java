package com.festaway.server.user.adapter.in.dto;

import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private MemberResponse member;

    public static AuthResponse of(String accessToken, MemberEntity member) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .member(MemberResponse.from(member))
                .build();
    }
}
