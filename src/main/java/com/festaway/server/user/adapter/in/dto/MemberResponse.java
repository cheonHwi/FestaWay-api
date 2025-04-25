package com.festaway.server.user.adapter.in.dto;

import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import com.festaway.server.user.domain.SocialLoginType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private String email;
    private String name;
    private String picture;
    private SocialLoginType socialType;

    public static MemberResponse from(MemberEntity member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .picture(member.getPicture())
                .socialType(member.getSocialType())
                .build();
    }
}