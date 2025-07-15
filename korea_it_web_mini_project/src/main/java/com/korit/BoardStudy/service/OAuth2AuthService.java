package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.OAuth2.OAuth2MergeReqDto;
import com.korit.BoardStudy.dto.OAuth2.OAuth2SignupReqDto;
import com.korit.BoardStudy.entity.OAuth2User;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.entity.UserRole;
import com.korit.BoardStudy.repository.OAuth2UserRepository;
import com.korit.BoardStudy.repository.UserRepository;
import com.korit.BoardStudy.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class OAuth2AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2UserRepository oAuth2UserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> mergeAccount(OAuth2MergeReqDto oAuth2MergeReqDto) {
        // 해당 id의 정보가 있는지 확인
        Optional<User> optionalUser = userRepository.getUserByUsername(oAuth2MergeReqDto.getUsername());
        // userRepository의 getUserByUsername에 oAuth2MergeReqDto가 가지고있는 Username을 줌
        if (optionalUser.isEmpty()) { // 회원 정보가 없을때
            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요",null);
        }

        User user = optionalUser.get();

        // 이미 소셜 로그인 연동이 되어있는지
        Optional<OAuth2User> optionalOAuth2User = oAuth2UserRepository
                .getOAuth2UserByProviderAndProviderUserId(oAuth2MergeReqDto.getProvider(), oAuth2MergeReqDto.getProviderUserId());
        // 전달받은 provider + providerUserId로 이미 등록된 소셜 계정이 있는지 확인
        if (optionalOAuth2User.isPresent()) { // 계정이 존재 한다면,
            return new ApiRespDto<>("failed", "이 계정은 이미 소셜 계정과 연동되어 있습니다.",null);
        }

        if (!bCryptPasswordEncoder.matches(oAuth2MergeReqDto.getPassword(), user.getPassword())) { // 평문 password / 암호화된 기존 password 비교
            return new ApiRespDto<>("failed","사용자 정보를 확인하세요.",null);
        }

        try { // 여기서 예외가 발생하면 rollback
            int result = oAuth2UserRepository.addOAuth2User(oAuth2MergeReqDto.toOAuth2User(user.getUserId()));
            if (result != 1) {
                throw new RuntimeException("OAuth2 사용자 정보 연동에 실패했습니다.");
            }
            // 통과하면 성공
            return new ApiRespDto<>("success","성공적으로 계정 연동이 되었습니다.",null);
        } catch (Exception e) {
            return new ApiRespDto<>("failed", "계정 연동 중 오류가 발생했습니다. : " + e.getMessage(), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> signup(OAuth2SignupReqDto oAuth2SignupReqDto) {
        Optional<User> userByUsername = userRepository.getUserByUsername(oAuth2SignupReqDto.getUsername());
        // 이미 존재하는 아이디(username) 인지 먼저 확인
        if (userByUsername.isPresent()) { // 이미 username이 존재한다면,
            return new ApiRespDto<>("failed","이미 존재하는 아이디 입니다.",null);
        }

        Optional<User> userByEmail = userRepository.getUserByEmail(oAuth2SignupReqDto.getEmail());
        // 이미 존재하는 이메일인지 확인
        if (userByEmail.isPresent()) {
            return new ApiRespDto<>("failed","이미 존재하는 이메일입니다.",null);
        }


        Optional<OAuth2User> optionalOAuth2User = oAuth2UserRepository
                .getOAuth2UserByProviderAndProviderUserId(oAuth2SignupReqDto.getProvider(), oAuth2SignupReqDto.getProviderUserId());
        // 이미 연동되어있으면
        if (optionalOAuth2User.isPresent()) {
            return new ApiRespDto<>("failed","이 계정은 이미 소셜 계정과 연동되어 있습니다.",null);
        }

        try {
            Optional<User> optionalUser = userRepository.addUser(oAuth2SignupReqDto.toEntity(bCryptPasswordEncoder));
            // 소셜계정으로 가입 시도하는 유저정보(password는 암호화 시켜야함)를 DB에 추가 시도(addUser)
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("회원 정보 추가에 실패했습니다.");
            }

            User user = optionalUser.get();

            UserRole userRole = UserRole.builder()
                    .userId(user.getUserId()) // 추가하고 난 다음 key가 여기 있을것임
                    .roleId(3) // 임시 사용자
                    .build();

            // 위 결과가 아래 변수로 들어감
            int addUserRoleResult = userRoleRepository.addUserRole(userRole);
            if (addUserRoleResult != 1) {
                throw new RuntimeException("권한 정보 추가에 실패했습니다.");
            }

            int oauth2InsertResult = oAuth2UserRepository.addOAuth2User(oAuth2SignupReqDto.toOAuth2User(user.getUserId()));
            // 받아온 user값을 dto에 넣어주고, repository 내에서 int값이 나오도록 해줌 - 그걸 받아옴
            if (oauth2InsertResult != 1) {
                throw new RuntimeException("OAuth2 사용자 정보 추가에 실패했습니다.");
            }

            return new ApiRespDto<>("success" , "정상적으로 회원가입이 완료되었습니다.",user);
            // 추가된 유저 객체를 반환시켜줌
        } catch (Exception e) {
            return new ApiRespDto<>("failed","회원가입 중 오류가 발생했습니다 : " + e.getMessage(), null);
        }
    }
}