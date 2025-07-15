package com.korit.BoardStudy.dto.OAuth2;

import com.korit.BoardStudy.entity.OAuth2User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OAuth2MergeReqDto {
    private String username;
    private String password;
    private String provider;
    private String providerUserId;

    public OAuth2User toOAuth2User(Integer userId) {
        return OAuth2User.builder()
                .userId(userId)
                .providerUserId(providerUserId)
                .provider(provider)
                .build();
    }
}
