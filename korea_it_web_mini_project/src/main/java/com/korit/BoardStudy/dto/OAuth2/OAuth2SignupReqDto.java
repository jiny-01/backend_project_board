package com.korit.BoardStudy.dto.OAuth2;

import com.korit.BoardStudy.entity.OAuth2User;
import com.korit.BoardStudy.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
@AllArgsConstructor
public class OAuth2SignupReqDto {
    private String username;
    private String password;
    private String email;
    private String provider;
    private String providerUserId;

    //User 엔티티
    public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder) {
        return User.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode(password))
                .email(email)
                .build();
    }

    //OAuth2User 엔티티 만들기
    public OAuth2User toOAuth2User(Integer userId) {
        return OAuth2User.builder()
                .userId(userId)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();
    }
}
