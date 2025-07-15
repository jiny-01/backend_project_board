package com.korit.BoardStudy.mapper;

import com.korit.BoardStudy.entity.OAuth2User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface OAuth2UserMapper {

    //provider, provider id 가지고 있는지 확인
    Optional<OAuth2User> getOAuth2UserByProviderAndProviderUserId(String provider, String providerUserId);

    //OAuth2User 객체 넘기기
    int addOAuth2User(OAuth2User oAuth2User);
}
