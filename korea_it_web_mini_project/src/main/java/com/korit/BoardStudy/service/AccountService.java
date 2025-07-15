package com.korit.BoardStudyReview.service;

import com.korit.BoardStudyReview.dto.ApiRespDto;
import com.korit.BoardStudyReview.dto.account.ChangePasswordReqDto;
import com.korit.BoardStudyReview.entity.User;
import com.korit.BoardStudyReview.repositroy.UserRepository;
import com.korit.BoardStudyReview.sequrity.model.PrincipalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // 비밀번호 변경
    public ApiRespDto<?> changePassword(ChangePasswordReqDto changePasswordReqDto, PrincipalUser principalUser) {
        // 해당 유저가 실제 토큰을 들고있는지 확인용 principaluser

        // principalUser 내 들어있는(가입 - 로그인 한) userId
        Optional<User> userByUserId = userRepository.getUserByUserId(principalUser.getUserId());
        // 비어있다면
        if (userByUserId.isEmpty()) {
            return new ApiRespDto<>("failed","존재하지 않는 사용자입니다.",null);
        }
        // 같지 않다면
        if (!Objects.equals(changePasswordReqDto.getUserId(), principalUser.getUserId())) {
            return new ApiRespDto<>("failed","잘못된 요청입니다.",null);
        }
        // 비밀번호 확인 - 평문 / 실제 db에 저장된 암호화된 비번
        if (!bCryptPasswordEncoder.matches(changePasswordReqDto.getOldPassword(), userByUserId.get().getPassword())) {
            return new ApiRespDto<>("failed", "기존 비밀번호가 일치하지 않습니다.",null);
        }

        // 이전 비번이랑 같을때
        if (bCryptPasswordEncoder.matches(changePasswordReqDto.getNewPassword(), userByUserId.get().getPassword())) {
            return new ApiRespDto<>("failed","새 비밀번호는 기존 비밀번호와 달라야 합니다.",null);
        }

        // 암호화된 (바뀐)비번을 db에 넣기위한 시도
        int result = userRepository.changePassword(changePasswordReqDto.toEntity(bCryptPasswordEncoder));

        // 처리된 값이 1이 아니면
        if (result != 1) {
            return new ApiRespDto<>("failed","문제가 발생했습니다.",null);
        }

        return new ApiRespDto<>("success","비밀번호 변경이 완료되었습니다. \n 다시 로그인 해주세요.",null);

    }
}