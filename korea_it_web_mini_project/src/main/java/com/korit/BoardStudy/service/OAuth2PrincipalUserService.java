package com.korit.BoardStudy.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OAuth2PrincipalUserService extends DefaultOAuth2UserService {

    @Override // 메서드 재정의  loadUser
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 사용자 정보를 파싱한 엑세스 토큰을 가져옴
        Map<String, Object> attributes = oAuth2User.getAttributes();
//        System.out.println(attributes);
        // OAuth2 의 사용자 정보를 가져옴
        String provider = userRequest.getClientRegistration().getRegistrationId();
        // 공급처 가져옴
        System.out.println("provider : " + provider);
        String email = null;
        String id = null;
        // 스위치 문으로 이메일을 가져옴
        switch (provider) {
            case "google" : // 구글일때
                id = attributes.get("sub") .toString();
                email =(String) attributes.get("email"); // String 값으로 attributes 가져옴
                break;

            case "naver" :
                Map<String, Object> response = (Map<String, Object>) attributes.get("response"); // 네이버의 경우 response가 한번 더 쌓여있어서 벗겨내야함
                id = response.get("id").toString(); // 아이디를 String으로 받아옴
                email = (String) response.get("email"); // 이메일을 String으로 받아옴
                break;

            case "kakao" :
                id = attributes.get("id").toString();
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                // 반환값이 맵 형태로 감싸져있어서 뽑아와야함
//                email = (String) kakaoAccount.get("email");
                email = "example@naver.com";
                break;
        }
        Map<String, Object> newAttributes = Map.of(
                "id", id,
                "provider",provider,
                "email",email // 이 형태로 받아옴
        );

        // 임시 권한 만들어줌
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_TEMPORARY"));

        return new DefaultOAuth2User(authorities, newAttributes,"id");


    }
}