package com.festaway.server.user.adapter.out.persistence;

import com.festaway.server.user.domain.SocialLoginType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class MemberEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialLoginType socialType;

    @Column(name = "social_id")
    private String socialId;
}