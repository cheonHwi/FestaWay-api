package com.festaway.server.common.security;

import com.festaway.server.user.adapter.out.persistence.MemberEntity;
import com.festaway.server.user.adapter.out.persistence.MemberRepository;
import com.festaway.server.user.domain.SocialLoginType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("asdasldoiyhasiougHRojzhfkjHDF");
        OAuth2User oauth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oauth2User);
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // 디버그용 로그 추가
        System.out.println("OAuth2 user attributes: " + attributes);
        
        String email = (String) attributes.get("email");
        if (email == null) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_email", "Email not found from OAuth2 provider", null)
            );
        }

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseGet(() -> createMember(attributes));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }

    private MemberEntity createMember(Map<String, Object> attributes) {
        MemberEntity member = MemberEntity.builder()
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .socialType(SocialLoginType.GOOGLE)
                .socialId((String) attributes.get("sub"))
                .build();

        return memberRepository.save(member);
    }
}