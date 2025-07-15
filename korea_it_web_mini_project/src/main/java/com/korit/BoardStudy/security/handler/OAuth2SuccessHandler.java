package com.korit.BoardStudy.security.handler;


import com.korit.BoardStudy.entity.OAuth2User;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.repository.OAuth2UserRepository;
import com.korit.BoardStudy.repository.UserRepository;
import com.korit.BoardStudy.security.jwt.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

//인증 객체 만들어졌을 때 사용자 정보 DB 에서 확인 - 연동 여부
//연동 O : 토큰
//연동 X : prover id, email 넘겨줌
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    // 인증객체가 만들어진 다음에 어떻게 처리할지 여기서 구성]


    @Autowired
    private OAuth2UserRepository oAuth2UserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String provider = defaultOAuth2User.getAttribute("provider"); // provider 빼옴
        String providerUserId = defaultOAuth2User.getAttribute("id"); // id 빼옴
        String email = defaultOAuth2User.getAttribute("email");// email 빼옴

        Optional<OAuth2User> optionalOAuth2User = oAuth2UserRepository.getOAuth2UserByProviderAndProviderUserId(provider, providerUserId);

        if (optionalOAuth2User.isEmpty()) { // 만약 비교될 유저 정보가 없으면
            response.sendRedirect("http://localhost:3000/auth/oauth2?provider=" + provider +
                    "&providerUserId=" + providerUserId + "&email=" + email); // 위 3가지 정보를 클라이언트에게 넘겨줌
            return; // 중요쓰
        }

        // 이쪽으로 이미 UserId가 들어와있음
        OAuth2User oAuth2User = optionalOAuth2User.get(); // 이렇게 해놓으면

        Optional<User> optionalUser = userRepository.getUserByUserId(oAuth2User.getUserId()); // 여기서 바로 사용 가능
        // 유저객체를 찾는 이유 = AccessToken을 만들어줘야하기 때문

        String accessToken = null;
        if (optionalUser.isPresent()) { // 존재한다면
            accessToken = jwtUtils.generateAccessToken(optionalUser.get().getUserId().toString());
        }
        // 다시 클라이언트 페이지로 보냄
        response.sendRedirect("http://localhost:3000/auth/oauth2/signin?accessToken=" + accessToken);

    }
}
