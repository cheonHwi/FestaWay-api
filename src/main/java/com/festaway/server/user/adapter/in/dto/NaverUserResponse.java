package com.festaway.server.user.adapter.in.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NaverUserResponse {
    private final String resultcode;
    private final String message;
    private final NaverUserProfile response;

    public record NaverUserProfile(
            String id,
            String nickname,
            String profile_image,
            String email)
    { }
}